import { Navigate, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";

export function RequireAuth({ role, children }) {
  const token = useSelector((s) => s.auth.token);
  const userRole = useSelector((s) => s.auth.role);
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (role && userRole !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}

