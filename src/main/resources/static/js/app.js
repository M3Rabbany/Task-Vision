import { api, unwrap } from "./api.js";
import {
  avatarTone,
  escapeHtml,
  icon,
  initials,
  applySearch,
  emptyState,
} from "./ui.js";
import {
  clearSession,
  getTheme,
  homeRouteForRole,
  requireSession,
  roleLabel,
  setTheme,
} from "./auth.js";
import { renderDashboard } from "./views/dashboard.js";
import { renderProjects } from "./views/projects.js";
import { renderTasks } from "./views/tasks.js";
import { renderUsers } from "./views/users.js";
import { renderNotifications } from "./views/notifications.js";
import { renderReports } from "./views/reports.js";
import { renderProfile } from "./views/profile.js";

const ROUTES = {
  dashboard: {
    render: renderDashboard,
    roles: ["ADMIN"],
    title: "Dashboard",
    subtitle: "Ringkasan project, deadline, dan performa tim",
  },
  projects: {
    render: renderProjects,
    roles: ["ADMIN", "PROJECT_MANAGER", "MEMBER"],
    title: "Projects",
    subtitle: "Project, anggota tim, dan progres pekerjaan",
  },
  tasks: {
    render: renderTasks,
    roles: ["ADMIN", "PROJECT_MANAGER", "MEMBER"],
    title: "Task board",
    subtitle: "Pekerjaan tim, approval, dan feedback",
  },
  users: {
    render: renderUsers,
    roles: ["ADMIN"],
    title: "Users",
    subtitle: "Akun, role, dan indikator performa anggota",
  },
  reports: {
    render: renderReports,
    roles: ["PROJECT_MANAGER"],
    title: "Reports",
    subtitle: "Laporan project dan ekspor Excel",
  },
  notifications: {
    render: renderNotifications,
    roles: ["ADMIN", "PROJECT_MANAGER", "MEMBER"],
    title: "Notifications",
    subtitle: "Pembaruan task, approval, dan aktivitas project",
  },
  profile: {
    render: renderProfile,
    roles: ["ADMIN", "PROJECT_MANAGER", "MEMBER"],
    title: "Profil saya",
    subtitle: "Informasi akun dan hak akses",
  },
};

const NAV_ITEMS = {
  ADMIN: [
    { path: "dashboard", label: "Dashboard", iconName: "layout-dashboard" },
    { path: "projects", label: "Projects", iconName: "folder-kanban" },
    { path: "users", label: "Users", iconName: "users" },
    { path: "notifications", label: "Notifications", iconName: "bell", badge: true },
  ],
  PROJECT_MANAGER: [
    { path: "projects", label: "Projects", iconName: "folder-kanban" },
    { path: "tasks", label: "Task board", iconName: "check-square" },
    { path: "reports", label: "Reports", iconName: "file-spreadsheet" },
    { path: "notifications", label: "Notifications", iconName: "bell", badge: true },
  ],
  MEMBER: [
    { path: "tasks", label: "Task saya", iconName: "clipboard-list" },
    { path: "projects", label: "Projects", iconName: "folder-kanban" },
    { path: "notifications", label: "Notifications", iconName: "bell", badge: true },
  ],
};

const elements = {
  view: document.getElementById("view"),
  nav: document.getElementById("main-nav"),
  sidebar: document.getElementById("sidebar"),
  overlay: document.getElementById("sidebar-overlay"),
  menuToggle: document.getElementById("menu-toggle"),
  pageTitle: document.getElementById("page-title"),
  pageSubtitle: document.getElementById("page-subtitle"),
  search: document.getElementById("global-search"),
  themeToggle: document.getElementById("theme-toggle"),
  notificationButton: document.getElementById("notification-button"),
  notifBadge: document.getElementById("notif-badge"),
  profileTrigger: document.getElementById("profile-trigger"),
  profileMenu: document.getElementById("profile-menu"),
  logoutButton: document.getElementById("logout-button"),
  sidebarUser: document.getElementById("sidebar-user"),
  topbarAvatar: document.getElementById("topbar-avatar"),
  topbarName: document.getElementById("topbar-name"),
  topbarRole: document.getElementById("topbar-role"),
  menuName: document.getElementById("menu-name"),
  menuEmail: document.getElementById("menu-email"),
};

let activeSession = null;
let notificationRequest = 0;

function setAvatar(element, session, extraClass = "") {
  if (!element || !session) return;
  element.className = `avatar${extraClass ? ` ${extraClass}` : ""}`;
  element.dataset.tone = avatarTone(session.id || session.email || session.name);
  element.textContent = initials(session.name || session.email);
}

function paintShell(session) {
  const brand = document.getElementById("sidebar-brand");
  if (brand) brand.setAttribute("href", homeRouteForRole(session.role));

  setAvatar(elements.topbarAvatar, session);
  if (elements.topbarName) elements.topbarName.textContent = session.name || "User";
  if (elements.topbarRole) elements.topbarRole.textContent = roleLabel(session.role);
  if (elements.menuName) elements.menuName.textContent = session.name || "User";
  if (elements.menuEmail) elements.menuEmail.textContent = session.email || "—";

  if (elements.sidebarUser) {
    elements.sidebarUser.innerHTML = `
      <span class="avatar" data-tone="${avatarTone(session.id || session.email || session.name)}">${escapeHtml(initials(session.name || session.email))}</span>
      <div class="sidebar-user-meta">
        <strong>${escapeHtml(session.name || "User")}</strong>
        <span>${escapeHtml(roleLabel(session.role))}</span>
      </div>
    `;
  }
}

function paintNavigation(session) {
  if (!elements.nav) return;
  const items = NAV_ITEMS[session.role] || [];
  elements.nav.innerHTML = `
    <span class="nav-label">Menu utama</span>
    ${items.map((item) => `
      <a class="nav-link" href="#/${item.path}" data-nav-route="${item.path}">
        ${icon(item.iconName, 17)}
        <span>${escapeHtml(item.label)}</span>
        ${item.badge ? '<span class="nav-badge hidden" data-nav-badge>0</span>' : ""}
      </a>
    `).join("")}
    <span class="nav-label">Akun</span>
    <a class="nav-link" href="#/profile" data-nav-route="profile">
      ${icon("user", 17)}
      <span>Profil saya</span>
    </a>
  `;

  elements.nav.querySelectorAll(".nav-link").forEach((link) => {
    link.addEventListener("click", closeSidebar);
  });
}

function paintTheme() {
  if (!elements.themeToggle) return;
  const theme = document.documentElement.getAttribute("data-theme") || getTheme();
  const dark = theme === "dark";
  elements.themeToggle.innerHTML = icon(dark ? "sun" : "moon", 17);
  elements.themeToggle.title = dark ? "Aktifkan tema terang" : "Aktifkan tema gelap";
  elements.themeToggle.setAttribute("aria-label", elements.themeToggle.title);
}

function parseHash() {
  const raw = window.location.hash.startsWith("#") ? window.location.hash.slice(1) : "";
  const [pathPart, queryPart] = raw.split("?");
  const path = pathPart.replace(/^\/+|\/+$/g, "");
  const params = {};
  new URLSearchParams(queryPart || "").forEach((value, key) => {
    params[key] = value;
  });
  return { path, params };
}

function defaultPathFor(session) {
  return homeRouteForRole(session.role).replace(/^#\//, "");
}

function updateMeta(route, config) {
  if (elements.pageTitle) elements.pageTitle.textContent = config.title;
  if (elements.pageSubtitle) elements.pageSubtitle.textContent = config.subtitle;
  document.title = `${config.title} · Task Vision`;
  elements.nav?.querySelectorAll("[data-nav-route]").forEach((link) => {
    const active = link.dataset.navRoute === route;
    link.classList.toggle("active", active);
    if (active) {
      link.setAttribute("aria-current", "page");
    } else {
      link.removeAttribute("aria-current");
    }
  });
}

function closeSidebar() {
  elements.sidebar?.classList.remove("open");
  elements.overlay?.classList.remove("open");
}

function openSidebar() {
  elements.sidebar?.classList.add("open");
  elements.overlay?.classList.add("open");
}

function setProfileMenu(open) {
  if (!elements.profileMenu || !elements.profileTrigger) return;
  elements.profileMenu.classList.toggle("open", open);
  elements.profileTrigger.setAttribute("aria-expanded", String(open));
}

function showRouteError(container, error) {
  if (!container.isConnected) return;
  container.innerHTML = emptyState({
    iconName: "alert-triangle",
    title: "Halaman gagal dimuat",
    description: error?.message || "Terjadi kesalahan yang tidak terduga.",
    action: '<button class="btn btn-primary" type="button" data-route-retry>Muat ulang halaman</button>',
  });
  container.querySelector("[data-route-retry]")?.addEventListener("click", () => {
    window.dispatchEvent(new Event("hashchange"));
  });
}

async function renderRoute(session) {
  const active = requireSession();
  if (!active) return;
  activeSession = active;

  let { path, params } = parseHash();
  if (!path) {
    path = defaultPathFor(active);
    window.history.replaceState(null, "", `#/${path}`);
  }

  const config = ROUTES[path];
  if (!config || !config.roles.includes(active.role)) {
    const fallback = defaultPathFor(active);
    if (window.location.hash !== `#/${fallback}`) {
      window.location.hash = `#/${fallback}`;
      return;
    }
    path = fallback;
  }

  const route = ROUTES[path];
  updateMeta(path, route);
  closeSidebar();

  if (elements.search) elements.search.value = "";
  applySearch("");

  const wrapper = document.createElement("div");
  wrapper.className = "fade-in";
  wrapper.innerHTML = `
    <div class="loading-block" style="padding-top:10px">
      <div class="skeleton skeleton-stat" style="width:min(360px,100%)"></div>
      <div class="skeleton skeleton-card" style="width:100%;max-width:960px"></div>
    </div>
  `;
  elements.view.replaceChildren(wrapper);
  elements.view.focus({ preventScroll: true });
  window.scrollTo(0, 0);

  try {
    await route.render(wrapper, active, params);
  } catch (error) {
    showRouteError(wrapper, error);
    console.error("Route render failed", error);
  }
}

async function refreshNotificationBadge(session = activeSession) {
  if (!session?.id) return;
  const request = ++notificationRequest;
  let unread = 0;

  try {
    const notifications = unwrap(await api.getNotifications(session.id)) || [];
    if (request !== notificationRequest) return;
    unread = notifications.filter((notification) => {
      if (notification?.read !== undefined) return !notification.read;
      if (notification?.isRead !== undefined) return !notification.isRead;
      return false;
    }).length;
  } catch {
    if (request !== notificationRequest) return;
    unread = 0;
  }

  elements.notifBadge?.classList.toggle("hidden", unread === 0);
  if (elements.notifBadge) elements.notifBadge.textContent = unread > 9 ? "9+" : String(unread);

  document.querySelectorAll("[data-nav-badge]").forEach((badge) => {
    badge.classList.toggle("hidden", unread === 0);
    badge.textContent = unread > 9 ? "9+" : String(unread);
  });
}

function logout() {
  clearSession();
  window.location.replace("/login");
}

function bindShell(session) {
  elements.menuToggle?.addEventListener("click", () => {
    if (elements.sidebar?.classList.contains("open")) {
      closeSidebar();
    } else {
      openSidebar();
    }
  });
  elements.overlay?.addEventListener("click", closeSidebar);

  elements.themeToggle?.addEventListener("click", () => {
    const current = document.documentElement.getAttribute("data-theme") || getTheme();
    setTheme(current === "dark" ? "light" : "dark");
    paintTheme();
  });

  elements.search?.addEventListener("input", () => applySearch(elements.search.value));
  elements.search?.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      elements.search.value = "";
      applySearch("");
      elements.search.blur();
    }
  });

  elements.notificationButton?.addEventListener("click", () => {
    window.location.hash = "#/notifications";
  });

  elements.profileTrigger?.addEventListener("click", (event) => {
    event.stopPropagation();
    const open = !elements.profileMenu?.classList.contains("open");
    setProfileMenu(open);
  });

  elements.profileMenu?.querySelectorAll("a, button").forEach((item) => {
    item.addEventListener("click", () => setProfileMenu(false));
  });

  elements.logoutButton?.addEventListener("click", logout);

  document.addEventListener("click", (event) => {
    if (!elements.profileMenu?.classList.contains("open")) return;
    if (event.target.closest(".dropdown")) return;
    setProfileMenu(false);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") setProfileMenu(false);
  });

  window.addEventListener("resize", () => {
    if (window.innerWidth > 1024) closeSidebar();
  });

  window.addEventListener("hashchange", () => {
    const active = requireSession();
    if (!active) return;
    renderRoute(active);
  });

  window.addEventListener("taskvision:notifications-updated", () => {
    refreshNotificationBadge();
  });

  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") refreshNotificationBadge();
  });

  paintShell(session);
  paintNavigation(session);
  paintTheme();
  refreshNotificationBadge(session);
}

function bootstrap() {
  const session = requireSession();
  if (!session) return;
  activeSession = session;

  if (!window.location.hash) {
    window.history.replaceState(null, "", homeRouteForRole(session.role));
  }

  bindShell(session);
  renderRoute(session);
}

bootstrap();
