import { api, unwrap } from "../api.js";
import {
  avatarHtml,
  avatarStack,
  deadlineState,
  deadlineText,
  escapeHtml,
  formatDate,
  formatKpi,
  icon,
  pageHeader,
  parseDate,
  skeletonStats,
  statusBadge,
  toast,
  emptyState,
} from "../ui.js";

function greeting() {
  const hour = new Date().getHours();
  if (hour < 11) return "Selamat pagi";
  if (hour < 15) return "Selamat siang";
  if (hour < 19) return "Selamat sore";
  return "Selamat malam";
}

function projectName(project) {
  return project.projectName || `Project ${String(project.id || "").slice(0, 6)}`;
}

export async function renderDashboard(container, session) {
  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Overview",
      title: "Dashboard",
      description: "Ringkasan project, deadline, dan performa tim Anda.",
      actions: `<button class="btn btn-outline" type="button" data-refresh>${icon("refresh", 15)} Muat ulang</button>`,
    })}
    ${skeletonStats(4)}
    <div class="grid grid-2" style="margin-top:18px">
      <div class="skeleton skeleton-card" style="height:320px"></div>
      <div class="skeleton skeleton-card" style="height:320px"></div>
    </div>
  `;

  container.querySelector("[data-refresh]").addEventListener("click", () => renderDashboard(container, session));

  let projects = [];
  let users = [];
  let kpiPage = { content: [] };
  let dashboardPage = { content: [] };

  try {
    const [projectsResponse, usersResponse, kpiResponse, dashboardResponse] = await Promise.all([
      api.getProjects(),
      api.getUsers(),
      api.getKpi({ page: 0, size: 100 }),
      api.getDashboardStatus({ page: 0, size: 6, sort: "createdAt", direction: "DESC" }),
    ]);

    projects = unwrap(projectsResponse) || [];
    users = usersResponse || [];
    kpiPage = unwrap(kpiResponse) || { content: [] };
    dashboardPage = unwrap(dashboardResponse) || { content: [] };
  } catch (error) {
    container.innerHTML = `
      ${pageHeader({ eyebrow: "Overview", title: "Dashboard", description: "Ringkasan project dan performa tim." })}
      ${emptyState({
        iconName: "alert-triangle",
        title: "Gagal memuat dashboard",
        description: error.message || "Terjadi kesalahan saat mengambil data.",
        action: '<button class="btn btn-primary" type="button" data-retry> Coba lagi</button>',
      })}
    `;
    container.querySelector("[data-retry]")?.addEventListener("click", () => renderDashboard(container, session));
    toast(error.message || "Gagal memuat dashboard.", "error");
    return;
  }

  const kpis = Array.isArray(kpiPage?.content) ? kpiPage.content : [];
  const members = users.filter((user) => user.role === "MEMBER");
  const activeProjects = projects.filter((project) => project.status === "IN_PROGRESS");
  const completedProjects = projects.filter((project) => project.status === "COMPLETED");
  const avgKpi = kpis.length
    ? kpis.reduce((total, item) => total + (Number(item.kpi) || 0), 0) / kpis.length
    : 0;

  const projectsById = new Map(projects.map((project) => [project.id, project]));
  const recentProjects = [...projects]
    .sort((a, b) => (parseDate(b.createdAt)?.getTime() || 0) - (parseDate(a.createdAt)?.getTime() || 0))
    .slice(0, 5);

  const upcoming = [...projects]
    .filter((project) => project.status !== "COMPLETED" && project.deadline)
    .sort((a, b) => (parseDate(a.deadline)?.getTime() || 0) - (parseDate(b.deadline)?.getTime() || 0))
    .slice(0, 5);

  const kpiTop = [...kpis].sort((a, b) => (Number(b.kpi) || 0) - (Number(a.kpi) || 0)).slice(0, 6);
  const dashboardItems = Array.isArray(dashboardPage?.content) ? dashboardPage.content : [];

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Overview",
      title: "Dashboard",
      description: "Ringkasan project, deadline, dan performa tim Anda.",
      actions: `
        <a class="btn btn-ghost" href="#/users">${icon("users", 15)} Kelola tim</a>
        <a class="btn btn-primary" href="#/projects">${icon("plus", 15)} Buat project</a>
      `,
    })}

    <section class="banner">
      <div class="banner-content">
        <h2>${greeting()}, ${escapeHtml(session.name || "Admin")} 👋</h2>
        <p>Ada ${activeProjects.length} project yang sedang berjalan dan ${upcoming.length} deadline terdekat yang perlu diperhatikan.</p>
        <span class="banner-date">${icon("calendar", 13)} ${new Intl.DateTimeFormat("id-ID", {
          weekday: "long",
          day: "numeric",
          month: "long",
          year: "numeric",
        }).format(new Date())}</span>
      </div>
      <div class="banner-actions">
        <a class="btn btn-outline" href="#/users">${icon("user-check", 15)} Anggota tim</a>
        <a class="btn btn-primary" href="#/projects">${icon("folder-kanban", 15)} Lihat project</a>
      </div>
    </section>

    <div class="stat-grid">
      <article class="stat-card" style="--stat-soft:rgba(79,70,229,.1);--stat-color:#4f46e5;--stat-glow:rgba(79,70,229,.07)">
        <div class="stat-top">
          <span class="stat-icon">${icon("folder-kanban", 18)}</span>
          <span class="stat-trend">${icon("trending-up", 12)} ${activeProjects.length} aktif</span>
        </div>
        <div class="stat-value">${projects.length}</div>
        <div class="stat-label">Total project</div>
        <div class="stat-sub">${completedProjects.length} project sudah selesai</div>
      </article>

      <article class="stat-card" style="--stat-soft:rgba(2,132,199,.1);--stat-color:#0284c7;--stat-glow:rgba(2,132,199,.07)">
        <div class="stat-top">
          <span class="stat-icon">${icon("users", 18)}</span>
          <span class="stat-trend">${icon("user-check", 12)} ${members.length} member</span>
        </div>
        <div class="stat-value">${users.length}</div>
        <div class="stat-label">Anggota workspace</div>
        <div class="stat-sub">${users.filter((user) => user.role === "PROJECT_MANAGER").length} project manager</div>
      </article>

      <article class="stat-card" style="--stat-soft:rgba(22,163,74,.1);--stat-color:#16a34a;--stat-glow:rgba(22,163,74,.07)">
        <div class="stat-top">
          <span class="stat-icon">${icon("target", 18)}</span>
          <span class="stat-trend">${icon("award", 12)} rata-rata</span>
        </div>
        <div class="stat-value">${formatKpi(avgKpi)}</div>
        <div class="stat-label">KPI tim</div>
        <div class="stat-sub">Dihitung dari task yang selesai tepat waktu</div>
      </article>

      <article class="stat-card" style="--stat-soft:rgba(217,119,6,.1);--stat-color:#d97706;--stat-glow:rgba(217,119,6,.08)">
        <div class="stat-top">
          <span class="stat-icon">${icon("calendar-clock", 18)}</span>
          <span class="stat-trend" style="color:var(--warning)">${icon("clock", 12)} terdekat</span>
        </div>
        <div class="stat-value">${upcoming.length}</div>
        <div class="stat-label">Deadline mendatang</div>
        <div class="stat-sub">${upcoming.filter((project) => deadlineState(project.deadline) === "late").length} project melewati deadline</div>
      </article>
    </div>

    <div class="grid grid-2" style="grid-template-columns:minmax(0,1.35fr) minmax(0,.65fr)">
      <section class="card">
        <div class="card-head">
          <div>
            <h3>Project terbaru</h3>
            <p>Project yang paling baru dibuat di workspace.</p>
          </div>
          <a class="btn btn-ghost btn-sm" href="#/projects">Semua project ${icon("arrow-right", 13)}</a>
        </div>
        <div class="card-body">
          ${recentProjects.length ? `
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr><th>Project</th><th>Status</th><th>Team</th><th>Deadline</th></tr>
                </thead>
                <tbody>
                  ${recentProjects.map((project) => `
                    <tr data-search-item="${escapeHtml(`${projectName(project)} ${project.description || ""} ${project.status || ""}`)}">
                      <td>
                        <div class="cell-main">
                          <span class="project-icon" style="width:34px;height:34px;border-radius:9px">${icon("folder", 16)}</span>
                          <span>
                            <span class="cell-title">${escapeHtml(projectName(project))}</span>
                            <span class="cell-sub">${escapeHtml(project.description || "Tanpa deskripsi")}</span>
                          </span>
                        </div>
                      </td>
                      <td>${statusBadge(project.status)}</td>
                      <td>${avatarStack(project.assigned_to || [], 3)}</td>
                      <td>
                        <span class="deadline-chip ${deadlineState(project.deadline)}">${icon("calendar", 12)} ${formatDate(project.deadline)}</span>
                      </td>
                    </tr>
                  `).join("")}
                </tbody>
              </table>
            </div>
          ` : emptyState({
            iconName: "folder-kanban",
            title: "Belum ada project",
            description: "Buat project pertama untuk mulai mengatur pekerjaan tim.",
            action: '<a class="btn btn-primary" href="#/projects">Buat project</a>',
          })}
        </div>
      </section>

      <section class="card">
        <div class="card-head">
          <div>
            <h3>KPI anggota tim</h3>
            <p>Performa task anggota berdasarkan approval.</p>
          </div>
          <a class="btn btn-ghost btn-sm" href="#/users">Detail</a>
        </div>
        <div class="card-body">
          ${kpiTop.length ? kpiTop.map((item, index) => {
            const value = Number(item.kpi) || 0;
            const percent = Math.round(value <= 1 ? value * 100 : value);
            return `
              <div class="kpi-row" data-search-item="${escapeHtml(`${item.name} ${item.email} ${percent}`)}">
                ${avatarHtml({ name: item.name, email: item.email, id: item.userId }, "sm")}
                <div style="min-width:0">
                  <div class="kpi-name">${escapeHtml(item.name || "Member")}</div>
                  <div class="kpi-email">${escapeHtml(item.email || "")}</div>
                  <div class="progress" style="margin-top:6px;height:4px">
                    <div class="progress-bar ${percent >= 80 ? "success" : percent >= 50 ? "" : "warning"}" style="width:${Math.min(percent, 100)}%"></div>
                  </div>
                </div>
                <div class="kpi-value">${percent}%</div>
              </div>
            `;
          }).join("") : emptyState({
            iconName: "target",
            title: "KPI belum tersedia",
            description: "KPI akan muncul setelah anggota tim memiliki task yang diproses.",
          })}
        </div>
      </section>
    </div>

    <div class="grid grid-2" style="margin-top:18px">
      <section class="card">
        <div class="card-head">
          <div>
            <h3>Deadline terdekat</h3>
            <p>Project aktif yang perlu diselesaikan lebih dulu.</p>
          </div>
        </div>
        <div class="card-body">
          ${upcoming.length ? `
            <div class="timeline">
              ${upcoming.map((project) => {
                const state = deadlineState(project.deadline);
                return `
                  <div class="timeline-item" data-search-item="${escapeHtml(`${projectName(project)} ${project.deadline}`)}">
                    <span class="timeline-dot" style="${state === "late" ? "background:var(--danger-soft);color:var(--danger)" : state === "soon" ? "background:var(--warning-soft);color:var(--warning)" : ""}">${icon("calendar", 13)}</span>
                    <div>
                      <strong>${escapeHtml(projectName(project))}</strong>
                      <p>${escapeHtml(project.description || "Tidak ada deskripsi")}</p>
                      <time class="${state === "late" ? "text-danger" : state === "soon" ? "text-warning" : ""}">${formatDate(project.deadline)} · ${deadlineText(project.deadline)}</time>
                    </div>
                  </div>
                `;
              }).join("")}
            </div>
          ` : emptyState({
            iconName: "calendar",
            title: "Tidak ada deadline terdekat",
            description: "Semua project aktif tampaknya masih memiliki waktu yang cukup.",
          })}
        </div>
      </section>

      <section class="card">
        <div class="card-head">
          <div>
            <h3>Status project</h3>
            <p>Perbandingan project yang berjalan dan selesai.</p>
          </div>
        </div>
        <div class="card-body">
          ${projects.length ? `
            <div style="display:grid;gap:18px;padding-top:4px">
              ${[
                { label: "In progress", value: activeProjects.length, className: "", color: "var(--info)" },
                { label: "Completed", value: completedProjects.length, className: "success", color: "var(--success)" },
              ].map((row) => {
                const percent = projects.length ? Math.round((row.value / projects.length) * 100) : 0;
                return `
                  <div>
                    <div class="progress-label">
                      <span>${row.label}</span>
                      <strong>${row.value} project · ${percent}%</strong>
                    </div>
                    <div class="progress" style="height:8px">
                      <div class="progress-bar ${row.className}" style="width:${percent}%"></div>
                    </div>
                  </div>
                `;
              }).join("")}
            </div>
            <div class="detail-list" style="margin-top:20px">
              <div class="detail-row">
                <dt>${icon("activity", 14)} Aktivitas dashboard</dt>
                <dd>${dashboardItems.length} data terbaru</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("users", 14)} Rata-rata anggota per project</dt>
                <dd>${projects.length ? (projects.reduce((total, project) => total + (project.assigned_to?.length || 0), 0) / projects.length).toFixed(1) : "0"} orang</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("target", 14)} Project selesai</dt>
                <dd>${completedProjects.length} dari ${projects.length}</dd>
              </div>
            </div>
          ` : emptyState({
            iconName: "activity",
            title: "Belum ada data status",
            description: "Status project akan muncul setelah project dibuat.",
          })}
        </div>
      </section>
    </div>
  `;

  // Keep the search field usable on this page too.
  const search = document.getElementById("global-search");
  if (search?.value) {
    container.querySelectorAll("[data-search-item]").forEach((element) => {
      const term = search.value.trim().toLowerCase();
      if (term) element.classList.toggle("hidden", !(element.dataset.searchItem || "").toLowerCase().includes(term));
    });
  }
}
