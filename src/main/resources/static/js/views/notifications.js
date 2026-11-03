import { api, unwrap } from "../api.js";
import {
  emptyState,
  escapeHtml,
  formatDateTime,
  formatRelative,
  icon,
  pageHeader,
  setButtonLoading,
  skeletonRows,
  toast,
} from "../ui.js";

const TYPE_LABELS = {
  INFO: "Informasi",
  WARNING: "Peringatan",
  URGENT: "Urgent",
};

function isRead(notification) {
  if (!notification || typeof notification !== "object") return false;
  if (notification.read !== undefined) return Boolean(notification.read);
  if (notification.isRead !== undefined) return Boolean(notification.isRead);
  return false;
}

function notificationType(notification) {
  return String(notification?.type || "INFO").toUpperCase();
}

function typeBadge(type) {
  return `<span class="badge badge-${type.toLowerCase()}"><span class="badge-dot"></span>${escapeHtml(TYPE_LABELS[type] || type)}</span>`;
}

function notifIcon(type) {
  const className = type === "URGENT" ? " urgent" : type === "WARNING" ? " warning" : "";
  const name = type === "URGENT" ? "alert-triangle" : type === "WARNING" ? "clock" : "info";
  return `<span class="notif-icon${className}">${icon(name, 16)}</span>`;
}

function notificationsHash(tab, type) {
  const query = new URLSearchParams();
  if (tab === "unread") query.set("tab", "unread");
  if (type) query.set("type", type);
  const suffix = query.toString();
  return `#/notifications${suffix ? `?${suffix}` : ""}`;
}

export async function renderNotifications(container, session, params = {}) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Inbox",
      title: "Notifications",
      description: "Pembaruan task, approval, dan aktivitas project Anda.",
    })}
    ${skeletonRows(5)}
  `;

  let notifications = [];
  try {
    notifications = unwrap(await api.getNotifications(session.id)) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Inbox", title: "Notifications", description: "Pembaruan aktivitas dan task Anda." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat notifikasi",
        description: error.message || "Tidak dapat mengambil notifikasi.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderNotifications(container, session, params));
    toast(error.message || "Gagal memuat notifikasi.", "error");
    return;
  }

  notifications = [...notifications].sort(
    (a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
  );

  const activeTab = params.tab === "unread" ? "unread" : "all";
  const typeFilter = ["INFO", "WARNING", "URGENT"].includes(params.type) ? params.type : "";
  const unread = notifications.filter((notification) => !isRead(notification));
  const urgent = notifications.filter((notification) => notificationType(notification) === "URGENT");

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Inbox",
      title: "Notifications",
      description: "Pembaruan task, approval, dan aktivitas project Anda.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        <button class="btn btn-primary" type="button" data-read-all ${unread.length ? "" : "disabled"}>
          ${icon("check-circle", 15)} Tandai semua dibaca
        </button>
      `,
    })}

    <div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5">
        <div class="stat-top">
          <span class="stat-icon">${icon("bell", 18)}</span>
          <span class="stat-trend">${icon("activity", 12)} total</span>
        </div>
        <div class="stat-value">${notifications.length}</div>
        <div class="stat-label">Semua notifikasi</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(2,132,199,.1);--stat-color:#0284c7">
        <div class="stat-top">
          <span class="stat-icon">${icon("mail", 18)}</span>
          <span class="stat-trend">${icon("eye", 12)} belum dibaca</span>
        </div>
        <div class="stat-value">${unread.length}</div>
        <div class="stat-label">Belum dibaca</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(220,38,38,.1);--stat-color:#dc2626">
        <div class="stat-top">
          <span class="stat-icon">${icon("alert-triangle", 18)}</span>
          <span class="stat-trend" style="${urgent.length ? "color:var(--danger)" : ""}">${icon("clock", 12)} prioritas</span>
        </div>
        <div class="stat-value">${urgent.length}</div>
        <div class="stat-label">Notifikasi urgent</div>
      </article>
    </div>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>Kotak masuk</h3>
          <p>Notifikasi terbaru muncul paling atas.</p>
        </div>
        <span class="text-muted" style="font-size:11.5px" id="notif-count">${notifications.length} notifikasi</span>
      </div>
      <div class="card-body">
        <div class="toolbar" style="margin-bottom:14px">
          <div class="tabs" role="tablist" aria-label="Filter notifikasi">
            <button class="tab ${activeTab === "all" ? "active" : ""}" type="button" data-tab="all">Semua</button>
            <button class="tab ${activeTab === "unread" ? "active" : ""}" type="button" data-tab="unread">Belum dibaca</button>
          </div>
          <select class="select select-sm" id="notif-type-filter" style="width:170px" aria-label="Filter tipe notifikasi">
            <option value="">Semua tipe</option>
            <option value="INFO" ${typeFilter === "INFO" ? "selected" : ""}>Informasi</option>
            <option value="WARNING" ${typeFilter === "WARNING" ? "selected" : ""}>Peringatan</option>
            <option value="URGENT" ${typeFilter === "URGENT" ? "selected" : ""}>Urgent</option>
          </select>
          <span class="spacer"></span>
          <div class="search-box">
            ${icon("search", 15)}
            <input type="search" id="notif-search" placeholder="Cari notifikasi..." aria-label="Cari notifikasi">
          </div>
        </div>

        <div id="notif-list" class="notification-list">
          ${notifications.length
            ? notifications.map(notificationItem).join("")
            : `<div style="padding-top:2px">${emptyState({
                iconName: "bell",
                title: "Belum ada notifikasi",
                description: "Pembaruan task, approval, dan aktivitas project akan muncul di sini.",
              })}</div>`}
        </div>
        <div id="notif-empty" class="hidden">
          ${emptyState({
            iconName: "bell",
            title: "Tidak ada notifikasi yang cocok",
            description: "Coba ubah filter atau kata kunci pencarian.",
          })}
        </div>
      </div>
    </section>
  `;

  const list = container.querySelector("#notif-list");
  const empty = container.querySelector("#notif-empty");
  const countLabel = container.querySelector("#notif-count");
  const searchInput = container.querySelector("#notif-search");
  const typeSelect = container.querySelector("#notif-type-filter");
  const readAllButton = container.querySelector("[data-read-all]");

  function filterList() {
    const term = (searchInput?.value || "").trim().toLowerCase();
    const type = typeSelect?.value || "";
    let visible = 0;

    list.querySelectorAll("[data-notification]").forEach((item) => {
      const matchesTab = activeTab === "all" || item.dataset.read === "false";
      const matchesType = !type || item.dataset.type === type;
      const matchesTerm = !term || (item.dataset.searchItem || "").toLowerCase().includes(term);
      const show = matchesTab && matchesType && matchesTerm;
      item.classList.toggle("hidden", !show);
      if (show) visible += 1;
    });

    countLabel.textContent = `${visible} notifikasi`;
    empty.classList.toggle("hidden", visible > 0 || (!term && !type && activeTab === "all"));
  }

  filterList();
  searchInput?.addEventListener("input", filterList);
  typeSelect?.addEventListener("change", () => {
    window.location.hash = notificationsHash(activeTab, typeSelect.value);
  });

  container.querySelector("[data-refresh]")?.addEventListener("click", () => renderNotifications(container, session, { tab: activeTab, type: typeFilter }));
  container.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => {
      window.location.hash = notificationsHash(button.dataset.tab, typeFilter);
    });
  });

  list.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-notification-read]");
    if (!button) return;

    const id = button.dataset.notificationRead;
    setButtonLoading(button, true, "Menyimpan...");
    try {
      await api.markNotificationRead(id);
      toast("Notifikasi ditandai sudah dibaca.", "success");
      window.dispatchEvent(new CustomEvent("taskvision:notifications-updated"));
      await renderNotifications(container, session, { tab: activeTab, type: typeFilter });
    } catch (error) {
      toast(error.message || "Gagal menandai notifikasi.", "error");
      setButtonLoading(button, false);
    }
  });

  readAllButton?.addEventListener("click", async () => {
    const ids = unread.map((notification) => notification.id);
    if (!ids.length) return;

    setButtonLoading(readAllButton, true, "Memproses...");
    const results = await Promise.allSettled(ids.map((id) => api.markNotificationRead(id)));
    const failed = results.filter((result) => result.status === "rejected").length;

    if (failed) {
      toast(`${ids.length - failed} notifikasi dibaca, ${failed} gagal diproses.`, "warning");
    } else {
      toast("Semua notifikasi ditandai sudah dibaca.", "success");
    }
    window.dispatchEvent(new CustomEvent("taskvision:notifications-updated"));
    await renderNotifications(container, session, { tab: activeTab, type: typeFilter });
  });
}

function notificationItem(notification) {
  const type = notificationType(notification);
  const read = isRead(notification);
  const searchText = `${notification.content || ""} ${TYPE_LABELS[type] || type}`;

  return `
    <article class="notification-item ${read ? "" : "unread"}" data-notification
      data-read="${read}" data-type="${escapeHtml(type)}" data-search-item="${escapeHtml(searchText)}">
      ${notifIcon(type)}
      <div class="notif-content">
        <p>${escapeHtml(notification.content || "Notifikasi tanpa pesan")}</p>
        <div class="notif-meta">
          ${typeBadge(type)}
          <span>${icon("clock", 11)} ${escapeHtml(formatRelative(notification.createdAt))}</span>
          <span title="${escapeHtml(formatDateTime(notification.createdAt))}">${escapeHtml(formatDateTime(notification.createdAt))}</span>
        </div>
      </div>
      <div class="notif-actions">
        ${read
          ? '<span class="badge badge-neutral">Dibaca</span>'
          : `<button class="btn btn-outline btn-sm" type="button" data-notification-read="${escapeHtml(notification.id)}">
              ${icon("check", 12)} Tandai dibaca
            </button>`}
        ${read ? "" : '<span class="unread-dot" aria-label="Belum dibaca"></span>'}
      </div>
    </article>
  `;
}
