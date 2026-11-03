import {
  avatarHtml,
  emptyState,
  escapeHtml,
  formatDateTime,
  icon,
  pageHeader,
  roleBadge,
  toast,
} from "../ui.js";
import {
  clearSession,
  decodeJwt,
  getSession,
  homeRouteForRole,
  roleLabel,
} from "../auth.js";

const CAPABILITIES = {
  ADMIN: [
    { iconName: "users", title: "Kelola pengguna", text: "Buat, ubah, dan hapus akun serta role anggota workspace." },
    { iconName: "folder-kanban", title: "Kelola project", text: "Atur project, anggota tim, dan status pekerjaan." },
    { iconName: "layout-dashboard", title: "Pantau dashboard", text: "Lihat ringkasan project, deadline, dan KPI tim." },
  ],
  PROJECT_MANAGER: [
    { iconName: "check-square", title: "Kelola task", text: "Buat task, tugaskan ke member, dan atur deadline." },
    { iconName: "check-circle", title: "Review & approval", text: "Setujui hasil kerja atau kirim feedback untuk perbaikan." },
    { iconName: "file-spreadsheet", title: "Ekspor laporan", text: "Buat pratinjau dan unduh laporan project dalam format Excel." },
  ],
  MEMBER: [
    { iconName: "clipboard-list", title: "Kerjakan task", text: "Lihat task yang ditugaskan dan pantau tenggat waktunya." },
    { iconName: "send", title: "Ajukan approval", text: "Kirim task ke project manager ketika pekerjaan sudah selesai." },
    { iconName: "message-square", title: "Tindak lanjuti feedback", text: "Perbaiki task berdasarkan masukan dari project manager." },
  ],
};

function shortId(value) {
  const text = String(value || "");
  if (!text) return "—";
  return text.length > 16 ? `${text.slice(0, 8)}…${text.slice(-4)}` : text;
}

async function copyText(text) {
  if (!text) return false;
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    // Fall through to the legacy copy method.
  }

  try {
    const area = document.createElement("textarea");
    area.value = text;
    area.setAttribute("readonly", "");
    area.style.position = "fixed";
    area.style.opacity = "0";
    document.body.appendChild(area);
    area.select();
    const copied = document.execCommand("copy");
    area.remove();
    return copied;
  } catch {
    return false;
  }
}

export function renderProfile(container, session) {
  const current = getSession() || session;

  if (!current) {
    container.innerHTML = emptyState({
      iconName: "user",
      title: "Sesi tidak ditemukan",
      description: "Silakan masuk kembali untuk membuka profil Anda.",
      action: '<a class="btn btn-primary" href="/login">Masuk</a>',
    });
    return;
  }

  const claims = decodeJwt(current.token);
  const capabilities = CAPABILITIES[current.role] || [];
  const home = homeRouteForRole(current.role);
  const expiresAt = claims.exp ? new Date(claims.exp * 1000) : null;
  const issuedAt = claims.iat ? new Date(claims.iat * 1000) : null;

  container.innerHTML = `
    ${pageHeader({
      eyebrow: "Account",
      title: "Profil saya",
      description: "Informasi akun dan hak akses Anda di Task Vision.",
      actions: `
        <a class="btn btn-outline" href="${home}">${icon("arrow-right", 15)} Kembali ke workspace</a>
        <button class="btn btn-danger" type="button" data-logout>${icon("log-out", 15)} Keluar</button>
      `,
    })}

    <div class="profile-grid">
      <div>
        <section class="profile-hero">
          ${avatarHtml(current, "xl")}
          <h3>${escapeHtml(current.name || "User")}</h3>
          <p>${escapeHtml(current.email || "")}</p>
          ${roleBadge(current.role)}
          <div class="profile-hero-foot">
            <div>
              <strong>${escapeHtml(current.role === "PROJECT_MANAGER" ? "PM" : current.role === "ADMIN" ? "Admin" : "Member")}</strong>
              <span>Role</span>
            </div>
            <div>
              <strong>${expiresAt ? formatDateTime(expiresAt).split(",").pop().trim() : "—"}</strong>
              <span>Token berakhir</span>
            </div>
          </div>
        </section>

        <section class="card" style="margin-top:18px">
          <div class="card-head">
            <div>
              <h3>Akses cepat</h3>
              <p>Navigasi yang sering digunakan.</p>
            </div>
          </div>
          <div class="card-body" style="display:grid;gap:8px">
            <a class="btn btn-outline" href="${home}" style="justify-content:flex-start">
              ${icon("layout-dashboard", 15)} ${current.role === "ADMIN" ? "Dashboard" : current.role === "MEMBER" ? "Task saya" : "Projects"}
            </a>
            <a class="btn btn-outline" href="#/notifications" style="justify-content:flex-start">
              ${icon("bell", 15)} Notifikasi
            </a>
            <a class="btn btn-outline" href="/swagger-ui.html" target="_blank" rel="noreferrer" style="justify-content:flex-start">
              ${icon("external-link", 15)} Dokumentasi API
            </a>
          </div>
        </section>
      </div>

      <div style="display:grid;gap:18px;align-content:start">
        <section class="card">
          <div class="card-head">
            <div>
              <h3>Informasi akun</h3>
              <p>Data sesi yang sedang aktif di browser ini.</p>
            </div>
            <span class="badge badge-neutral">${icon("shield", 12)} Terautentikasi</span>
          </div>
          <div class="card-body">
            <dl class="detail-list">
              <div class="detail-row">
                <dt>${icon("user", 14)} Nama lengkap</dt>
                <dd>${escapeHtml(current.name || "—")}</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("mail", 14)} Email</dt>
                <dd>${escapeHtml(current.email || "—")}</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("shield", 14)} Role</dt>
                <dd>${escapeHtml(roleLabel(current.role))}</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("key", 14)} User ID</dt>
                <dd>
                  <span class="code-pill" title="${escapeHtml(current.id || "")}">${escapeHtml(shortId(current.id))}</span>
                  ${current.id ? `<button class="icon-btn" type="button" data-copy-id title="Salin user ID" aria-label="Salin user ID" style="margin-left:6px">${icon("copy", 13)}</button>` : ""}
                </dd>
              </div>
              <div class="detail-row">
                <dt>${icon("calendar-clock", 14)} Sesi dimulai</dt>
                <dd>${issuedAt ? formatDateTime(issuedAt) : current.signedInAt ? formatDateTime(new Date(current.signedInAt)) : "—"}</dd>
              </div>
              <div class="detail-row">
                <dt>${icon("clock", 14)} Token berakhir</dt>
                <dd>${expiresAt ? formatDateTime(expiresAt) : "—"}</dd>
              </div>
            </dl>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <div>
              <h3>Hak akses</h3>
              <p>Fitur yang tersedia untuk role ${escapeHtml(roleLabel(current.role))}.</p>
            </div>
          </div>
          <div class="card-body">
            <div class="timeline">
              ${capabilities.map((capability) => `
                <div class="timeline-item">
                  <span class="timeline-dot">${icon(capability.iconName, 13)}</span>
                  <div>
                    <strong>${escapeHtml(capability.title)}</strong>
                    <p>${escapeHtml(capability.text)}</p>
                  </div>
                </div>
              `).join("")}
            </div>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <div>
              <h3>Keamanan sesi</h3>
              <p>Token disimpan di browser dan dikirim pada setiap permintaan API.</p>
            </div>
          </div>
          <div class="card-body">
            <div class="task-feedback" style="margin-top:0;border-left-color:var(--info);background:var(--info-soft);color:var(--info)">
              <strong>${icon("info", 12)} Tips keamanan</strong>
              Selalu keluar setelah menggunakan perangkat bersama. Sesi berakhir otomatis setelah token kedaluwarsa.
            </div>
            <div style="margin-top:14px">
              <button class="btn btn-danger" type="button" data-logout>${icon("log-out", 15)} Keluar dari akun</button>
            </div>
          </div>
        </section>
      </div>
    </div>
  `;

  container.querySelectorAll("[data-logout]").forEach((button) => {
    button.addEventListener("click", () => {
      clearSession();
      window.location.replace("/login");
    });
  });

  container.querySelector("[data-copy-id]")?.addEventListener("click", async () => {
    const copied = await copyText(current.id);
    toast(
      copied ? "User ID berhasil disalin." : "Gagal menyalin user ID.",
      copied ? "success" : "error"
    );
  });
}
