import { api, unwrap } from "../api.js";
import {
  avatarHtml,
  confirmDialog,
  deadlineState,
  deadlineText,
  emptyState,
  escapeHtml,
  formatDate,
  formatRelative,
  humanizeStatus,
  icon,
  openModal,
  pageHeader,
  readForm,
  setButtonLoading,
  skeletonRows,
  statusBadge,
  toast,
  toDateTimeInput,
} from "../ui.js";

const STATUS_COLUMNS = [
  { key: "ON_GOING", label: "On going", iconName: "activity" },
  { key: "PENDING", label: "Menunggu approval", iconName: "clock" },
  { key: "APPROVED", label: "Approved", iconName: "check-circle" },
  { key: "REJECTED", label: "Rejected", iconName: "circle-x" },
];

function projectName(project) {
  return project?.projectName || `Project ${String(project?.id || "").slice(0, 6)}`;
}

function taskName(task) {
  return task?.taskName || `Task ${String(task?.id || "").slice(0, 6)}`;
}

function findMember(project, task) {
  const members = project?.assigned_to || [];
  return members.find((member) => member.id === task?.assignedTo) || {
    id: task?.assignedTo || "",
    name: task?.assignedTo ? "Anggota tim" : "Belum ditugaskan",
    email: "",
  };
}

function isMeaningfulFeedback(feedback) {
  if (!feedback) return false;
  const title = String(feedback.title || "").toLowerCase();
  const message = String(feedback.feedback || "").toLowerCase();
  return title !== "no feedback" && !message.includes("no feedback available");
}

export async function renderTasks(container, session, params = {}) {
  if (session.role === "ADMIN") {
    await renderAdminTasks(container, session, params);
    return;
  }

  if (session.role === "PROJECT_MANAGER") {
    await renderManagerTasks(container, session, params);
    return;
  }

  await renderMemberTasks(container, session, params);
}

/* --------------------------------------------------------------------------
   Admin read-only board
   -------------------------------------------------------------------------- */

async function renderAdminTasks(container, session, params = {}) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Oversight",
      title: "Task board",
      description: "Tinjau task dan progres setiap project dalam mode baca saja.",
    })}
    ${skeletonRows(5)}
  `;

  let projects = [];
  try {
    projects = unwrap(await api.getProjects()) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Oversight", title: "Task board", description: "Tinjau pekerjaan tim per project." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat project",
        description: error.message || "Tidak dapat mengambil daftar project.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderAdminTasks(container, session, params));
    toast(error.message || "Gagal memuat project.", "error");
    return;
  }

  if (!projects.length) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Oversight", title: "Task board", description: "Tinjau pekerjaan tim per project." })}
      ${emptyState({
        iconName: "folder-kanban",
        title: "Belum ada project",
        description: "Buat project dan tugaskan project manager untuk mulai mengelola task.",
        action: '<a class="btn btn-primary" href="#/projects">Buat project</a>',
      })}
    `;
    return;
  }

  const selectedId = params.project && projects.some((project) => project.id === params.project)
    ? params.project
    : projects[0].id;
  const project = projects.find((item) => item.id === selectedId);

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Oversight",
      title: "Task board",
      description: "Tinjau task dan progres setiap project dalam mode baca saja.",
      actions: `<button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>`,
    })}

    <div class="toolbar">
      <label class="text-muted" style="font-size:11.5px" for="task-project-select">Project</label>
      <select class="select" id="task-project-select" style="min-width:230px" aria-label="Pilih project">
        ${projects.map((item) => `
          <option value="${escapeHtml(item.id)}" ${item.id === selectedId ? "selected" : ""}>
            ${escapeHtml(projectName(item))}
          </option>
        `).join("")}
      </select>
      <span class="spacer"></span>
      <div class="search-box">
        ${icon("search", 15)}
        <input type="search" id="task-search" placeholder="Cari task atau anggota..." aria-label="Cari task">
      </div>
    </div>

    <div style="display:flex;align-items:center;gap:8px;padding:10px 12px;margin-bottom:14px;border:1px solid var(--border);border-radius:10px;background:var(--surface-2);color:var(--muted);font-size:11.5px">
      ${icon("eye", 15)}
      <span>Mode baca saja. Perubahan task dilakukan oleh project manager.</span>
    </div>

    <div class="board-meta">
      <span>${statusBadge(project.status)}</span>
      <span>${icon("calendar", 13)} ${formatDate(project.deadline)}</span>
      <span>${icon("users", 13)} ${(project.assigned_to || []).length} anggota</span>
      <span class="spacer"></span>
      <span id="task-total">Memuat task...</span>
    </div>

    <div id="task-board-content">${skeletonRows(4)}</div>
  `;

  container.querySelector("[data-refresh]")?.addEventListener("click", () => {
    renderAdminTasks(container, session, { project: selectedId });
  });

  const projectSelect = container.querySelector("#task-project-select");
  projectSelect?.addEventListener("change", () => {
    window.location.hash = `#/tasks?project=${encodeURIComponent(projectSelect.value)}`;
  });

  const board = container.querySelector("#task-board-content");
  const totalLabel = container.querySelector("#task-total");

  let tasks = [];
  try {
    tasks = unwrap(await api.getProjectTasks(project.id)) || [];
  } catch (error) {
    board.innerHTML = emptyState({
      iconName: "alert-triangle",
      title: "Gagal memuat task",
      description: error.message || "Tidak dapat mengambil task pada project ini.",
      action: '<button class="btn btn-outline" type="button" data-retry>Coba lagi</button>',
    });
    board.querySelector("[data-retry]")?.addEventListener("click", () => {
      renderAdminTasks(container, session, { project: selectedId });
    });
    toast(error.message || "Gagal memuat task.", "error");
    return;
  }

  totalLabel.textContent = `${tasks.length} task`;
  board.innerHTML = renderKanban(project, tasks, { manageable: false, readOnly: true });

  const searchInput = container.querySelector("#task-search");
  searchInput?.addEventListener("input", () => filterBoard(board, searchInput.value));
}

/* --------------------------------------------------------------------------
   Project manager board
   -------------------------------------------------------------------------- */

async function renderManagerTasks(container, session, params = {}) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Execution",
      title: "Task board",
      description: "Buat, tugaskan, setujui, dan beri masukan untuk task tim.",
    })}
    ${skeletonRows(5)}
  `;

  let projects = [];
  try {
    projects = unwrap(await api.getProjectsByUser(session.id)) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Execution", title: "Task board", description: "Kelola pekerjaan tim per project." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat project",
        description: error.message || "Tidak dapat mengambil daftar project Anda.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderManagerTasks(container, session, params));
    toast(error.message || "Gagal memuat project.", "error");
    return;
  }

  if (!projects.length) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Execution", title: "Task board", description: "Kelola pekerjaan tim per project." })}
      ${emptyState({
        iconName: "folder-kanban",
        title: "Belum ada project",
        description: "Anda belum ditugaskan sebagai pengelola project. Hubungi administrator untuk menambahkan Anda ke project.",
        action: '<a class="btn btn-primary" href="#/projects">Lihat project</a>',
      })}
    `;
    return;
  }

  const selectedId = params.project && projects.some((project) => project.id === params.project)
    ? params.project
    : projects[0].id;
  const project = projects.find((item) => item.id === selectedId);

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Execution",
      title: "Task board",
      description: "Buat, tugaskan, setujui, dan beri masukan untuk task tim.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        <button class="btn btn-primary" type="button" data-create-task>${icon("plus", 15)} Task baru</button>
      `,
    })}

    <div class="toolbar">
      <label class="text-muted" style="font-size:11.5px" for="task-project-select">Project</label>
      <select class="select" id="task-project-select" style="min-width:230px" aria-label="Pilih project">
        ${projects.map((item) => `
          <option value="${escapeHtml(item.id)}" ${item.id === selectedId ? "selected" : ""}>
            ${escapeHtml(projectName(item))}
          </option>
        `).join("")}
      </select>
      <span class="spacer"></span>
      <div class="search-box">
        ${icon("search", 15)}
        <input type="search" id="task-search" placeholder="Cari task atau anggota..." aria-label="Cari task">
      </div>
    </div>

    <div class="board-meta">
      <span>${statusBadge(project.status)}</span>
      <span>${icon("calendar", 13)} ${formatDate(project.deadline)}</span>
      <span>${icon("users", 13)} ${(project.assigned_to || []).length} anggota</span>
      <span class="spacer"></span>
      <span id="task-total">Memuat task...</span>
    </div>

    <div id="task-board-content">${skeletonRows(4)}</div>
  `;

  container.querySelector("[data-refresh]")?.addEventListener("click", () => renderManagerTasks(container, session, { project: selectedId }));
  container.querySelector("[data-create-task]")?.addEventListener("click", () => openTaskModal({ container, session, project }));

  const projectSelect = container.querySelector("#task-project-select");
  projectSelect?.addEventListener("change", () => {
    window.location.hash = `#/tasks?project=${encodeURIComponent(projectSelect.value)}`;
  });

  const board = container.querySelector("#task-board-content");
  const totalLabel = container.querySelector("#task-total");
  let tasks = [];

  try {
    tasks = unwrap(await api.getProjectTasks(project.id)) || [];
  } catch (error) {
    board.innerHTML = emptyState({
      iconName: "alert-triangle",
      title: "Gagal memuat task",
      description: error.message || "Tidak dapat mengambil task pada project ini.",
      action: '<button class="btn btn-outline" type="button" data-retry>Coba lagi</button>',
    });
    board.querySelector("[data-retry]")?.addEventListener("click", () => renderManagerTasks(container, session, { project: selectedId }));
    toast(error.message || "Gagal memuat task.", "error");
    return;
  }

  totalLabel.textContent = `${tasks.length} task`;
  board.innerHTML = renderKanban(project, tasks, { manageable: true });

  const searchInput = container.querySelector("#task-search");
  searchInput?.addEventListener("input", () => filterBoard(board, searchInput.value));

  board.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-task-action]");
    if (!button) return;

    const task = tasks.find((item) => item.id === button.dataset.id);
    if (!task) return;

    const action = button.dataset.taskAction;
    if (action === "edit") {
      openTaskModal({ container, session, project, task });
    } else if (action === "delete") {
      await deleteTask(project, task, container, session, selectedId);
    } else if (action === "assign") {
      openAssignTaskModal({ project, task, container, session, selectedId });
    } else if (action === "approve") {
      await approveTask(project, task, container, session, selectedId);
    } else if (action === "feedback") {
      openFeedbackModal({ project, task, container, session, selectedId });
    }
  });
}

function renderKanban(project, tasks, { manageable = false, readOnly = false } = {}) {
  return `
    <div class="kanban">
      ${STATUS_COLUMNS.map((column) => {
        const items = tasks.filter((task) => task.status === column.key);
        return `
          <section class="kanban-col" data-status="${column.key}">
            <div class="kanban-head">
              <span class="col-dot"></span>
              <h3>${escapeHtml(column.label)}</h3>
              <span class="kanban-count">${items.length}</span>
            </div>
            ${items.length ? items.map((task) => taskCard(project, task, { manageable, readOnly })).join("") : `
              <div class="kanban-empty">${icon("layers", 18)}<br>Belum ada task</div>
            `}
          </section>
        `;
      }).join("")}
    </div>
  `;
}

function taskCard(project, task, { manageable = false, readOnly = false, feedback = null } = {}) {
  const member = findMember(project, task);
  const deadline = deadlineState(task.deadline);
  const canChange = task.status !== "APPROVED";
  const searchText = `${taskName(task)} ${member.name} ${member.email || ""} ${task.status} ${projectName(project)}`;
  const canRequestApproval = task.status === "ON_GOING" || task.status === "REJECTED";

  let actions = "";
  if (manageable) {
    actions = `
      ${task.status === "PENDING" ? `
        <button class="btn btn-primary btn-sm" type="button" data-task-action="approve" data-id="${escapeHtml(task.id)}">
          ${icon("check", 12)} Approve
        </button>
      ` : ""}
      ${canChange ? `
        <button class="btn btn-outline btn-sm" type="button" data-task-action="feedback" data-id="${escapeHtml(task.id)}">
          ${icon("message-square", 12)} Feedback
        </button>
        <button class="btn btn-ghost btn-sm" type="button" data-task-action="assign" data-id="${escapeHtml(task.id)}">
          ${icon("user-check", 12)} Assign
        </button>
      ` : ""}
    `;
  } else if (!readOnly && canRequestApproval) {
    actions = `
      <button class="btn btn-primary btn-sm" type="button" data-task-action="request-approval" data-id="${escapeHtml(task.id)}">
        ${icon("send", 12)} Ajukan approval
      </button>
    `;
  }

  return `
    <article class="task-card" data-task-card data-search-item="${escapeHtml(searchText)}">
      <div class="task-card-top">
        <h4>${escapeHtml(taskName(task))}</h4>
        ${manageable && canChange ? `
          <div class="flex items-center gap-1">
            <button class="task-menu-btn" type="button" data-task-action="edit" data-id="${escapeHtml(task.id)}" title="Edit task" aria-label="Edit task">
              ${icon("pencil", 13)}
            </button>
            <button class="task-menu-btn" type="button" data-task-action="delete" data-id="${escapeHtml(task.id)}" title="Hapus task" aria-label="Hapus task" style="color:var(--danger)">
              ${icon("trash", 13)}
            </button>
          </div>
        ` : ""}
      </div>

      ${feedback && isMeaningfulFeedback(feedback) ? `
        <div class="task-feedback">
          <strong>${escapeHtml(feedback.title || "Feedback")}</strong>
          ${escapeHtml(feedback.feedback || "")}
        </div>
      ` : ""}

      <div class="task-meta">
        <span class="task-assignee">
          ${avatarHtml(member, "sm")}
          <span>${escapeHtml(member.name || "Belum ditugaskan")}</span>
        </span>
        <span class="task-deadline ${deadline}">
          ${icon("clock", 11)} ${formatDate(task.deadline)}
        </span>
      </div>

      ${actions.trim() ? `<div class="task-actions">${actions}</div>` : ""}
    </article>
  `;
}

function filterBoard(board, term) {
  const query = term.trim().toLowerCase();
  board.querySelectorAll("[data-task-card]").forEach((card) => {
    const matches = !query || (card.dataset.searchItem || "").toLowerCase().includes(query);
    card.classList.toggle("hidden", !matches);
  });

  board.querySelectorAll(".kanban-col").forEach((column) => {
    const total = column.querySelectorAll("[data-task-card]").length;
    const visible = column.querySelectorAll("[data-task-card]:not(.hidden)").length;
    const empty = column.querySelector(".kanban-empty");
    if (empty) empty.classList.toggle("hidden", visible > 0);
    if (total && visible === 0 && query) {
      column.classList.add("is-filtered");
    } else {
      column.classList.remove("is-filtered");
    }
  });
}

function openTaskModal({ container, session, project, task = null }) {
  const editing = Boolean(task);
  const members = (project.assigned_to || []).filter((member) => member.role === "MEMBER");
  const selectedMember = task?.assignedTo || members[0]?.id || "";

  if (!members.length && !editing) {
    toast("Tambahkan minimal satu member ke project ini sebelum membuat task.", "warning");
    return;
  }

  const modal = openModal({
    title: editing ? "Edit task" : "Task baru",
    subtitle: `Project: ${projectName(project)}`,
    size: "modal-lg",
    content: `
      <form id="task-form" class="form-grid" novalidate>
        <div class="field span-2">
          <label for="task-name">Nama task</label>
          <input class="input" id="task-name" name="taskName" type="text" maxlength="160"
            placeholder="Contoh: Implementasi halaman laporan" value="${escapeHtml(task?.taskName || "")}" required>
        </div>
        <div class="field span-2">
          <label for="task-assignee">Ditugaskan kepada</label>
          <select class="select" id="task-assignee" name="assignedTo" required ${members.length ? "" : "disabled"}>
            ${members.length ? members.map((member) => `
              <option value="${escapeHtml(member.id)}" ${member.id === selectedMember ? "selected" : ""}>
                ${escapeHtml(member.name || member.email)} · ${escapeHtml(member.email || "")}
              </option>
            `).join("") : '<option value="">Belum ada member di project ini</option>'}
          </select>
          <span class="hint">Hanya user dengan role Member yang dapat menerima task.</span>
        </div>
        <div class="field">
          <label for="task-deadline">Deadline</label>
          <input class="input" id="task-deadline" name="deadline" type="datetime-local"
            value="${toDateTimeInput(task?.deadline)}" required>
        </div>
        <div class="field">
          <label>Status saat ini</label>
          <input class="input" type="text" value="${escapeHtml(task ? humanizeStatus(task.status) : "On going")}" disabled>
          <span class="hint">${editing ? "Menyimpan perubahan detail tidak mengubah status task." : "Task baru otomatis berstatus On going."}</span>
        </div>
        <div class="form-error span-2" id="task-form-error"></div>
      </form>
    `,
    footer: `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="task-form" id="task-submit">
        ${icon(editing ? "check" : "plus", 15)} ${editing ? "Simpan perubahan" : "Buat task"}
      </button>
    `,
  });

  const form = modal.body.querySelector("#task-form");
  const submitButton = modal.footer.querySelector("#task-submit");
  const errorBox = modal.body.querySelector("#task-form-error");

  modal.footer.querySelector("[data-cancel]").addEventListener("click", modal.close);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");

    const values = readForm(form);
    if (!values.taskName) {
      errorBox.textContent = "Nama task wajib diisi.";
      errorBox.classList.add("show");
      return;
    }
    if (!values.assignedTo) {
      errorBox.textContent = "Pilih anggota yang akan mengerjakan task ini.";
      errorBox.classList.add("show");
      return;
    }
    if (!values.deadline) {
      errorBox.textContent = "Deadline task wajib diisi.";
      errorBox.classList.add("show");
      return;
    }

    const payload = {
      taskName: values.taskName,
      assignedTo: values.assignedTo,
      deadline: values.deadline,
    };

    setButtonLoading(submitButton, true, editing ? "Menyimpan..." : "Membuat...");
    try {
      if (editing) {
        await api.updateTask(project.id, task.id, payload);
        toast("Task berhasil diperbarui.", "success");
      } else {
        await api.createTask(project.id, payload);
        toast("Task baru berhasil dibuat.", "success");
      }
      modal.close();
      await renderManagerTasks(container, session, { project: project.id });
    } catch (error) {
      errorBox.textContent = error.message || "Gagal menyimpan task.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

function openAssignTaskModal({ project, task, container, session, projectId }) {
  const members = (project.assigned_to || []).filter((member) => member.role === "MEMBER");
  if (!members.length) {
    toast("Belum ada member pada project ini.", "warning");
    return;
  }

  const modal = openModal({
    title: "Assign task",
    subtitle: taskName(task),
    content: `
      <form id="assign-task-form" novalidate>
        <div class="field">
          <label for="assign-task-member">Pilih anggota</label>
          <select class="select" id="assign-task-member" name="assignedTo">
            ${members.map((member) => `
              <option value="${escapeHtml(member.id)}" ${member.id === task.assignedTo ? "selected" : ""}>
                ${escapeHtml(member.name || member.email)} · ${escapeHtml(member.email || "")}
              </option>
            `).join("")}
          </select>
        </div>
        <div class="form-error" id="assign-task-error" style="margin-top:12px"></div>
      </form>
    `,
    footer: `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="assign-task-form" id="assign-task-submit">
        ${icon("user-check", 15)} Assign task
      </button>
    `,
  });

  const form = modal.body.querySelector("#assign-task-form");
  const submitButton = modal.footer.querySelector("#assign-task-submit");
  const errorBox = modal.body.querySelector("#assign-task-error");

  modal.footer.querySelector("[data-cancel]").addEventListener("click", modal.close);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");
    const userId = form.querySelector("#assign-task-member").value;

    setButtonLoading(submitButton, true, "Menugaskan...");
    try {
      await api.assignTask(project.id, task.id, userId);
      toast("Task berhasil ditugaskan.", "success");
      modal.close();
      await renderManagerTasks(container, session, { project: projectId });
    } catch (error) {
      errorBox.textContent = error.message || "Gagal menugaskan task.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

function openFeedbackModal({ project, task, container, session, projectId }) {
  const modal = openModal({
    title: "Beri feedback",
    subtitle: `${taskName(task)} · ${projectName(project)}`,
    size: "modal-lg",
    content: `
      <form id="feedback-form" class="form-grid" novalidate>
        <div class="field span-2">
          <label for="feedback-title">Judul feedback</label>
          <input class="input" id="feedback-title" name="title" type="text" maxlength="120"
            placeholder="Contoh: Perlu perbaikan validasi form" required>
        </div>
        <div class="field span-2">
          <label for="feedback-message">Catatan untuk anggota</label>
          <textarea class="textarea" id="feedback-message" name="feedback" rows="5"
            placeholder="Jelaskan bagian yang perlu diperbaiki..."></textarea>
        </div>
        <div class="form-error span-2" id="feedback-form-error"></div>
      </form>
    `,
    footer: `
      <button class="btn btn-outline" type="button" data-cancel>Batal</button>
      <button class="btn btn-primary" type="submit" form="feedback-form" id="feedback-submit">
        ${icon("send", 15)} Kirim feedback
      </button>
    `,
  });

  const form = modal.body.querySelector("#feedback-form");
  const submitButton = modal.footer.querySelector("#feedback-submit");
  const errorBox = modal.body.querySelector("#feedback-form-error");

  modal.footer.querySelector("[data-cancel]").addEventListener("click", modal.close);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.classList.remove("show");

    const values = readForm(form);
    if (!values.title) {
      errorBox.textContent = "Judul feedback wajib diisi.";
      errorBox.classList.add("show");
      return;
    }

    setButtonLoading(submitButton, true, "Mengirim...");
    try {
      await api.feedbackTask(project.id, task.id, values.title, values.feedback || "");
      toast("Feedback terkirim dan task dikembalikan untuk diperbaiki.", "success");
      modal.close();
      await renderManagerTasks(container, session, { project: projectId });
    } catch (error) {
      errorBox.textContent = error.message || "Gagal mengirim feedback.";
      errorBox.classList.add("show");
      setButtonLoading(submitButton, false);
    }
  });
}

async function approveTask(project, task, container, session, projectId) {
  const confirmed = await confirmDialog({
    title: "Setujui task?",
    message: `Task <strong>${escapeHtml(taskName(task))}</strong> akan ditandai sebagai <strong>Approved</strong> dan tidak dapat diubah lagi.`,
    confirmLabel: "Ya, setujui",
  });
  if (!confirmed) return;

  try {
    await api.approveTask(project.id, task.id);
    toast("Task berhasil disetujui.", "success");
    await renderManagerTasks(container, session, { project: projectId });
  } catch (error) {
    toast(error.message || "Gagal menyetujui task.", "error");
  }
}

async function deleteTask(project, task, container, session, projectId) {
  const confirmed = await confirmDialog({
    title: "Hapus task?",
    message: `Task <strong>${escapeHtml(taskName(task))}</strong> akan dihapus permanen dari project ini.`,
    confirmLabel: "Hapus task",
    danger: true,
  });
  if (!confirmed) return;

  try {
    await api.deleteTask(project.id, task.id);
    toast("Task berhasil dihapus.", "success");
    await renderManagerTasks(container, session, { project: projectId });
  } catch (error) {
    toast(error.message || "Gagal menghapus task.", "error");
  }
}

/* --------------------------------------------------------------------------
   Member task list
   -------------------------------------------------------------------------- */

async function renderMemberTasks(container, session, params = {}) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "My work",
      title: "Task saya",
      description: "Kerjakan task yang ditugaskan kepada Anda lalu ajukan approval.",
    })}
    ${skeletonRows(5)}
  `;

  let tasks = [];
  let pending = [];
  let feedbackItems = [];
  let projects = [];

  try {
    const [taskResponse, pendingResponse, feedbackResponse, projectResponse] = await Promise.all([
      api.getUserTasks(session.id),
      api.getUserPendingTasks(session.id),
      api.getUserFeedback(session.id),
      api.getProjectsByUser(session.id).catch(() => null),
    ]);

    tasks = unwrap(taskResponse) || [];
    pending = unwrap(pendingResponse) || [];
    feedbackItems = unwrap(feedbackResponse) || [];
    projects = unwrap(projectResponse) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "My work", title: "Task saya", description: "Task yang ditugaskan kepada Anda." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat task",
        description: error.message || "Tidak dapat mengambil daftar task Anda.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderMemberTasks(container, session, params));
    toast(error.message || "Gagal memuat task.", "error");
    return;
  }

  const projectsById = new Map(projects.map((project) => [project.id, project]));
  const feedbackByTask = new Map(
    feedbackItems.map((item) => [item.taskId, item])
  );

  const activeTab = ["all", "pending", "feedback"].includes(params.tab) ? params.tab : "all";
  const approved = tasks.filter((task) => task.status === "APPROVED").length;
  const ongoing = tasks.filter((task) => task.status === "ON_GOING").length;
  const rejected = tasks.filter((task) => task.status === "REJECTED").length;
  const withFeedback = feedbackItems.filter(isMeaningfulFeedback);

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "My work",
      title: "Task saya",
      description: "Kerjakan task yang ditugaskan kepada Anda lalu ajukan approval.",
      actions: `<button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>`,
    })}

    <div class="stat-grid">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5">
        <div class="stat-top">
          <span class="stat-icon">${icon("clipboard-list", 18)}</span>
          <span class="stat-trend">${icon("activity", 12)} ${ongoing} berjalan</span>
        </div>
        <div class="stat-value">${tasks.length}</div>
        <div class="stat-label">Total task</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(217,119,6,.1);--stat-color:#d97706">
        <div class="stat-top">
          <span class="stat-icon">${icon("clock", 18)}</span>
          <span class="stat-trend">${icon("send", 12)} menunggu</span>
        </div>
        <div class="stat-value">${pending.length}</div>
        <div class="stat-label">Menunggu approval</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(22,163,74,.1);--stat-color:#16a34a">
        <div class="stat-top">
          <span class="stat-icon">${icon("check-circle", 18)}</span>
          <span class="stat-trend">${tasks.length ? Math.round((approved / tasks.length) * 100) : 0}%</span>
        </div>
        <div class="stat-value">${approved}</div>
        <div class="stat-label">Approved</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(220,38,38,.1);--stat-color:#dc2626">
        <div class="stat-top">
          <span class="stat-icon">${icon("message-square", 18)}</span>
          <span class="stat-trend" style="${rejected ? "color:var(--danger)" : ""}">${rejected} rejected</span>
        </div>
        <div class="stat-value">${withFeedback.length}</div>
        <div class="stat-label">Task dengan feedback</div>
      </article>
    </div>

    <div class="toolbar">
      <div class="tabs" role="tablist" aria-label="Filter task">
        <button class="tab ${activeTab === "all" ? "active" : ""}" type="button" data-tab="all">Semua task</button>
        <button class="tab ${activeTab === "pending" ? "active" : ""}" type="button" data-tab="pending">Menunggu approval</button>
        <button class="tab ${activeTab === "feedback" ? "active" : ""}" type="button" data-tab="feedback">Feedback</button>
      </div>
      <span class="spacer"></span>
      <div class="search-box">
        ${icon("search", 15)}
        <input type="search" id="member-task-search" placeholder="Cari task..." aria-label="Cari task saya">
      </div>
    </div>

    <div id="member-task-content"></div>
  `;

  const content = container.querySelector("#member-task-content");
  const searchInput = container.querySelector("#member-task-search");

  container.querySelector("[data-refresh]")?.addEventListener("click", () => renderMemberTasks(container, session, { tab: activeTab }));
  container.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => renderMemberTasks(container, session, { tab: button.dataset.tab }));
  });

  function paint() {
    if (activeTab === "pending") {
      content.innerHTML = pending.length ? `
        <div class="grid grid-3">
          ${pending.map((task) => memberTaskCard(task, {
            project: projectsById.get(task.projectId),
            feedback: feedbackByTask.get(task.id),
            showApprovalButton: false,
          })).join("")}
        </div>
      ` : emptyState({
        iconName: "clock",
        title: "Tidak ada task menunggu approval",
        description: "Ajukan approval dari task yang sedang dikerjakan untuk meminta review project manager.",
      });
      return;
    }

    if (activeTab === "feedback") {
      content.innerHTML = withFeedback.length ? `
        <div class="grid grid-2">
          ${withFeedback.map((item) => {
            const task = tasks.find((candidate) => candidate.id === item.taskId);
            return `
              <article class="card" data-search-item="${escapeHtml(`${item.title} ${item.feedback} ${task ? taskName(task) : ""}`)}">
                <div class="card-head">
                  <div>
                    <h3>${escapeHtml(task ? taskName(task) : "Task")}</h3>
                    <p>${escapeHtml(projectsById.get(task?.projectId) ? projectName(projectsById.get(task.projectId)) : "Feedback project manager")}</p>
                  </div>
                  <span class="badge badge-rejected">Rejected</span>
                </div>
                <div class="card-body">
                  <div class="task-feedback" style="margin-top:0">
                    <strong>${escapeHtml(item.title || "Feedback")}</strong>
                    ${escapeHtml(item.feedback || "")}
                  </div>
                  <div class="notif-meta" style="margin-top:12px">
                    ${icon("clock", 12)} ${formatRelative(item.createdAt || item.updatedAt)}
                  </div>
                  ${task && (task.status === "ON_GOING" || task.status === "REJECTED") ? `
                    <div style="margin-top:12px">
                      <button class="btn btn-primary btn-sm" type="button" data-task-action="request-approval" data-id="${escapeHtml(task.id)}">
                        ${icon("send", 13)} Ajukan ulang approval
                      </button>
                    </div>
                  ` : ""}
                </div>
              </article>
            `;
          }).join("")}
        </div>
      ` : emptyState({
        iconName: "message-square",
        title: "Belum ada feedback",
        description: "Feedback dari project manager akan muncul di sini ketika task perlu diperbaiki.",
      });
      return;
    }

    content.innerHTML = tasks.length ? `
      <div class="grid grid-3">
        ${tasks.map((task) => memberTaskCard(task, {
          project: projectsById.get(task.projectId),
          feedback: feedbackByTask.get(task.id),
          showApprovalButton: true,
        })).join("")}
      </div>
    ` : emptyState({
      iconName: "clipboard-list",
      title: "Belum ada task",
      description: "Task yang ditugaskan project manager akan muncul di halaman ini.",
      action: '<a class="btn btn-outline" href="#/projects">Lihat project saya</a>',
    });
  }

  paint();

  searchInput?.addEventListener("input", () => {
    const term = searchInput.value.trim().toLowerCase();
    content.querySelectorAll("[data-search-item]").forEach((element) => {
      element.classList.toggle("hidden", Boolean(term) && !(element.dataset.searchItem || "").toLowerCase().includes(term));
    });
  });

  content.addEventListener("click", async (event) => {
    const button = event.target.closest('[data-task-action="request-approval"]');
    if (!button) return;

    const task = tasks.find((item) => item.id === button.dataset.id);
    if (!task) return;

    const confirmed = await confirmDialog({
      title: "Ajukan approval?",
      message: `Task <strong>${escapeHtml(taskName(task))}</strong> akan dikirim ke project manager untuk direview.`,
      confirmLabel: "Ya, ajukan",
    });
    if (!confirmed) return;

    setButtonLoading(button, true, "Mengirim...");
    try {
      await api.requestTaskApproval(task.id);
      toast("Task berhasil diajukan untuk approval.", "success");
      window.dispatchEvent(new CustomEvent("taskvision:notifications-updated"));
      await renderMemberTasks(container, session, { tab: activeTab });
    } catch (error) {
      toast(error.message || "Gagal mengajukan approval.", "error");
      setButtonLoading(button, false);
    }
  });
}

function memberTaskCard(task, { project, feedback, showApprovalButton }) {
  const deadline = deadlineState(task.deadline);
  const searchText = `${taskName(task)} ${project ? projectName(project) : ""} ${task.status} ${feedback?.title || ""} ${feedback?.feedback || ""}`;

  return `
    <article class="task-card" data-search-item="${escapeHtml(searchText)}" style="margin-bottom:0">
      <div class="task-card-top">
        <h4>${escapeHtml(taskName(task))}</h4>
        ${statusBadge(task.status)}
      </div>

      ${isMeaningfulFeedback(feedback) ? `
        <div class="task-feedback">
          <strong>${escapeHtml(feedback.title || "Feedback")}</strong>
          ${escapeHtml(feedback.feedback || "")}
        </div>
      ` : ""}

      <p class="text-muted" style="margin-top:9px;font-size:10.5px">
        ${icon("folder", 12)} ${escapeHtml(project ? projectName(project) : "Project")}
      </p>

      <div class="task-meta">
        <span class="task-deadline ${deadline}">${icon("calendar", 11)} ${formatDate(task.deadline)}</span>
        <span class="task-deadline ${deadline}">${deadlineText(task.deadline)}</span>
      </div>

      ${showApprovalButton && (task.status === "ON_GOING" || task.status === "REJECTED") ? `
        <div class="task-actions">
          <button class="btn btn-primary btn-sm" type="button" data-task-action="request-approval" data-id="${escapeHtml(task.id)}">
            ${icon("send", 12)} Ajukan approval
          </button>
        </div>
      ` : ""}
    </article>
  `;
}
