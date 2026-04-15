import axios from "axios";

const TOKEN_KEYS = {
  access: "accessToken"
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

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      // Session expired: clear local token and send user back to login.
      localStorage.removeItem(TOKEN_KEYS.access);
      localStorage.removeItem("refreshToken");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  },
);

