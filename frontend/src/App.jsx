import { useEffect } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useDispatch } from "react-redux";
import { loadAuthFromStorage } from "./store/slices/authSlice.js";
import { Toolbar } from "./components/Toolbar.jsx";
import { RequireAuth } from "./routes/RequireAuth.jsx";
import { RootRedirect } from "./pages/Root.jsx";
import { Login } from "./pages/Login.jsx";
import { StudentDashboard } from "./pages/StudentDashboard.jsx";
import { AdminDashboard } from "./pages/AdminDashboard.jsx";
import { NotificationsPage } from "./pages/Notifications.jsx";

export default function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(loadAuthFromStorage());
  }, [dispatch]);

  return (
    <>
      <Toolbar />
      <Routes>
        <Route path="/" element={<RootRedirect />} />
        <Route path="/login" element={<Login />} />

        <Route
          path="/student"
          element={
            <RequireAuth role="STUDENT">
              <StudentDashboard />
            </RequireAuth>
          }
        />

        <Route
          path="/admin"
          element={
            <RequireAuth role="ADMIN">
              <AdminDashboard />
            </RequireAuth>
          }
        />

        <Route
          path="/notifications"
          element={
            <RequireAuth>
              <NotificationsPage />
            </RequireAuth>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
