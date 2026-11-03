import { api } from "./api.js";
import { setButtonLoading } from "./ui.js";

const form = document.getElementById("reset-form");
const alertBox = document.getElementById("reset-alert");
const submitButton = document.getElementById("reset-submit");
const tokenInput = document.getElementById("reset-token");

const tokenFromUrl = new URLSearchParams(window.location.search).get("token");
if (tokenFromUrl) {
  tokenInput.value = tokenFromUrl;
  document.getElementById("reset-password").focus();
}

function showAlert(type, message) {
  alertBox.className = `auth-alert show ${type}`;
  alertBox.textContent = message;
}

function hideAlert() {
  alertBox.className = "auth-alert";
  alertBox.textContent = "";
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideAlert();

  const token = tokenInput.value.trim();
  const password = document.getElementById("reset-password").value;
  const confirm = document.getElementById("reset-confirm").value;

  if (!token) {
    showAlert("error", "Token reset tidak ditemukan. Buka kembali tautan yang dikirim melalui email.");
    return;
  }
  if (!password || password.length < 6) {
    showAlert("error", "Password baru minimal 6 karakter.");
    return;
  }
  if (password !== confirm) {
    showAlert("error", "Password baru dan konfirmasi password tidak cocok.");
    return;
  }

  setButtonLoading(submitButton, true, "Menyimpan...");
  try {
    await api.resetPassword(token, password, confirm);
    showAlert("success", "Password berhasil diperbarui. Silakan masuk menggunakan password baru Anda.");
    form.reset();
    tokenInput.value = "";
    document.getElementById("login-link")?.classList.remove("hidden");
    setTimeout(() => window.location.replace("/login"), 2200);
  } catch (error) {
    showAlert("error", error.message || "Gagal memperbarui password. Token mungkin sudah kedaluwarsa.");
  } finally {
    setButtonLoading(submitButton, false);
  }
});
