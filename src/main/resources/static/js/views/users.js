import { api } from "../api.js";
import {
  avatarTone,
  confirmDialog,
  emptyState,
  escapeHtml,
  formatDate,
  icon,
  openModal,
  pageHeader,
  readForm,
  roleBadge,
  setButtonLoading,
  skeletonRows,
  toast,
} from "../ui.js";

const ROLES = [
  { value: "MEMBER", label: "Member" },
  { value: "PROJECT_MANAGER", label: "Project Manager" },
  { value: "ADMIN", label: "Administrator" },
];

function userKpiPercent(user) {
  const value = Number(user?.kpi);
  if (Number.isNaN(value) || user?.kpi === null || user?.kpi === undefined) return null;
  return Math.round(value <= 1 ? value * 100 : value);
}

export async function renderUsers(container, session) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Administration",
      title: "Users",
      description: "Kelola akun, role, dan indikator performa anggota workspace.",
      actions: `<button class="btn btn-primary" type="button" data-create-user>${icon("plus", 15)} User baru</button>`,
    })}
    ${skeletonRows(6)}
  `;

  let users = [];
  try {
    users = (await api.getUsers()) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Administration", title: "Users", description: "Kelola akun dan role anggota workspace." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat users",
        description: error.message || "Tidak dapat mengambil daftar user.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderUsers(container, session));
    toast(error.message || "Gagal memuat users.", "error");
    return;
  }

  const members = users.filter((user) => user.role === "MEMBER");
  const managers = users.filter((user) => user.role === "PROJECT_MANAGER");
  const admins = users.filter((user) => user.role === "ADMIN");
  const kpiValues = members.map(userKpiPercent).filter((value) => value !== null);
  const avgKpi = kpiValues.length
    ? kpiValues.reduce((total, value) => total + value, 0) / kpiValues.length
    : null;

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Administration",
      title: "Users",
      description: "Kelola akun, role, dan indikator performa anggota workspace.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        <button class="btn btn-primary" type="button" data-create-user>${icon("plus", 15)} User baru</button>
      `,
    })}

    <div class="stat-grid">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5">
        <div class="stat-top">
          <span class="stat-icon">${icon("users", 18)}</span>
          <span class="stat-trend">${icon("shield", 12)} ${admins.length} admin</span>
        </div>
        <div class="stat-value">${users.length}</div>
        <div class="stat-label">Total akun</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(2,132,199,.1);--stat-color:#0284c7">
        <div class="stat-top">
          <span class="stat-icon">${icon("user-check", 18)}</span>
          <span class="stat-trend">${icon("briefcase", 12)} pengelola</span>
        </div>
        <div class="stat-value">${managers.length}</div>
        <div class="stat-label">Project manager</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(22,163,74,.1);--stat-color:#16a34a">
        <div class="stat-top">
          <span class="stat-icon">${icon("user", 18)}</span>
          <span class="stat-trend">${icon("activity", 12)} kontributor</span>
        </div>
        <div class="stat-value">${members.length}</div>
        <div class="stat-label">Member</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(217,119,6,.1);--stat-color:#d97706">
        <div class="stat-top">
          <span class="stat-icon">${icon("target", 18)}</span>
          <span class="stat-trend">${icon("award", 12)} rata-rata</span>
        </div>
        <div class="stat-value">${avgKpi === null ? "—" : `${Math.round(avgKpi)}%`}</div>
        <div class="stat-label">KPI member</div>
      </article>
    </div>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>Daftar anggota</h3>
          <p>Gunakan pencarian dan filter role untuk menemukan anggota dengan cepat.</p>
        </div>
        <span class="text-muted" style="font-size:11.5px" id="user-count">${users.length} akun</span>
      </div>
      <div class="card-body">
        <div class="toolbar" style="margin-bottom:14px">
          <div class="search-box">
            ${icon("search", 15)}
            <input type="search" id="user-search" placeholder="Cari nama, email, kontak..." aria-label="Cari user">
          </div>
          <select class="select select-sm" id="user-role-filter" style="width:180px" aria-label="Filter role">
            <option value="">Semua role</option>
            ${ROLES.map((role) => `<option value="${role.value}">${role.label}</option>`).join("")}
          </select>
          <span class="spacer"></span>
          <span class="text-muted" style="font-size:10.5px">${icon("info", 12)} KPI diisi manual sebagai indikator awal.</span>
        </div>

        ${users.length ? `
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>Anggota</th>
                  <th>Role</th>
                  <th>Kontak</th>
                  <th>KPI</th>
                  <th>Bergabung</th>
                  <th style="text-align:right">Aksi</th>
                </tr>
              </thead>
              <tbody id="user-table-body">
                ${users.map((user) => userRow(user, session)).join("")}
              </tbody>
            </table>
          </div>
          <div id="user-filter-empty" class="empty-state hidden" style="margin-top:16px">
            <span class="empty-icon">${icon("search", 24)}</span>
            <h3>Tidak ada user yang cocok</h3>
            <p>Coba ubah kata kunci atau filter role.</p>
          </div>
        ` : emptyState({
          iconName: "users",
          title: "Belum ada user",
          description: "Tambahkan akun pertama untuk mulai membangun tim Anda.",
          action: '<button class="btn btn-primary" type="button" data-create-user>Tambah user</button>',
        })}
      </div>
    </section>
  `;

  const searchInput = container.querySelector("#user-search");
  const roleFilter = container.querySelector("#user-role-filter");
  const countLabel = container.querySelector("#user-count");
  const filterEmpty = container.querySelector("#user-filter-empty");
  const tableBody = container.querySelector("#user-table-body");

  function filterUsers() {
    const term = (searchInput?.value || "").trim().toLowerCase();
    const role = roleFilter?.value || "";
    let visible = 0;

    tableBody?.querySelectorAll("tr[data-user-row]").forEach((row) => {
      const matchesTerm = !term || (row.dataset.searchItem || "").toLowerCase().includes(term);
      const matchesRole = !role || row.dataset.role === role;
      const show = matchesTerm && matchesRole;
      row.classList.toggle("hidden", !show);
      if (show) visible += 1;
    });

    if (countLabel) countLabel.textContent = `${visible} akun`;
    filterEmpty?.classList.toggle("hidden", visible > 0 || (!term && !role));
  }

  searchInput?.addEventListener("input", filterUsers);
  roleFilter?.addEventListener("change", filterUsers);

  container.querySelectorAll("[data-refresh]").forEach((button) => {
    button.addEventListener("click", () => renderUsers(container, session));
  });
  container.querySelectorAll("[data-create-user]").forEach((button) => {
    button.addEventListener("click", () => openUserModal({ container, session }));
  });

  tableBody?.addEventListener("click", async (event) => {
    const actionButton = event.target.closest("[data-user-action]");
    if (!actionButton) return;

    const user = users.find((item) => item.id === actionButton.dataset.id);
    if (!user) return;

    if (actionButton.dataset.userAction === "edit") {
      openUserModal({ container, session, user });
    } else if (actionButton.dataset.userAction === "delete") {
      await deleteUser(user, container, session);
    }
  });
}

function userRow(user, session) {
  const kpi = userKpiPercent(user);
  const isSelf = user.id === session.id;
  const searchText = `${user.name || ""} ${user.email || ""} ${user.contact || ""} ${user.role || ""}`;

  return `
    <tr data-user-row data-role="${escapeHtml(user.role || "")}" data-search-item="${escapeHtml(searchText)}">
      <td>
        <div class="cell-main">
          <span class="avatar" data-tone="${avatarTone(user.id || user.email || user.name)}">${escapeHtml(
            (user.name || user.email || "?").split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase()
          )}</span>
          <span>
            <span class="cell-title">${escapeHtml(user.name || "Tanpa nama")} ${isSelf ? '<span class="badge badge-approved" style="margin-left:4px">Anda</span>' : ""}</span>
            <span class="cell-sub">${escapeHtml(user.email || "")}</span>
          </span>
        </div>
      </td>
      <td>${roleBadge(user.role)}</td>
      <td><span class="text-muted" style="font-size:11px">${escapeHtml(user.contact || "—")}</span></td>
      <td>
        ${kpi === null ? '<span class="text-muted" style="font-size:11px">Belum diisi</span>' : `
          <div style="display:flex;align-items:center;gap:8px;min-width:110px">
            <div class="progress" style="flex:1;height:5px">
              <div class="progress-bar ${kpi >= 80 ? "success" : kpi >= 50 ? "" : "warning"}" style="width:${Math.min(kpi, 100)}%"></div>
            </div>
            <strong style="font-size:10.5px">${kpi}%</strong>
          </div>
        `}
      </td>
      <td><span class="text-muted" style="font-size:10.5px">${formatDate(user.createdAt)}</span></td>
      <td>
        <div class="table-actions">
          <button class="icon-btn" type="button" data-user-action="edit" data-id="${escapeHtml(user.id)}" title="Edit user" aria-label="Edit user">
            ${icon("pencil", 14)}
          </button>
          <button class="icon-btn" type="button" data-user-action="delete" data-id="${escapeHtml(user.id)}"
            title="${isSelf ? "Anda tidak dapat menghapus akun sendiri" : "Hapus user"}"
            aria-label="Hapus user" style="color:${isSelf ? "var(--muted-2)" : "var(--danger)"}" ${isSelf ? "disabled" : ""}>
            ${icon("trash", 14)}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function openUserModal({ container, session, user = null }) {
  const editing = Boolean(user);
  const kpiValue = userKpiPercent(user);

  const modal = openModal({
    title: editing ? "Edit user" : "User baru",
    subtitle: editing
      ? "Perbarui data akun. Password tidak diubah dari halaman ini."
      : "Buat akun baru dan tentukan role-nya di workspace.",
    size: "modal-lg",
    content: `
      <form id="user-form" class="form-grid" novalidate>
        <div class="field">
          <label for="user-name">Nama lengkap</label>
          <input class="input" id="user-name" name="name" type="text" maxlength="120"
            placeholder="Contoh: Budi Santoso" value="${escapeHtml(user?.name || "")}" required>
        </div>
        <div class="field">
          <label for="user-email">Email</label>
          <input class="input" id="user-email" name="email" type="email" maxlength="160"
            placeholder="nama@perusahaan.com" value="${escapeHtml(user?.email || "")}" required>
        </div>
        ${editing ? "" : `
          <div class="field">
            <label for="user-password">Password awal</label>
            <input class="input" id="user-password" name="password" type="password" minlength="6"
              placeholder="Minimal 6 karakter" autocomplete="new-password" required>
          </div>
        `}
        <div class="field">
          <label for="user-role">Role</label>
          <select class="select" id="user-role" name="role" required>
            ${ROLES.map((role) => `
              <option value="${role.value}" ${(user?.role || "MEMBER") === role.value ? "selected" : ""}>${role.label}</option>
            `).join("")}
          </select>
        </div>
        <div class="field">
          <label for="user-contact">Kontak</label>
          <input class="input" id="user-contact" name="contact" type="text" maxlength="30"
            placeholder="Nomor telepon" value="${escapeHtml(user?.contact || "")}" required>
        </div>
        <div class="field">
          <label for="user-kpi">KPI (%)</label>
          <input class="input" id="user-kpi" name="kpi" type="number" min="0" max="100" step="1"
            placeholder="0 - 100" value="${kpiValue === null ? "" : kpiValue}">
          <span class="hint">Opsional. Nilai awal performa anggota.</span>
        </div>
        <div class="form-error span-2" id="user-form-error"></div>
      </form>
    `,
    footer: `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="user-form" id="user-submit">
        ${icon(editing ? "check" : "plus", 15)} ${editing ? "Simpan perubahan" : "Buat user"}
      </button>
    `,
  });

  const form = modal.body.querySelector("#user-form");
  const submitButton = modal.footer.querySelector("#user-submit");
  const errorBox = modal.body.querySelector("#user-form-error");

  modal.footer.querySelector("[data-cancel]").addEventListener("click", modal.close);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");

    const values = readForm(form);
    if (!values.name) {
      errorBox.textContent = "Nama lengkap wajib diisi.";
      errorBox.classList.add("show");
      return;
    }
    if (!values.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
      errorBox.textContent = "Masukkan alamat email yang valid.";
      errorBox.classList.add("show");
      return;
    }
    if (!editing && (!values.password || values.password.length < 6)) {
      errorBox.textContent = "Password awal minimal 6 karakter.";
      errorBox.classList.add("show");
      return;
    }
    if (!values.contact) {
      errorBox.textContent = "Kontak wajib diisi.";
      errorBox.classList.add("show");
      return;
    }

    let kpi = null;
    if (values.kpi !== "" && values.kpi !== undefined) {
      const parsed = Number(values.kpi);
      if (Number.isNaN(parsed) || parsed < 0 || parsed > 100) {
        errorBox.textContent = "KPI harus berada di antara 0 dan 100.";
        errorBox.classList.add("show");
        return;
      }
      kpi = parsed / 100;
    }

    const payload = {
      name: values.name,
      email: values.email,
      role: values.role,
      contact: values.contact,
      kpi,
    };

    setButtonLoading(submitButton, true, editing ? "Menyimpan..." : "Membuat...");
    try {
      if (editing) {
        await api.updateUser(user.id, payload);
        toast("Data user berhasil diperbarui.", "success");
      } else {
        await api.createUser({ ...payload, password: values.password });
        toast("User baru berhasil dibuat.", "success");
      }
      modal.close();
      await renderUsers(container, session);
    } catch (error) {
      errorBox.textContent = error.message || "Gagal menyimpan user.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

async function deleteUser(user, container, session) {
  const confirmed = await confirmDialog({
    title: "Hapus user?",
    message: `Akun <strong>${escapeHtml(user.name || user.email || "user")}</strong> akan dihapus permanen. Task dan riwayat yang terkait dapat ikut terpengaruh.`,
    confirmLabel: "Hapus user",
    danger: true,
  });
  if (!confirmed) return;

  try {
    await api.deleteUser(user.id);
    toast("User berhasil dihapus.", "success");
    await renderUsers(container, session);
  } catch (error) {
    toast(error.message || "Gagal menghapus user.", "error");
  }
}
