import { api, unwrap } from "./api.js";
import { getSession, saveSession } from "./auth.js";
import { setButtonLoading } from "./ui.js";

const loginView = document.getElementById("login-view");
const forgotView = document.getElementById("forgot-view");
const loginAlert = document.getElementById("login-alert");
const forgotAlert = document.getElementById("forgot-alert");
const loginForm = document.getElementById("login-form");
const forgotForm = document.getElementById("forgot-form");
const loginSubmit = document.getElementById("login-submit");
const forgotSubmit = document.getElementById("forgot-submit");
const passwordInput = document.getElementById("login-password");
const togglePassword = document.getElementById("toggle-password");

document.getElementById("auth-year").textContent = new Date().getFullYear();

/* If the visitor already has a valid session, go straight to the app. */
if (getSession()) {
  window.location.replace("/app");
}

function showAlert(element, type, message) {
  if (!element) return;
  element.className = `auth-alert show ${type}`;
  element.textContent = message;
}

function hideAlert(element) {
  if (!element) return;
  element.className = "auth-alert";
  element.textContent = "";
}

function setView(view) {
  const showForgot = view === "forgot";
  loginView.classList.toggle("hidden", showForgot);
  forgotView.classList.toggle("hidden", !showForgot);
  hideAlert(loginAlert);
  hideAlert(forgotAlert);

  if (showForgot) {
    document.title = "Lupa password · Task Vision";
    window.history.replaceState({}, "", "/forgot-password");
    document.getElementById("forgot-email").value = document.getElementById("login-email").value;
    setTimeout(() => document.getElementById("forgot-email").focus(), 50);
  } else {
    document.title = "Masuk · Task Vision";
    window.history.replaceState({}, "", "/login");
    setTimeout(() => document.getElementById("login-email").focus(), 50);
  }
}

/* Support direct navigation to /forgot-password. */
if (window.location.pathname.includes("forgot-password")) {
  setView("forgot");
} else {
  const expired = new URLSearchParams(window.location.search).get("expired");
  if (expired) {
    showAlert(loginAlert, "info", "Sesi Anda telah berakhir. Silakan masuk kembali untuk melanjutkan.");
  }
}

document.querySelectorAll("[data-show-forgot]").forEach((link) => {
  link.addEventListener("click", (event) => {
    event.preventDefault();
    setView("forgot");
  });
});

document.querySelectorAll("[data-show-login]").forEach((button) => {
  button.addEventListener("click", () => setView("login"));
});

togglePassword.addEventListener("click", () => {
  const visible = passwordInput.type === "text";
  passwordInput.type = visible ? "password" : "text";
  togglePassword.setAttribute("aria-label", visible ? "Tampilkan password" : "Sembunyikan password");
  togglePassword.innerHTML = visible
    ? `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/></svg>`
    : `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49"/><path d="M14.084 14.158a3 3 0 0 1-4.242-4.242"/><path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143"/><path d="m2 2 20 20"/></svg>`;
});

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideAlert(loginAlert);

  const email = document.getElementById("login-email").value.trim();
  const password = passwordInput.value;

  if (!email || !password) {
    showAlert(loginAlert, "error", "Email dan password wajib diisi.");
    return;
  }

  setButtonLoading(loginSubmit, true, "Memverifikasi...");
  try {
    const response = await api.login(email, password);
    const login = unwrap(response);
    if (!login || !login.token) {
      throw new Error("Respons login tidak valid.");
    }
    saveSession(login, document.getElementById("remember-me").checked);
    window.location.replace("/app");
  } catch (error) {
    showAlert(loginAlert, "error", error.message || "Gagal masuk. Silakan coba lagi.");
  } finally {
    setButtonLoading(loginSubmit, false);
  }
});

forgotForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideAlert(forgotAlert);

  const email = document.getElementById("forgot-email").value.trim();
  if (!email) {
    showAlert(forgotAlert, "error", "Email wajib diisi.");
    return;
  }

  setButtonLoading(forgotSubmit, true, "Mengirim...");
  try {
    await api.forgotPassword(email);
    showAlert(
      forgotAlert,
      "success",
      "Tautan reset password telah diproses. Silakan periksa kotak masuk email Anda."
    );
    forgotForm.reset();
  } catch (error) {
    showAlert(forgotAlert, "error", error.message || "Gagal mengirim tautan reset password.");
  } finally {
    setButtonLoading(forgotSubmit, false);
  }
});

document.getElementById("login-email").focus();
