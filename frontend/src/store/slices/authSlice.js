import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../api/http.js";

const TOKEN_KEYS = {
  access: "accessToken",
  refresh: "refreshToken"
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

export const login = createAsyncThunk("auth/login", async (credentials, thunkApi) => {
  try {
    const res = await api.post("/api/auth/login", credentials);
    const data = res.data ?? res;

    const token =
      data.accessToken || data.access_token || data.token || data.jwt || null;

    const refreshToken =
      data.refreshToken || data.refresh_token || null;

    const role = data.role || data.userRole || roleFromToken(token) || "USER";

    if (token) {
      localStorage.setItem(TOKEN_KEYS.access, token);
    }
    if (refreshToken) {
      localStorage.setItem(TOKEN_KEYS.refresh, refreshToken);
    }

    return {
      token,
      refreshToken,
      role
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
  const role = token ? roleFromToken(token) || "USER" : null;
  return { token, refreshToken, role };
});

const authSlice = createSlice({
  name: "auth",
  initialState: {
    token: null,
    refreshToken: null,
    role: null,
    hydrated: false,
    status: "idle",
    error: null
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem(TOKEN_KEYS.access);
      localStorage.removeItem(TOKEN_KEYS.refresh);
      state.token = null;
      state.refreshToken = null;
      state.role = null;
      state.hydrated = true;
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
        state.hydrated = true;
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
        state.hydrated = true;
      })
      .addCase(login.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload || "Login failed";
        state.hydrated = true;
      });
  }
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
