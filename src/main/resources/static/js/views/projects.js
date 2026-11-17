import { api, unwrap } from "../api.js";
import {
  avatarHtml,
  avatarStack,
  confirmDialog,
  deadlineState,
  deadlineText,
  emptyState,
  escapeHtml,
  formatDate,
  icon,
  openModal,
  pageHeader,
  readForm,
  roleBadge,
  setButtonLoading,
  skeletonCards,
  statusBadge,
  toast,
  toDateTimeInput,
} from "../ui.js";

function projectName(project) {
  return project.projectName || `Project ${String(project.id || "").slice(0, 6)}`;
}

export async function renderProjects(container, session) {
  const isAdmin = session.role === "ADMIN";
  const isManager = session.role === "PROJECT_MANAGER";
  const root = document.createElement("div");
  root.className = "fade-in";
  container.replaceChildren(root);

  root.innerHTML = `
    ${pageHeader({
      eyebrow: "Workspace",
      title: "Projects",
      description: isAdmin
        ? "Kelola seluruh project, anggota tim, dan tenggat waktu."
        : "Project yang melibatkan Anda beserta progres terbarunya.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        ${isAdmin ? `<button class="btn btn-primary" type="button" data-create>${icon("plus", 15)} Project baru</button>` : ""}
      `,
    })}
    <div id="projects-content">
      ${skeletonCards(6)}
    </div>
  `;

  root.querySelector("[data-refresh]").addEventListener("click", () => renderProjects(container, session));
  root.querySelector("[data-create]")?.addEventListener("click", () => openProjectModal({ root, session }));

  let projects = [];
  let users = [];

  try {
    const [projectResponse, userResponse] = await Promise.all([
      isAdmin ? api.getProjects() : api.getProjectsByUser(session.id),
      isAdmin ? api.getUsers() : Promise.resolve([]),
    ]);
    projects = unwrap(projectResponse) || [];
    users = userResponse || [];
  } catch (error) {
    const content = root.querySelector("#projects-content");
    content.innerHTML = emptyState({
      iconName: "alert-triangle",
      title: "Gagal memuat project",
      description: error.message || "Terjadi kesalahan saat mengambil data project.",
      action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
    });
    content.querySelector("[data-retry]")?.addEventListener("click", () => renderProjects(container, session));
    toast(error.message || "Gagal memuat project.", "error");
    return;
  }

  const active = projects.filter((project) => project.status === "IN_PROGRESS").length;
  const completed = projects.filter((project) => project.status === "COMPLETED").length;
  const overdue = projects.filter((project) => deadlineState(project.deadline) === "late").length;

  const content = root.querySelector("#projects-content");
  content.innerHTML = `
    <div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5">
        <div class="stat-top">
          <span class="stat-icon">${icon("folder-kanban", 18)}</span>
          <span class="stat-trend">${icon("activity", 12)} ${active} aktif</span>
        </div>
        <div class="stat-value">${projects.length}</div>
        <div class="stat-label">Total project</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(22,163,74,.1);--stat-color:#16a34a">
        <div class="stat-top">
          <span class="stat-icon">${icon("check-circle", 18)}</span>
          <span class="stat-trend">${projects.length ? Math.round((completed / projects.length) * 100) : 0}%</span>
        </div>
        <div class="stat-value">${completed}</div>
        <div class="stat-label">Project selesai</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(220,38,38,.1);--stat-color:#dc2626">
        <div class="stat-top">
          <span class="stat-icon">${icon("alert-triangle", 18)}</span>
          <span class="stat-trend" style="color:var(--danger)">${icon("clock", 12)} perlu perhatian</span>
        </div>
        <div class="stat-value">${overdue}</div>
        <div class="stat-label">Melewati deadline</div>
      </article>
    </div>

    <div class="toolbar">
      <div class="search-box">
        ${icon("search", 15)}
        <input type="search" id="project-search" placeholder="Cari project..." aria-label="Cari project">
      </div>
      <select class="select select-sm" id="project-status-filter" style="width:170px" aria-label="Filter status">
        <option value="">Semua status</option>
        <option value="IN_PROGRESS">In progress</option>
        <option value="COMPLETED">Completed</option>
      </select>
      <span class="spacer"></span>
      <span class="text-muted" style="font-size:11.5px" id="project-count">${projects.length} project</span>
    </div>

    <div id="project-grid">
      ${projects.length ? `
        <div class="project-grid">
          ${projects.map((project) => projectCard(project, { isAdmin, isManager })).join("")}
        </div>
        <div id="project-filter-empty" class="empty-state hidden" style="margin-top:18px">
          <span class="empty-icon">${icon("search", 24)}</span>
          <h3>Tidak ada project yang cocok</h3>
          <p>Coba ubah kata kunci atau filter status project.</p>
        </div>
      ` : emptyState({
        iconName: "folder-kanban",
        title: "Belum ada project",
        description: isAdmin
          ? "Mulai dengan membuat project pertama dan tugaskan anggota tim."
          : "Anda belum ditugaskan ke project mana pun. Hubungi administrator untuk informasi lebih lanjut.",
        action: isAdmin ? '<button class="btn btn-primary" type="button" data-create>Buat project pertama</button>' : "",
      })}
    </div>
  `;

  const searchInput = root.querySelector("#project-search");
  const statusFilter = root.querySelector("#project-status-filter");
  const countLabel = root.querySelector("#project-count");
  const filterEmpty = root.querySelector("#project-filter-empty");

  function filterProjects() {
    const term = searchInput.value.trim().toLowerCase();
    const status = statusFilter.value;
    const cards = root.querySelectorAll("[data-project-card]");
    let visible = 0;

    cards.forEach((card) => {
      const matchesText = !term || (card.dataset.searchItem || "").toLowerCase().includes(term);
      const matchesStatus = !status || card.dataset.status === status;
      const show = matchesText && matchesStatus;
      card.classList.toggle("hidden", !show);
      if (show) visible += 1;
    });

    countLabel.textContent = `${visible} project`;
    filterEmpty?.classList.toggle("hidden", visible > 0 || (!term && !status));
  }

  searchInput?.addEventListener("input", filterProjects);
  statusFilter?.addEventListener("change", filterProjects);

  root.querySelectorAll("[data-create]").forEach((button) => {
    button.addEventListener("click", () => openProjectModal({ root, session }));
  });

  content.addEventListener("click", async (event) => {
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton) return;

    const project = projects.find((item) => item.id === actionButton.dataset.id);
    if (!project) return;

    const action = actionButton.dataset.action;
    if (action === "edit") {
      openProjectModal({ root, session, project });
    } else if (action === "delete") {
      await deleteProject(project, root, session);
    } else if (action === "assign") {
      openAssignModal({ root, session, project, users });
    } else if (action === "open-tasks") {
      window.location.hash = `#/tasks?project=${encodeURIComponent(project.id)}`;
    }
  });

  content.addEventListener("change", async (event) => {
    const select = event.target.closest("[data-status-select]");
    if (!select) return;

    const project = projects.find((item) => item.id === select.dataset.id);
    if (!project) return;

    const previous = project.status;
    select.disabled = true;
    try {
      await api.updateProjectStatus(project.id, select.value);
      toast(`Status "${projectName(project)}" diperbarui.`, "success");
      await renderProjects(container, session);
    } catch (error) {
      select.value = previous;
      select.disabled = false;
      toast(error.message || "Gagal memperbarui status project.", "error");
    }
  });
}

function projectCard(project, { isAdmin, isManager }) {
  const members = project.assigned_to || [];
  const state = deadlineState(project.deadline);
  const searchText = `${projectName(project)} ${project.description || ""} ${project.status || ""} ${members.map((member) => member.name).join(" ")}`;

  return `
    <article class="project-card" data-project-card data-status="${escapeHtml(project.status || "")}" data-search-item="${escapeHtml(searchText)}">
      <div class="project-top">
        <span class="project-icon">${icon("folder-kanban", 19)}</span>
        <div class="flex items-center gap-1">
          ${isAdmin ? `
            <button class="icon-btn" style="width:30px;height:30px;border-color:transparent;background:transparent" type="button"
              data-action="assign" data-id="${escapeHtml(project.id)}" title="Atur anggota" aria-label="Atur anggota">
              ${icon("users", 15)}
            </button>
            <button class="icon-btn" style="width:30px;height:30px;border-color:transparent;background:transparent" type="button"
              data-action="edit" data-id="${escapeHtml(project.id)}" title="Edit project" aria-label="Edit project">
              ${icon("pencil", 15)}
            </button>
            <button class="icon-btn" style="width:30px;height:30px;border-color:transparent;background:transparent;color:var(--danger)" type="button"
              data-action="delete" data-id="${escapeHtml(project.id)}" title="Hapus project" aria-label="Hapus project">
              ${icon("trash", 15)}
            </button>
          ` : statusBadge(project.status)}
        </div>
      </div>

      <h3 class="project-title">${escapeHtml(projectName(project))}</h3>
      <p class="project-desc">${escapeHtml(project.description || "Belum ada deskripsi project.")}</p>

      <div class="project-meta">
        <div class="project-team">
          ${avatarStack(members, 4)}
          <span class="text-muted" style="font-size:10px">${members.length} anggota</span>
        </div>
        <span class="deadline-chip ${state}">
          ${icon("calendar", 12)} ${formatDate(project.deadline)}
        </span>
      </div>

      <div class="project-foot">
        <span class="deadline-chip ${state}" style="font-size:10px">
          ${icon("clock", 12)} ${deadlineText(project.deadline)}
        </span>
        <div class="flex items-center gap-2">
          ${isManager ? `
            <select class="select status-select" data-status-select data-id="${escapeHtml(project.id)}" aria-label="Ubah status project">
              <option value="IN_PROGRESS" ${project.status === "IN_PROGRESS" ? "selected" : ""}>In progress</option>
              <option value="COMPLETED" ${project.status === "COMPLETED" ? "selected" : ""}>Completed</option>
            </select>
          ` : ""}
          ${isAdmin || isManager ? `
            <button class="btn btn-outline btn-sm" type="button" data-action="open-tasks" data-id="${escapeHtml(project.id)}">
              ${icon("check-square", 13)} Board
            </button>
          ` : ""}
        </div>
      </div>
    </article>
  `;
}

function openProjectModal({ root, session, project = null }) {
  const editing = Boolean(project);
  const modal = openModal({
    title: editing ? "Edit project" : "Project baru",
    subtitle: editing
      ? "Perbarui informasi project tanpa mengubah status yang sedang berjalan."
      : "Tentukan nama, deskripsi, dan tenggat waktu project.",
    size: "modal-lg",
    content: `
      <form id="project-form" class="form-grid" novalidate>
        <div class="field span-2">
          <label for="project-name">Nama project</label>
          <input class="input" id="project-name" name="projectName" type="text" maxlength="120"
            placeholder="Contoh: Redesign dashboard mobile" value="${escapeHtml(project?.projectName || "")}" required>
        </div>
        <div class="field span-2">
          <label for="project-description">Deskripsi</label>
          <textarea class="textarea" id="project-description" name="description" maxlength="500"
            placeholder="Jelaskan tujuan dan cakupan project...">${escapeHtml(project?.description || "")}</textarea>
        </div>
        <div class="field">
          <label for="project-deadline">Deadline</label>
          <input class="input" id="project-deadline" name="deadline" type="datetime-local"
            value="${toDateTimeInput(project?.deadline)}" required>
        </div>
        <div class="field">
          <label>Status project</label>
          <input class="input" type="text" value="${escapeHtml(project?.status || "IN_PROGRESS")}" disabled>
          <span class="hint">${editing ? "Status diubah melalui tombol status pada kartu project." : "Project baru otomatis berstatus In progress."}</span>
        </div>
        <div class="form-error span-2" id="project-form-error"></div>
      </form>
    `,
    footer: `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="project-form" id="project-submit">
        ${icon(editing ? "check" : "plus", 15)} ${editing ? "Simpan perubahan" : "Buat project"}
      </button>
    `,
  });

  const form = modal.body.querySelector("#project-form");
  const submitButton = modal.footer.querySelector("#project-submit");
  const errorBox = modal.body.querySelector("#project-form-error");

  modal.footer.querySelector("[data-cancel]").addEventListener("click", modal.close);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");

    const values = readForm(form);
    if (!values.projectName) {
      errorBox.textContent = "Nama project wajib diisi.";
      errorBox.classList.add("show");
      return;
    }
    if (!values.deadline) {
      errorBox.textContent = "Deadline project wajib diisi.";
      errorBox.classList.add("show");
      return;
    }

    const payload = {
      projectName: values.projectName,
      description: values.description || "",
      deadline: values.deadline,
    };

    setButtonLoading(submitButton, true, editing ? "Menyimpan..." : "Membuat...");
    try {
      if (editing) {
        await api.updateProject(project.id, payload);
        toast("Project berhasil diperbarui.", "success");
      } else {
        await api.createProject(payload);
        toast("Project baru berhasil dibuat.", "success");
      }
      modal.close();
      await renderProjects(root.parentElement, session);
    } catch (error) {
      errorBox.textContent = error.message || "Gagal menyimpan project.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

function openAssignModal({ root, session, project, users }) {
  const existingIds = new Set((project.assigned_to || []).map((user) => user.id));
  const candidates = users.filter((user) => user.role !== "ADMIN");

  const modal = openModal({
    title: "Atur anggota project",
    subtitle: `Pilih anggota tim yang akan terlibat dalam "${projectName(project)}".`,
    content: candidates.length ? `
      <form id="assign-form" novalidate>
        <div class="check-list">
          ${candidates.map((user) => `
            <label class="check-item">
              <input type="checkbox" name="userIds" value="${escapeHtml(user.id)}" ${existingIds.has(user.id) ? "checked" : ""}>
              ${avatarHtml(user, "sm")}
              <span class="check-meta">
                <strong>${escapeHtml(user.name || "Member")}</strong>
                <span>${escapeHtml(user.email || "")} · ${escapeHtml(user.role === "PROJECT_MANAGER" ? "Project Manager" : "Member")}</span>
              </span>
              ${roleBadge(user.role)}
            </label>
          `).join("")}
        </div>
        <p class="hint" style="margin-top:9px">Anggota yang tidak dipilih akan dikeluarkan dari project ini.</p>
        <div class="form-error" id="assign-error" style="margin-top:12px"></div>
      </form>
    ` : emptyState({
      iconName: "users",
      title: "Belum ada anggota",
      description: "Tambahkan anggota tim terlebih dahulu melalui menu Users.",
      action: '<a class="btn btn-primary" href="#/users">Kelola users</a>',
    }),
    footer: candidates.length ? `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="assign-form" id="assign-submit">
        ${icon("user-check", 15)} Simpan anggota
      </button>
    ` : "",
  });

  modal.footer?.querySelector("[data-cancel]")?.addEventListener("click", modal.close);
  if (!candidates.length) return;

  const form = modal.body.querySelector("#assign-form");
  const submitButton = modal.footer.querySelector("#assign-submit");
  const errorBox = modal.body.querySelector("#assign-error");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");

    const selected = Array.from(form.querySelectorAll('input[name="userIds"]:checked')).map((input) => input.value);
    if (!selected.length) {
      errorBox.textContent = "Pilih minimal satu anggota untuk project ini.";
      errorBox.classList.add("show");
      return;
    }

    setButtonLoading(submitButton, true, "Menyimpan...");
    try {
      await api.assignProject(project.id, selected);
      toast("Anggota project berhasil diperbarui.", "success");
      modal.close();
      await renderProjects(root.parentElement, session);
    } catch (error) {
      errorBox.textContent = error.message || "Gagal memperbarui anggota project.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

async function deleteProject(project, root, session) {
  const confirmed = await confirmDialog({
    title: "Hapus project?",
    message: `Project <strong>${escapeHtml(projectName(project))}</strong> beserta task dan laporannya akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.`,
    confirmLabel: "Hapus project",
    danger: true,
  });
  if (!confirmed) return;

  try {
    await api.deleteProject(project.id);
    toast("Project berhasil dihapus.", "success");
    await renderProjects(root.parentElement, session);
  } catch (error) {
    toast(error.message || "Gagal menghapus project.", "error");
  }
}
