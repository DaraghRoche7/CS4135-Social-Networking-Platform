import axios from "axios";

const TOKEN_KEYS = {
  access: "accessToken",
  refresh: "refreshToken"
};

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: {
    Accept: "application/json"
  }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEYS.access);
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let pendingQueue = [];

function processQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  pendingQueue = [];
}

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;
    const isAuthEndpoint = originalRequest?.url?.startsWith("/api/auth/");
    if (err?.response?.status === 401 && !isAuthEndpoint && !originalRequest._retried) {
      originalRequest._retried = true;
      const refreshToken = localStorage.getItem(TOKEN_KEYS.refresh);

      if (!refreshToken) {
        clearAuthAndRedirect();
        return Promise.reject(err);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject });
        }).then((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        });
      }

      isRefreshing = true;

      try {
        const res = await axios.post(
          `${api.defaults.baseURL}/api/auth/refresh`,
          { refreshToken },
          { headers: { Accept: "application/json" } }
        );

        const newJwt = res.data.token;
        const newRefreshToken = res.data.refreshToken;

        localStorage.setItem(TOKEN_KEYS.access, newJwt);
        if (newRefreshToken) {
          localStorage.setItem(TOKEN_KEYS.refresh, newRefreshToken);
        }

        api.defaults.headers.common.Authorization = `Bearer ${newJwt}`;
        processQueue(null, newJwt);
        originalRequest.headers.Authorization = `Bearer ${newJwt}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearAuthAndRedirect();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(err);
  }
);
function clearAuthAndRedirect() {
  localStorage.removeItem(TOKEN_KEYS.access);
  localStorage.removeItem(TOKEN_KEYS.refresh);
  window.location.href = "/login";
}
