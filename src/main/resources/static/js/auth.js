const SESSION_KEY = "taskvision.session";
const THEME_KEY = "taskvision.theme";

/**
 * Read the JWT payload. The backend signs the token with the user id, role and
 * email, so this information can be used for UI decisions (never for security).
 */
export function decodeJwt(token) {
  if (!token || typeof token !== "string") return {};

  try {
    const part = token.split(".")[1];
    if (!part) return {};
    const normalized = part.replace(/-/g, "+").replace(/_/g, "/");
    const padding = "=".repeat((4 - (normalized.length % 4)) % 4);
    return JSON.parse(decodeURIComponent(
      atob(normalized + padding)
        .split("")
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, "0")}`)
        .join("")
    ));
  } catch {
    return {};
  }
}

export function isTokenExpired(token) {
  const claims = decodeJwt(token);
  if (!claims.exp) return false;
  return claims.exp * 1000 <= Date.now();
}

export function getToken() {
  const session = getSession();
  return session ? session.token : null;
}

export function getSession() {
  try {
    const raw = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;

    const session = JSON.parse(raw);
    if (!session || !session.token || isTokenExpired(session.token)) {
      localStorage.removeItem(SESSION_KEY);
      sessionStorage.removeItem(SESSION_KEY);
      return null;
    }

    return session;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    sessionStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function saveSession(loginData, remember = true) {
  const claims = decodeJwt(loginData.token);
  const email = loginData.email || claims.sub || "";
  const session = {
    id: loginData.id || claims.id || claims.userId || "",
    name: loginData.name || loginData.fullName || email.split("@")[0] || "User",
    email,
    role: loginData.role || claims.role || "MEMBER",
    token: loginData.token,
    signedInAt: Date.now(),
  };

  if (remember) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    sessionStorage.removeItem(SESSION_KEY);
  } else {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
    localStorage.removeItem(SESSION_KEY);
  }
  return session;
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  sessionStorage.removeItem(SESSION_KEY);
}

export function requireSession() {
  const session = getSession();
  if (!session) {
    redirectToLogin();
    return null;
  }
  return session;
}

export function redirectToLogin(expired = false) {
  const target = expired ? "/login?expired=1" : "/login";
  if (window.location.pathname !== "/login") {
    window.location.replace(target);
  }
}

export function homeRouteForRole(role) {
  switch (role) {
    case "ADMIN":
      return "#/dashboard";
    case "PROJECT_MANAGER":
      return "#/projects";
    case "MEMBER":
      return "#/tasks";
    default:
      return "#/projects";
  }
}

export function roleLabel(role) {
  switch (role) {
    case "ADMIN":
      return "Administrator";
    case "PROJECT_MANAGER":
      return "Project Manager";
    case "MEMBER":
      return "Team Member";
    default:
      return role || "User";
  }
}

export function getTheme() {
  return localStorage.getItem(THEME_KEY) || "light";
}

export function setTheme(theme) {
  localStorage.setItem(THEME_KEY, theme);
  document.documentElement.setAttribute("data-theme", theme);
  window.dispatchEvent(new CustomEvent("taskvision:theme", { detail: { theme } }));
}
