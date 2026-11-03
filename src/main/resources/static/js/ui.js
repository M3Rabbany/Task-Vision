/* Shared UI helpers: icons, formatters, toasts, modals and small components. */

const iconPaths = {
  "layout-dashboard": '<rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/>',
  "folder": '<path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"/>',
  "folder-kanban": '<path d="M4 20h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"/><path d="M8 10v4"/><path d="M12 10v2"/><path d="M16 10v6"/>',
  "check-square": '<path d="m9 11 3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
  "clipboard-list": '<rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/>',
  "users": '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  "user": '<path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  "file-text": '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/>',
  "file-spreadsheet": '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M8 13h2"/><path d="M14 13h2"/><path d="M8 17h2"/><path d="M14 17h2"/>',
  "bell": '<path d="M10.268 21a2 2 0 0 0 3.464 0"/><path d="M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326"/>',
  "log-out": '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/>',
  "menu": '<line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="18" y2="18"/>',
  "search": '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  "sun": '<circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/>',
  "moon": '<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>',
  "chevron-down": '<path d="m6 9 6 6 6-6"/>',
  "chevron-right": '<path d="m9 18 6-6-6-6"/>',
  "plus": '<path d="M5 12h14"/><path d="M12 5v14"/>',
  "pencil": '<path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/>',
  "trash": '<path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/>',
  "x": '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
  "calendar": '<path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/>',
  "calendar-clock": '<path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/><path d="M12 14v3l2 1"/>',
  "clock": '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  "arrow-right": '<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>',
  "download": '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/>',
  "filter": '<polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>',
  "check": '<path d="M20 6 9 17l-5-5"/>',
  "check-circle": '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="m9 11 3 3L22 4"/>',
  "alert-triangle": '<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/>',
  "info": '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
  "mail": '<rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>',
  "lock": '<rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
  "key": '<circle cx="7.5" cy="15.5" r="5.5"/><path d="m21 2-9.6 9.6"/><path d="m15.5 7.5 3 3L22 7l-3-3"/>',
  "eye": '<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>',
  "eye-off": '<path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49"/><path d="M14.084 14.158a3 3 0 0 1-4.242-4.242"/><path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143"/><path d="m2 2 20 20"/>',
  "refresh": '<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/>',
  "rotate-ccw": '<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/>',
  "message-square": '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
  "briefcase": '<path d="M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/><rect width="20" height="14" x="2" y="6" rx="2"/>',
  "target": '<circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/>',
  "award": '<path d="m15.477 12.89 1.515 8.526a.5.5 0 0 1-.81.47l-3.58-2.687a1 1 0 0 0-1.197 0l-3.586 2.686a.5.5 0 0 1-.81-.469l1.514-8.526"/><circle cx="12" cy="8" r="6"/>',
  "trending-up": '<polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/>',
  "activity": '<path d="M22 12h-4l-3 9L9 3l-3 9H2"/>',
  "shield": '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/>',
  "copy": '<rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/>',
  "send": '<path d="M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z"/><path d="m21.854 2.147-10.94 10.939"/>',
  "external-link": '<path d="M15 3h6v6"/><path d="M10 14 21 3"/><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>',
  "sparkles": '<path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/><path d="M5 3v4"/><path d="M19 17v4"/><path d="M3 5h4"/><path d="M17 19h4"/>',
  "flag": '<path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" x2="4" y1="22" y2="15"/>',
  "layers": '<path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z"/><path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65"/><path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65"/>',
  "more-horizontal": '<circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>',
  "circle": '<circle cx="12" cy="12" r="10"/>',
  "circle-x": '<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>',
  "user-check": '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><polyline points="16 11 18 13 22 9"/>',
  "building": '<path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/>',
  "list-filter": '<path d="M3 6h18"/><path d="M7 12h10"/><path d="M10 18h4"/>',
  "arrow-up-right": '<path d="M7 7h10v10"/><path d="M7 17 17 7"/>',
};

export function icon(name, size = 18, extraClass = "") {
  const path = iconPaths[name] || iconPaths.circle;
  return `<svg class="${extraClass}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${path}</svg>`;
}

export const qs = (selector, root = document) => root.querySelector(selector);
export const qsa = (selector, root = document) => Array.from(root.querySelectorAll(selector));

export function escapeHtml(value) {
  if (value === null || value === undefined) return "";
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

export function parseDate(value) {
  if (!value) return null;
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
  const normalized = String(value).includes(" ")
    ? String(value).replace(" ", "T")
    : String(value);
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDate(value, fallback = "—") {
  const date = parseDate(value);
  if (!date) return fallback;
  return new Intl.DateTimeFormat("id-ID", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

export function formatDateTime(value, fallback = "—") {
  const date = parseDate(value);
  if (!date) return fallback;
  return new Intl.DateTimeFormat("id-ID", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export function formatRelative(value, fallback = "—") {
  const date = parseDate(value);
  if (!date) return fallback;
  const diff = Date.now() - date.getTime();
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return "Baru saja";
  if (diff < hour) return `${Math.floor(diff / minute)} menit lalu`;
  if (diff < day) return `${Math.floor(diff / hour)} jam lalu`;
  if (diff < 7 * day) return `${Math.floor(diff / day)} hari lalu`;
  return formatDate(date);
}

export function toDateTimeInput(value) {
  const date = parseDate(value);
  if (!date) return "";
  const pad = (number) => String(number).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function deadlineState(value) {
  const date = parseDate(value);
  if (!date) return "";
  const diff = date.getTime() - Date.now();
  if (diff < 0) return "late";
  if (diff < 3 * 24 * 60 * 60 * 1000) return "soon";
  return "";
}

export function daysUntil(value) {
  const date = parseDate(value);
  if (!date) return null;
  return Math.ceil((date.getTime() - Date.now()) / (24 * 60 * 60 * 1000));
}

export function deadlineText(value) {
  const days = daysUntil(value);
  if (days === null) return "No deadline";
  if (days < 0) return `${Math.abs(days)} hari terlambat`;
  if (days === 0) return "Jatuh tempo hari ini";
  if (days === 1) return "Jatuh tempo besok";
  return `${days} hari lagi`;
}

export function initials(name = "") {
  const parts = String(name).trim().split(/\s+/).filter(Boolean);
  if (!parts.length) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

const avatarTones = ["violet", "cyan", "emerald", "amber", "rose", "slate"];

export function avatarTone(seed = "") {
  const text = String(seed || "user");
  let sum = 0;
  for (let index = 0; index < text.length; index += 1) {
    sum += text.charCodeAt(index);
  }
  return avatarTones[sum % avatarTones.length];
}

export function avatarHtml(userOrName, size = "") {
  const user = typeof userOrName === "object" && userOrName !== null
    ? userOrName
    : { name: userOrName };
  const name = user.name || user.email || "User";
  const tone = avatarTone(user.id || user.email || name);
  const sizeClass = size ? ` ${size}` : "";
  return `<span class="avatar${sizeClass}" data-tone="${tone}" title="${escapeHtml(name)}">${escapeHtml(initials(name))}</span>`;
}

export function avatarStack(users = [], max = 4) {
  const list = Array.isArray(users) ? users : [];
  if (!list.length) {
    return `<span class="text-muted" style="font-size:10.5px">Belum ada anggota</span>`;
  }
  const visible = list.slice(0, max).map((user) => avatarHtml(user, "sm")).join("");
  const more = list.length > max
    ? `<span class="avatar-more">+${list.length - max}</span>`
    : "";
  return `<span class="avatar-stack">${visible}${more}</span>`;
}

export function humanizeStatus(status = "") {
  return String(status)
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export function statusBadge(status, label = null) {
  const normalized = String(status || "unknown").toLowerCase();
  return `<span class="badge badge-${normalized}"><span class="badge-dot"></span>${escapeHtml(label || humanizeStatus(status))}</span>`;
}

export function roleBadge(role) {
  const normalized = String(role || "").toLowerCase();
  const label = role === "PROJECT_MANAGER"
    ? "Project Manager"
    : role === "ADMIN"
      ? "Admin"
      : role === "MEMBER"
        ? "Member"
        : role;
  return `<span class="badge badge-role-${normalized}">${escapeHtml(label || "User")}</span>`;
}

export function formatKpi(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return "—";
  const number = Number(value);
  return `${Math.round((number <= 1 ? number * 100 : number))}%`;
}

export function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

export function debounce(fn, delay = 250) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}

/* --------------------------------------------------------------------------
   Toast
   -------------------------------------------------------------------------- */

export function toast(message, type = "info", title = "") {
  let root = qs("#toast-root");
  if (!root) {
    root = document.createElement("div");
    root.id = "toast-root";
    root.className = "toast-stack";
    document.body.appendChild(root);
  }

  const config = {
    success: { icon: "check-circle", title: "Berhasil" },
    error: { icon: "alert-triangle", title: "Terjadi kesalahan" },
    warning: { icon: "alert-triangle", title: "Perhatian" },
    info: { icon: "info", title: "Informasi" },
  }[type] || { icon: "info", title: "Informasi" };

  const element = document.createElement("div");
  element.className = `toast ${type}`;
  element.setAttribute("role", "status");
  element.innerHTML = `
    <span class="toast-icon">${icon(config.icon, 15)}</span>
    <div class="toast-body">
      <strong>${escapeHtml(title || config.title)}</strong>
      <p>${escapeHtml(message)}</p>
    </div>
    <button class="toast-close" type="button" aria-label="Tutup">${icon("x", 14)}</button>
  `;

  const remove = () => {
    if (!element.isConnected) return;
    element.classList.add("leaving");
    setTimeout(() => element.remove(), 220);
  };

  element.querySelector(".toast-close").addEventListener("click", remove);
  root.appendChild(element);
  const timer = setTimeout(remove, type === "error" ? 6500 : 4500);
  element.addEventListener("mouseenter", () => clearTimeout(timer), { once: true });
  return element;
}

/* --------------------------------------------------------------------------
   Modal
   -------------------------------------------------------------------------- */

let activeModal = null;

export function closeModal() {
  if (!activeModal) return;
  const modal = activeModal;
  activeModal = null;
  modal.backdrop.remove();
  document.body.style.overflow = "";
  document.removeEventListener("keydown", modal.onKeydown);
  if (typeof modal.onClose === "function") modal.onClose();
}

export function openModal({ title, subtitle = "", content = "", footer = "", size = "", onClose = null }) {
  closeModal();

  let root = qs("#modal-root");
  if (!root) {
    root = document.createElement("div");
    root.id = "modal-root";
    document.body.appendChild(root);
  }

  const backdrop = document.createElement("div");
  backdrop.className = "modal-backdrop";
  backdrop.innerHTML = `
    <div class="modal ${size}" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal-head">
        <div>
          <h3 id="modal-title">${escapeHtml(title || "")}</h3>
          ${subtitle ? `<p>${escapeHtml(subtitle)}</p>` : ""}
        </div>
        <button class="modal-close" type="button" data-modal-close aria-label="Tutup">${icon("x", 18)}</button>
      </div>
      <div class="modal-body">${content}</div>
      ${footer ? `<div class="modal-foot">${footer}</div>` : ""}
    </div>
  `;

  root.appendChild(backdrop);
  document.body.style.overflow = "hidden";

  const onKeydown = (event) => {
    if (event.key === "Escape") closeModal();
  };
  document.addEventListener("keydown", onKeydown);

  const modal = {
    backdrop,
    dialog: backdrop.querySelector(".modal"),
    body: backdrop.querySelector(".modal-body"),
    footer: backdrop.querySelector(".modal-foot"),
    onClose,
    onKeydown,
    close: closeModal,
  };
  activeModal = modal;

  backdrop.querySelector("[data-modal-close]").addEventListener("click", closeModal);
  backdrop.addEventListener("mousedown", (event) => {
    if (event.target === backdrop) closeModal();
  });

  const firstField = backdrop.querySelector("input, select, textarea, button:not(.modal-close)");
  if (firstField) {
    window.setTimeout(() => firstField.focus(), 60);
  }

  return modal;
}

export function confirmDialog({
  title = "Konfirmasi",
  message = "Apakah Anda yakin?",
  confirmLabel = "Ya, lanjutkan",
  cancelLabel = "Batal",
  danger = false,
} = {}) {
  return new Promise((resolve) => {
    let settled = false;
    const finish = (value) => {
      if (settled) return;
      settled = true;
      resolve(value);
    };

    const modal = openModal({
      title,
      size: "modal-sm",
      content: `<p class="confirm-text">${message}</p>`,
      footer: `
        <button type="button" class="btn btn-outline" data-cancel>${escapeHtml(cancelLabel)}</button>
        <button type="button" class="btn ${danger ? "btn-danger" : "btn-primary"}" data-confirm>${escapeHtml(confirmLabel)}</button>
      `,
      onClose: () => finish(false),
    });

    modal.footer.querySelector("[data-cancel]").addEventListener("click", () => {
      finish(false);
      modal.close();
    });
    modal.footer.querySelector("[data-confirm]").addEventListener("click", () => {
      // Resolve before closing: closeModal() runs onClose, which would
      // otherwise settle the promise with `false` and cancel the action.
      finish(true);
      modal.close();
    });
  });
}

/* --------------------------------------------------------------------------
   Loading helpers
   -------------------------------------------------------------------------- */

export function setButtonLoading(button, loading, loadingLabel = "Memproses...") {
  if (!button) return;
  if (loading) {
    if (!button.dataset.originalHtml) {
      button.dataset.originalHtml = button.innerHTML;
    }
    const lightSpinner = button.classList.contains("btn-primary")
      || button.classList.contains("btn-danger");
    button.disabled = true;
    button.innerHTML = `<span class="spinner${lightSpinner ? "" : " dark"}"></span><span>${escapeHtml(loadingLabel)}</span>`;
  } else {
    button.disabled = false;
    if (button.dataset.originalHtml) {
      button.innerHTML = button.dataset.originalHtml;
      delete button.dataset.originalHtml;
    }
  }
}

export function skeletonStats(count = 4) {
  return `<div class="stat-grid">${Array.from({ length: count })
    .map(() => '<div class="skeleton skeleton-stat"></div>')
    .join("")}</div>`;
}

export function skeletonCards(count = 6) {
  return `<div class="project-grid">${Array.from({ length: count })
    .map(() => '<div class="skeleton skeleton-card"></div>')
    .join("")}</div>`;
}

export function skeletonRows(count = 6) {
  return `<div class="loading-block">${Array.from({ length: count })
    .map(() => '<div class="skeleton skeleton-row"></div>')
    .join("")}</div>`;
}

export function emptyState({ iconName = "folder", title = "Belum ada data", description = "", action = "" }) {
  return `
    <div class="empty-state">
      <span class="empty-icon">${icon(iconName, 24)}</span>
      <h3>${escapeHtml(title)}</h3>
      ${description ? `<p>${escapeHtml(description)}</p>` : ""}
      ${action || ""}
    </div>
  `;
}

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename || "download";
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1500);
}

export function pageHeader({ eyebrow = "", title, description = "", actions = "" }) {
  return `
    <div class="page-head">
      <div>
        ${eyebrow ? `<span class="eyebrow">${icon("sparkles", 12)}${escapeHtml(eyebrow)}</span>` : ""}
        <h2>${escapeHtml(title)}</h2>
        ${description ? `<p>${escapeHtml(description)}</p>` : ""}
      </div>
      ${actions ? `<div class="page-actions">${actions}</div>` : ""}
    </div>
  `;
}

export function readForm(form) {
  const values = {};
  new FormData(form).forEach((value, key) => {
    values[key] = typeof value === "string" ? value.trim() : value;
  });
  return values;
}

export function applySearch(term = "") {
  const query = term.trim().toLowerCase();
  qsa("[data-search-item]").forEach((element) => {
    const haystack = (element.dataset.searchItem || element.textContent || "").toLowerCase();
    element.classList.toggle("hidden", Boolean(query) && !haystack.includes(query));
  });
  const visible = qsa("[data-search-item]:not(.hidden)").length;
  const empty = qs("[data-search-empty]");
  if (empty) empty.classList.toggle("hidden", visible > 0 || !query);
}
