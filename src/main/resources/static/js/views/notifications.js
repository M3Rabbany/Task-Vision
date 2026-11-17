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

const PAGE_SIZE = 20;
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

function pageContent(page) {
  return Array.isArray(page?.content) ? page.content : [];
}

function pageTotal(page) {
  const total = Number(page?.totalElements);
  return Number.isFinite(total) ? total : pageContent(page).length;
}

export async function renderNotifications(container, session, params = {}) {
  const activeTab = params.tab === "unread" ? "unread" : "all";
  const typeFilter = ["INFO", "WARNING", "URGENT"].includes(params.type) ? params.type : "";
  let searchTerm = "";
  let requestVersion = 0;

  const state = {
    items: [],
    page: 0,
    totalPages: 0,
    totalElements: 0,
    hasMore: false,
    stats: { all: 0, unread: 0, urgent: 0 },
  };

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Inbox",
      title: "Notifications",
      description: "Pembaruan task, approval, dan aktivitas project Anda.",
    })}
    ${skeletonRows(5)}
  `;

  function listQuery(page) {
    const query = {
      page,
      size: PAGE_SIZE,
      sort: "createdAt,desc",
    };
    if (activeTab === "unread") query.read = "false";
    if (typeFilter) query.filterBy = typeFilter;
    if (searchTerm) query.search = searchTerm;
    return query;
  }

  async function loadPage(page, append = false) {
    const version = ++requestVersion;
    const response = await api.getFilteredNotifications(listQuery(page));
    if (version !== requestVersion) return;

    const data = unwrap(response) || {};
    const content = pageContent(data);
    state.items = append ? [...state.items, ...content] : content;
    state.page = Number(data.number) || page;
    state.totalPages = Number(data.totalPages) || 0;
    state.totalElements = pageTotal(data);
    state.hasMore = Boolean(data.hasNext) || state.page + 1 < state.totalPages;
  }

  async function loadStats() {
    const [all, unread, urgent] = await Promise.all([
      api.getFilteredNotifications({ page: 0, size: 1 }),
      api.getFilteredNotifications({ page: 0, size: 1, read: "false" }),
      api.getFilteredNotifications({ page: 0, size: 1, filterBy: "URGENT" }),
    ]);

    state.stats = {
      all: pageTotal(unwrap(all)),
      unread: pageTotal(unwrap(unread)),
      urgent: pageTotal(unwrap(urgent)),
    };
  }

  try {
    await Promise.all([loadPage(0), loadStats()]);
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

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Inbox",
      title: "Notifications",
      description: "Pembaruan task, approval, dan aktivitas project Anda.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        <button class="btn btn-primary" type="button" data-read-all ${state.stats.unread ? "" : "disabled"}>
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
        <div class="stat-value" id="notif-total">${state.stats.all}</div>
        <div class="stat-label">Semua notifikasi</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(2,132,199,.1);--stat-color:#0284c7">
        <div class="stat-top">
          <span class="stat-icon">${icon("mail", 18)}</span>
          <span class="stat-trend">${icon("eye", 12)} belum dibaca</span>
        </div>
        <div class="stat-value" id="notif-unread">${state.stats.unread}</div>
        <div class="stat-label">Belum dibaca</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(220,38,38,.1);--stat-color:#dc2626">
        <div class="stat-top">
          <span class="stat-icon">${icon("alert-triangle", 18)}</span>
          <span class="stat-trend" style="${state.stats.urgent ? "color:var(--danger)" : ""}">${icon("clock", 12)} prioritas</span>
        </div>
        <div class="stat-value" id="notif-urgent">${state.stats.urgent}</div>
        <div class="stat-label">Notifikasi urgent</div>
      </article>
    </div>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>Kotak masuk</h3>
          <p>Notifikasi terbaru muncul paling atas.</p>
        </div>
        <span class="text-muted" style="font-size:11.5px" id="notif-count">${state.totalElements} notifikasi</span>
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

        <div id="notif-list" class="notification-list"></div>
        <div id="notif-empty" class="hidden"></div>
        <div style="display:flex;justify-content:center;margin-top:14px">
          <button class="btn btn-outline btn-sm hidden" type="button" data-load-more>
            ${icon("chevron-down", 13)} Muat lebih banyak
          </button>
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
  const loadMoreButton = container.querySelector("[data-load-more]");

  function paintStats() {
    container.querySelector("#notif-total").textContent = String(state.stats.all);
    container.querySelector("#notif-unread").textContent = String(state.stats.unread);
    container.querySelector("#notif-urgent").textContent = String(state.stats.urgent);
    if (readAllButton) readAllButton.disabled = state.stats.unread === 0;
  }

  function paintList() {
    countLabel.textContent = `${state.totalElements} notifikasi`;
    loadMoreButton.classList.toggle("hidden", !state.hasMore);

    if (!state.items.length) {
      list.innerHTML = "";
      const filtered = Boolean(searchTerm || typeFilter || activeTab === "unread");
      empty.classList.remove("hidden");
      empty.innerHTML = emptyState({
        iconName: "bell",
        title: filtered ? "Tidak ada notifikasi yang cocok" : "Belum ada notifikasi",
        description: filtered
          ? "Coba ubah filter, tab, atau kata kunci pencarian."
          : "Pembaruan task, approval, dan aktivitas project akan muncul di sini.",
      });
      return;
    }

    empty.classList.add("hidden");
    list.innerHTML = state.items.map(notificationItem).join("");
  }

  paintList();

  container.querySelector("[data-refresh]")?.addEventListener("click", () => {
    renderNotifications(container, session, { tab: activeTab, type: typeFilter });
  });

  container.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => {
      window.location.hash = notificationsHash(button.dataset.tab, typeFilter);
    });
  });

  typeSelect?.addEventListener("change", () => {
    window.location.hash = notificationsHash(activeTab, typeSelect.value);
  });

  let searchTimer = null;
  searchInput?.addEventListener("input", () => {
    searchTerm = searchInput.value.trim();
    window.clearTimeout(searchTimer);
    searchTimer = window.setTimeout(async () => {
      try {
        await loadPage(0);
        paintList();
      } catch (error) {
        toast(error.message || "Gagal mencari notifikasi.", "error");
      }
    }, 300);
  });

  loadMoreButton?.addEventListener("click", async () => {
    if (!state.hasMore) return;

    setButtonLoading(loadMoreButton, true, "Memuat...");
    try {
      await loadPage(state.page + 1, true);
      paintList();
    } catch (error) {
      toast(error.message || "Gagal memuat notifikasi berikutnya.", "error");
    } finally {
      setButtonLoading(loadMoreButton, false);
    }
  });

  async function refreshNotifications() {
    await Promise.all([loadPage(0), loadStats()]);
    paintStats();
    paintList();
  }

  list.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-notification-read]");
    if (!button) return;

    setButtonLoading(button, true, "Menyimpan...");
    try {
      await api.markNotificationRead(button.dataset.notificationRead);
      toast("Notifikasi ditandai sudah dibaca.", "success");
      window.dispatchEvent(new CustomEvent("taskvision:notifications-updated"));
      await refreshNotifications();
    } catch (error) {
      toast(error.message || "Gagal menandai notifikasi.", "error");
      setButtonLoading(button, false);
    }
  });

  readAllButton?.addEventListener("click", async () => {
    if (!state.stats.unread) return;

    setButtonLoading(readAllButton, true, "Memproses...");
    try {
      const response = await api.getFilteredNotifications({ page: 0, size: 1000, read: "false" });
      const unreadItems = pageContent(unwrap(response));
      const ids = unreadItems.map((notification) => notification.id);
      const results = await Promise.allSettled(ids.map((id) => api.markNotificationRead(id)));
      const failed = results.filter((result) => result.status === "rejected").length;

      if (failed) {
        toast(`${ids.length - failed} notifikasi dibaca, ${failed} gagal diproses.`, "warning");
      } else {
        toast("Semua notifikasi ditandai sudah dibaca.", "success");
      }
      window.dispatchEvent(new CustomEvent("taskvision:notifications-updated"));
      await refreshNotifications();
    } catch (error) {
      toast(error.message || "Gagal menandai semua notifikasi.", "error");
    } finally {
      setButtonLoading(readAllButton, false);
    }
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
