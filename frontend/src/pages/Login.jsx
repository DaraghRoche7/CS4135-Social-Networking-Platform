import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { login } from "../store/slices/authSlice.js";
import roundLogo from "../../images/round-logo.png";

export function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const role = useSelector((s) => s.auth.role);
  const status = useSelector((s) => s.auth.status);
  const error = useSelector((s) => s.auth.error);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const onSubmit = async (e) => {
    e.preventDefault();
    const res = await dispatch(login({ email, password }));
    if (res.meta.requestStatus === "fulfilled") {
      if (res.payload.role === "ADMIN") navigate("/admin");
      else navigate("/student");
    }
  };

  // If we already have a role, redirect automatically.
  useEffect(() => {
    if (!role) return;
    navigate(role === "ADMIN" ? "/admin" : "/student", { replace: true });
  }, [role, navigate]);

  return (
    <main className="page login-page" role="main">
      <section className="card login-card">
        <img className="login-logo" src={roundLogo} alt="StudyHub round logo" />

        <h1 style={{ marginTop: 0, marginBottom: "0.25rem", textAlign: "center" }}>Login</h1>
        <p className="muted login-subtitle" style={{ marginTop: 0 }}>
          Welcome to StudyHub
        </p>

        <form onSubmit={onSubmit}>
          <div className="form-row">
            <label>
              Email
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                type="email"
                autoComplete="email"
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              Password
              <input
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                type="password"
                autoComplete="current-password"
                required
              />
            </label>
          </div>

          {error ? (
            <p style={{ color: "#c0392b", marginTop: "0.75rem" }}>{String(error)}</p>
          ) : null}

          <div className="btn-row">
            <button className="btn btn--primary" type="submit" disabled={status === "loading"}>
              {status === "loading" ? "Logging in..." : "Login"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}

