import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../api/http.js";

const TOKEN_KEYS = {
  access: "accessToken",
  refresh: "refreshToken",
  role: "userRole",
  userId: "userId"
};

function decodeJwtPayload(token) {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, "0")}`)
        .join(""),
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function roleFromToken(token) {
  const payload = decodeJwtPayload(token);
  if (!payload) return null;
  return (
    payload.role ||
    (payload.roles && payload.roles[0]) ||
    payload.authorities ||
    payload.authority ||
    payload.userRole ||
    null
  );
}

export const register = createAsyncThunk("auth/register", async (credentials, thunkApi) => {
  try {
    const res = await api.post("/api/auth/register", credentials);
    const data = res.data ?? res;

    const token = data.accessToken || data.access_token || data.token || data.jwt || null;
    const refreshToken = data.refreshToken || data.refresh_token || null;
    const userId = data.userId || data.user_id || null;
    const role = data.role || data.userRole || roleFromToken(token) || "STUDENT";

    if (token) {
      localStorage.setItem(TOKEN_KEYS.access, token);
    }
    if (refreshToken) {
      localStorage.setItem(TOKEN_KEYS.refresh, refreshToken);
    }
    if (role) {
      localStorage.setItem(TOKEN_KEYS.role, role);
    }
    if (userId) {
      localStorage.setItem(TOKEN_KEYS.userId, String(userId));
    }

    return { token, refreshToken, role, userId };
  } catch (err) {
    const errors = err?.response?.data?.errors;
    const message =
      errors ? Object.values(errors).join(", ") :
      err?.response?.data?.detail ||
      err?.response?.data?.message ||
      err?.message ||
      "Registration failed";
    return thunkApi.rejectWithValue(message);
  }
});

export const login = createAsyncThunk("auth/login", async (credentials, thunkApi) => {
  try {
    const res = await api.post("/api/auth/login", credentials);
    const data = res.data ?? res;

    const token =
      data.accessToken || data.access_token || data.token || data.jwt || null;

    const refreshToken =
      data.refreshToken || data.refresh_token || null;

    const userId = data.userId || data.user_id || null;
    const role = data.role || data.userRole || roleFromToken(token) || "USER";

    if (token) {
      localStorage.setItem(TOKEN_KEYS.access, token);
    }
    if (refreshToken) {
      localStorage.setItem(TOKEN_KEYS.refresh, refreshToken);
    }
    if (role) {
      localStorage.setItem(TOKEN_KEYS.role, role);
    }
    if (userId) {
      localStorage.setItem(TOKEN_KEYS.userId, String(userId));
    }

    return {
      token,
      refreshToken,
      role,
      userId
    };
  } catch (err) {
    const message =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err?.message ||
      "Login failed";
    return thunkApi.rejectWithValue(message);
  }
});

export const loadAuthFromStorage = createAsyncThunk("auth/load", async () => {
  const token = localStorage.getItem(TOKEN_KEYS.access);
  const refreshToken = localStorage.getItem(TOKEN_KEYS.refresh);
  const role = localStorage.getItem(TOKEN_KEYS.role) || (token ? roleFromToken(token) : null);
  const userId = localStorage.getItem(TOKEN_KEYS.userId);
  return { token, refreshToken, role, userId };
});

const authSlice = createSlice({
  name: "auth",
  initialState: {
    token: null,
    refreshToken: null,
    role: null,
    userId: null,
    status: "idle",
    error: null
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem(TOKEN_KEYS.access);
      localStorage.removeItem(TOKEN_KEYS.refresh);
      localStorage.removeItem(TOKEN_KEYS.role);
      localStorage.removeItem(TOKEN_KEYS.userId);
      state.token = null;
      state.refreshToken = null;
      state.role = null;
      state.userId = null;
      state.status = "idle";
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadAuthFromStorage.fulfilled, (state, action) => {
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.role = action.payload.role;
        state.userId = action.payload.userId;
      })
      .addCase(register.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.role = action.payload.role;
        state.userId = action.payload.userId;
      })
      .addCase(register.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload || "Registration failed";
      })
      .addCase(login.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.role = action.payload.role;
        state.userId = action.payload.userId;
      })
      .addCase(login.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload || "Login failed";
      });
  }
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;

