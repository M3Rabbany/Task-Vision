import { api, unwrap } from "../api.js";
import {
  avatarHtml,
  downloadBlob,
  emptyState,
  escapeHtml,
  formatDate,
  formatDateTime,
  formatKpi,
  icon,
  pageHeader,
  roleBadge,
  setButtonLoading,
  skeletonRows,
  statusBadge,
  toast,
} from "../ui.js";

function projectName(project) {
  return project?.projectName || `Project ${String(project?.id || "").slice(0, 6)}`;
}

function formatFileName(name) {
  return String(name || "task-vision-report")
    .replace(/[\\/:*?"<>|]+/g, "-")
    .replace(/\s+/g, "-")
    .slice(0, 80);
}

export async function renderReports(container, session, params = {}) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Analytics",
      title: "Reports",
      description: "Rangkum progres project dan unduh laporan dalam format Excel.",
    })}
    ${skeletonRows(5)}
  `;

  let projects = [];
  try {
    projects = unwrap(await api.getProjectsByUser(session.id)) || [];
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Analytics", title: "Reports", description: "Laporan progres project." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat project",
        description: error.message || "Tidak dapat mengambil daftar project.",
        action: '<button class="btn btn-primary" type="button" data-retry>Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderReports(container, session, params));
    toast(error.message || "Gagal memuat project.", "error");
    return;
  }

  if (!projects.length) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Analytics", title: "Reports", description: "Laporan progres project." })}
      ${emptyState({
        iconName: "file-spreadsheet",
        title: "Belum ada project",
        description: "Laporan dapat dibuat setelah Anda ditugaskan ke sebuah project.",
        action: '<a class="btn btn-primary" href="#/projects">Lihat project</a>',
      })}
    `;
    return;
  }

  const selectedId = params.project && projects.some((project) => project.id === params.project)
    ? params.project
    : projects[0].id;
  const selectedProject = projects.find((project) => project.id === selectedId);

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Analytics",
      title: "Reports",
      description: "Rangkum progres project dan unduh laporan dalam format Excel.",
      actions: `
        <button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>
        <button class="btn btn-primary" type="button" data-download>
          ${icon("download", 15)} Unduh Excel
        </button>
      `,
    })}

    <div class="toolbar">
      <label class="text-muted" style="font-size:11.5px" for="report-project-select">Project</label>
      <select class="select" id="report-project-select" style="min-width:240px" aria-label="Pilih project">
        ${projects.map((project) => `
          <option value="${escapeHtml(project.id)}" ${project.id === selectedId ? "selected" : ""}>
            ${escapeHtml(projectName(project))}
          </option>
        `).join("")}
      </select>
      <span class="spacer"></span>
      <div class="search-box">
        ${icon("search", 15)}
        <input type="search" id="report-search" placeholder="Cari task, anggota, aktivitas..." aria-label="Cari dalam laporan">
      </div>
    </div>

    <div id="report-content">${skeletonRows(5)}</div>
  `;

  const select = container.querySelector("#report-project-select");
  const downloadButton = container.querySelector("[data-download]");
  const content = container.querySelector("#report-content");
  const searchInput = container.querySelector("#report-search");

  select?.addEventListener("change", () => {
    window.location.hash = `#/reports?project=${encodeURIComponent(select.value)}`;
  });

  container.querySelector("[data-refresh]")?.addEventListener("click", () => {
    renderReports(container, session, { project: selectedId });
  });

  downloadButton?.addEventListener("click", async () => {
    setButtonLoading(downloadButton, true, "Menyiapkan file...");
    try {
      const file = await api.downloadReport(selectedId);
      downloadBlob(file.blob, file.filename || `report-${formatFileName(projectName(selectedProject))}.xlsx`);
      toast("Laporan Excel berhasil diunduh.", "success");
    } catch (error) {
      toast(error.message || "Gagal mengunduh laporan.", "error");
    } finally {
      setButtonLoading(downloadButton, false);
    }
  });

  let report;
  try {
    report = unwrap(await api.createReport(selectedId));
  } catch (error) {
    content.innerHTML = emptyState({
      iconName: "alert-triangle",
      title: "Gagal membuat laporan",
      description: error.message || "Tidak dapat mengambil data laporan project ini.",
      action: '<button class="btn btn-outline" type="button" data-retry>Coba lagi</button>',
    });
    content.querySelector("[data-retry]")?.addEventListener("click", () => {
      renderReports(container, session, { project: selectedId });
    });
    toast(error.message || "Gagal membuat laporan.", "error");
    return;
  }

  const tasks = Array.isArray(report?.tasks) ? report.tasks : [];
  const members = Array.isArray(report?.members) ? report.members : [];
  const activities = Array.isArray(report?.activities) ? report.activities : [];
  const feedbackCount = tasks.reduce((total, task) => total + (task.feedbacks?.length || 0), 0);
  const approved = tasks.filter((task) => task.status === "APPROVED").length;
  const progress = Math.max(0, Math.min(100, Number(report?.progress) || 0));

  content.innerHTML = `
    <section class="report-hero">
      <div style="min-width:0">
        <span class="eyebrow">${icon("file-spreadsheet", 12)} Project report</span>
        <h2>${escapeHtml(report?.project_name || projectName(selectedProject))}</h2>
        <p>${escapeHtml(report?.description || "Belum ada deskripsi project.")}</p>
        <div class="flex items-center gap-2" style="margin-top:12px;flex-wrap:wrap">
          ${statusBadge(report?.status || selectedProject.status)}
          <span class="deadline-chip">${icon("calendar", 12)} ${formatDate(selectedProject.deadline)}</span>
        </div>
      </div>
      <div class="report-progress">
        <div class="progress-label">
          <span>Progress project</span>
          <strong>${progress}%</strong>
        </div>
        <div class="progress" style="height:9px">
          <div class="progress-bar ${progress >= 100 ? "success" : progress >= 50 ? "" : "warning"}" style="width:${progress}%"></div>
        </div>
        <p class="text-muted" style="margin-top:8px;font-size:10.5px">
          ${approved} dari ${tasks.length} task sudah approved.
        </p>
      </div>
    </section>

    <div class="stat-grid" style="margin-top:18px;grid-template-columns:repeat(4,minmax(0,1fr))">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5">
        <div class="stat-top">
          <span class="stat-icon">${icon("clipboard-list", 18)}</span>
          <span class="stat-trend">${icon("check-circle", 12)} ${approved} selesai</span>
        </div>
        <div class="stat-value">${tasks.length}</div>
        <div class="stat-label">Total task</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(2,132,199,.1);--stat-color:#0284c7">
        <div class="stat-top">
          <span class="stat-icon">${icon("users", 18)}</span>
          <span class="stat-trend">${icon("briefcase", 12)} tim</span>
        </div>
        <div class="stat-value">${members.length}</div>
        <div class="stat-label">Anggota project</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(217,119,6,.1);--stat-color:#d97706">
        <div class="stat-top">
          <span class="stat-icon">${icon("message-square", 18)}</span>
          <span class="stat-trend">${icon("send", 12)} masukan</span>
        </div>
        <div class="stat-value">${feedbackCount}</div>
        <div class="stat-label">Feedback task</div>
      </article>
      <article class="stat-card" style="--stat-soft:rgba(22,163,74,.1);--stat-color:#16a34a">
        <div class="stat-top">
          <span class="stat-icon">${icon("activity", 18)}</span>
          <span class="stat-trend">${icon("clock", 12)} riwayat</span>
        </div>
        <div class="stat-value">${activities.length}</div>
        <div class="stat-label">Aktivitas tercatat</div>
      </article>
    </div>

    <div class="report-grid">
      <section class="card">
        <div class="card-head">
          <div>
            <h3>Detail task</h3>
            <p>Status pengerjaan beserta feedback terakhir.</p>
          </div>
          <span class="badge badge-neutral">${tasks.length} task</span>
        </div>
        <div class="card-body">
          ${tasks.length ? `
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr><th>Task</th><th>Status</th><th>Deadline</th><th>PIC</th><th>Feedback</th></tr>
                </thead>
                <tbody>
                  ${tasks.map((task) => {
                    const feedback = task.feedbacks?.[0];
                    return `
                      <tr data-search-item="${escapeHtml(`${task.task_name} ${task.status} ${task.assigned_to} ${feedback?.title || ""}`)}">
                        <td><span class="cell-title">${escapeHtml(task.task_name || "Task")}</span></td>
                        <td>${statusBadge(task.status)}</td>
                        <td><span class="text-muted" style="font-size:10.5px">${formatDate(task.deadline)}</span></td>
                        <td><span class="text-muted" style="font-size:10.5px">${escapeHtml(task.assigned_to || "—")}</span></td>
                        <td>
                          ${feedback ? `
                            <span class="badge badge-warning" title="${escapeHtml(feedback.feedback || "")}">
                              ${escapeHtml(feedback.title || "Feedback")}
                            </span>
                          ` : '<span class="text-muted" style="font-size:10.5px">—</span>'}
                        </td>
                      </tr>
                    `;
                  }).join("")}
                </tbody>
              </table>
            </div>
          ` : emptyState({
            iconName: "clipboard-list",
            title: "Belum ada task",
            description: "Tambahkan task pada board project untuk melihat detailnya di laporan.",
          })}
        </div>
      </section>

      <section class="card">
        <div class="card-head">
          <div>
            <h3>Anggota tim</h3>
            <p>Anggota yang terlibat dalam project ini.</p>
          </div>
        </div>
        <div class="card-body">
          ${members.length ? members.map((member) => `
            <div class="kpi-row" data-search-item="${escapeHtml(`${member.name} ${member.email} ${member.role}`)}">
              ${avatarHtml({ name: member.name, email: member.email, id: member.email }, "sm")}
              <div style="min-width:0">
                <div class="kpi-name">${escapeHtml(member.name || "Member")}</div>
                <div class="kpi-email">${escapeHtml(member.email || "")}</div>
                <div style="margin-top:5px">${roleBadge(member.role)}</div>
              </div>
              <div class="kpi-value">${formatKpi(member.kpi)}</div>
            </div>
          `).join("") : emptyState({
            iconName: "users",
            title: "Belum ada anggota",
            description: "Tambahkan anggota ke project untuk melihat data tim.",
          })}
        </div>
      </section>
    </div>

    <section class="card" style="margin-top:18px">
      <div class="card-head">
        <div>
          <h3>Aktivitas project</h3>
          <p>Jejak perubahan task dan project terbaru.</p>
        </div>
        <span class="badge badge-neutral">${activities.length} aktivitas</span>
      </div>
      <div class="card-body">
        ${activities.length ? `
          <div class="timeline">
            ${activities.slice(0, 12).map((activity) => `
              <div class="timeline-item" data-search-item="${escapeHtml(`${activity.entity} ${activity.action} ${activity.details}`)}">
                <span class="timeline-dot">${icon("activity", 13)}</span>
                <div>
                  <strong>${escapeHtml(activity.action || "Aktivitas")} · ${escapeHtml(activity.entity || "Project")}</strong>
                  <p>${escapeHtml(activity.details || "")}</p>
                  <time>${formatDateTime(activity.created_at || activity.createdAt)}</time>
                </div>
              </div>
            `).join("")}
          </div>
        ` : emptyState({
          iconName: "activity",
          title: "Belum ada aktivitas",
          description: "Aktivitas project akan tercatat setelah ada perubahan pada project atau task.",
        })}
      </div>
    </section>
  `;

  searchInput?.addEventListener("input", () => {
    const term = searchInput.value.trim().toLowerCase();
    content.querySelectorAll("[data-search-item]").forEach((element) => {
      element.classList.toggle("hidden", Boolean(term) && !(element.dataset.searchItem || "").toLowerCase().includes(term));
    });
  });
}
