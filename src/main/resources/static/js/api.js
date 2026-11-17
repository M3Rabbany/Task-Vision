import { clearSession, getToken } from "./auth.js";

export class ApiError extends Error {
  constructor(message, status = 0, payload = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

function buildQuery(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    search.append(key, value);
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

/**
 * Wrapper around fetch that injects the JWT and normalises API errors.
 * Responses from the backend follow CommonResponse: { statusCode, message, data, error }.
 */
async function request(path, options = {}, config = {}) {
  const { method = "GET", body, headers = {}, responseType = "json" } = options;
  const finalHeaders = { Accept: "application/json", ...headers };
  let payloadBody;

  if (body !== undefined && body !== null) {
    if (body instanceof FormData) {
      payloadBody = body;
    } else {
      finalHeaders["Content-Type"] = "application/json";
      payloadBody = JSON.stringify(body);
    }
  }

  const token = config.skipAuth ? null : getToken();
  if (token) {
    finalHeaders.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(path, {
      method,
      headers: finalHeaders,
      body: payloadBody,
      credentials: "same-origin",
    });
  } catch {
    throw new ApiError("Tidak dapat terhubung ke server. Periksa koneksi Anda.", 0);
  }

  if (response.status === 401 && !config.skipAuthRedirect) {
    clearSession();
    if (window.location.pathname !== "/login") {
      window.location.replace("/login?expired=1");
    }
    throw new ApiError("Sesi Anda telah berakhir. Silakan masuk kembali.", 401);
  }

  if (responseType === "blob") {
    if (!response.ok) {
      throw new ApiError(await extractErrorMessage(response), response.status);
    }
    return {
      blob: await response.blob(),
      filename: extractFilename(response.headers.get("Content-Disposition")),
      contentType: response.headers.get("Content-Type") || "",
    };
  }

  const raw = await response.text();
  let payload = null;
  if (raw) {
    const contentType = response.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
      try {
        payload = JSON.parse(raw);
      } catch {
        payload = raw;
      }
    } else {
      payload = raw;
    }
  }

  if (!response.ok) {
    const message = payload && typeof payload === "object"
      ? payload.error || payload.message || `Permintaan gagal (${response.status})`
      : payload || `Permintaan gagal (${response.status})`;
    throw new ApiError(message, response.status, payload);
  }

  return payload;
}

async function extractErrorMessage(response) {
  try {
    const text = await response.text();
    if (!text) return `Permintaan gagal (${response.status})`;
    const payload = JSON.parse(text);
    return payload.error || payload.message || `Permintaan gagal (${response.status})`;
  } catch {
    return `Permintaan gagal (${response.status})`;
  }
}

function extractFilename(contentDisposition) {
  if (!contentDisposition) return "task-vision-report.xlsx";
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match) return decodeURIComponent(utf8Match[1]);
  const match = contentDisposition.match(/filename="?([^";]+)"?/i);
  return match ? match[1] : "task-vision-report.xlsx";
}

/** Unwrap CommonResponse payloads while still supporting raw array responses. */
export function unwrap(response) {
  if (response && typeof response === "object" && !Array.isArray(response)
      && Object.prototype.hasOwnProperty.call(response, "data")) {
    return response.data;
  }
  return response;
}

export const api = {
  /* Auth ------------------------------------------------------------------ */
  login(email, password) {
    return request("/api/auth/login", {
      method: "POST",
      body: { email, password },
    }, { skipAuth: true, skipAuthRedirect: true });
  },

  forgotPassword(email) {
    return request("/api/auth/forgot-password", {
      method: "POST",
      body: { email },
    }, { skipAuth: true, skipAuthRedirect: true });
  },

  resetPassword(token, newPassword, confirmNewPassword) {
    return request("/api/auth/reset-password", {
      method: "POST",
      body: { token, newPassword, confirmNewPassword },
    }, { skipAuth: true, skipAuthRedirect: true });
  },

  /* Users ----------------------------------------------------------------- */
  getUsers() {
    return request("/api/v1/users/get");
  },

  createUser(user) {
    return request("/api/v1/users/create", { method: "POST", body: user });
  },

  updateUser(id, user) {
    return request(`/api/v1/users/${encodeURIComponent(id)}`, { method: "PUT", body: user });
  },

  deleteUser(id) {
    return request(`/api/v1/users/${encodeURIComponent(id)}`, { method: "DELETE" });
  },

  getUserTasks(userId) {
    return request(`/api/v1/users/${encodeURIComponent(userId)}/tasks`);
  },

  getUserPendingTasks(userId) {
    return request(`/api/v1/users/${encodeURIComponent(userId)}/tasks/pending`);
  },

  getUserFeedback(userId) {
    return request(`/api/v1/users/${encodeURIComponent(userId)}/tasks/feedback`);
  },

  requestTaskApproval(taskId) {
    return request(`/api/v1/users/${encodeURIComponent(taskId)}/approval`, { method: "PUT" });
  },

  /* Projects -------------------------------------------------------------- */
  getProjects() {
    return request("/api/v1/projects/get");
  },

  getProjectsByUser(userId) {
    return request(`/api/v1/projects/users/${encodeURIComponent(userId)}`);
  },

  createProject(project) {
    return request("/api/v1/projects/create", { method: "POST", body: project });
  },

  updateProject(id, project) {
    return request(`/api/v1/projects/${encodeURIComponent(id)}`, { method: "PUT", body: project });
  },

  deleteProject(id) {
    return request(`/api/v1/projects/${encodeURIComponent(id)}`, { method: "DELETE" });
  },

  assignProject(projectId, userIds) {
    return request("/api/v1/projects/assign", {
      method: "POST",
      body: { project_id: projectId, user_id: userIds },
    });
  },

  updateProjectStatus(projectId, projectStatus) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/status`, {
      method: "PUT",
      body: { projectStatus },
    });
  },

  /* Tasks ----------------------------------------------------------------- */
  getProjectTasks(projectId) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/get`);
  },

  createTask(projectId, task) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/create`, {
      method: "POST",
      body: task,
    });
  },

  updateTask(projectId, taskId, task) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}`, {
      method: "PUT",
      body: task,
    });
  },

  deleteTask(projectId, taskId) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}`, {
      method: "DELETE",
    });
  },

  assignTask(projectId, taskId, userId) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}/assign`, {
      method: "POST",
      body: { project_id: projectId, task_id: taskId, user_id: userId },
    });
  },

  approveTask(projectId, taskId) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}/approve`, {
      method: "POST",
      body: { taskId, projectId },
    });
  },

  feedbackTask(projectId, taskId, title, feedback) {
    return request(`/api/v1/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}/feedback`, {
      method: "POST",
      body: { projectId, task_id: taskId, title, feedback },
    });
  },

  /* Dashboard ------------------------------------------------------------- */
  getDashboardStatus(params = {}) {
    return request(`/api/v1/dashboard/status${buildQuery(params)}`);
  },

  getKpi(params = {}) {
    return request(`/api/v1/dashboard/kpi${buildQuery(params)}`);
  },

  /* Notifications --------------------------------------------------------- */
  getNotifications(userId) {
    return request(`/api/v1/notifications/user/${encodeURIComponent(userId)}`);
  },

  getFilteredNotifications(params = {}) {
    return request(`/api/v1/notifications/filter${buildQuery(params)}`);
  },

  markNotificationRead(id) {
    return request(`/api/v1/notifications/${encodeURIComponent(id)}/read`, { method: "PUT" });
  },

  /* Reports --------------------------------------------------------------- */
  createReport(projectId) {
    return request("/api/v1/report", {
      method: "POST",
      body: { project_id: projectId },
    });
  },

  downloadReport(projectId) {
    return request(`/api/v1/report/download${buildQuery({ projectId })}`, {
      responseType: "blob",
    });
  },
};
