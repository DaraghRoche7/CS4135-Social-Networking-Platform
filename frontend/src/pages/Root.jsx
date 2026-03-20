import { Navigate } from "react-router-dom";
import { useSelector } from "react-redux";

export function RootRedirect() {
  const token = useSelector((s) => s.auth.token);
  const role = useSelector((s) => s.auth.role);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (role === "ADMIN") {
    return <Navigate to="/admin" replace />;
  }

  return <Navigate to="/student" replace />;
}

