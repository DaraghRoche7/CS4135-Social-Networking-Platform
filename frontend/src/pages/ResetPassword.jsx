import { useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { api } from "../api/http.js";
import roundLogo from "../../images/round-logo.png";

export function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const token = searchParams.get("token");

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await api.post("/api/auth/reset-password", { token, newPassword });
      navigate("/login", { state: { message: "Password reset — please log in." } });
    } catch (err) {
      setError(
        err?.response?.data?.detail ||
        err?.response?.data?.message ||
        "Invalid or expired reset link."
      );
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <main className="page login-page" role="main">
        <section className="card login-card">
          <p style={{ textAlign: "center" }}>Invalid reset link.</p>
          <p className="muted" style={{ textAlign: "center" }}>
            <Link to="/forgot-password" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
              Request a new one
            </Link>
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="page login-page" role="main">
      <section className="card login-card">
        <img className="login-logo" src={roundLogo} alt="StudyHub round logo" />

        <h1 style={{ marginTop: 0, marginBottom: "0.25rem", textAlign: "center" }}>New password</h1>

        <form onSubmit={onSubmit}>
          <div className="form-row">
            <label>
              New password
              <input value={newPassword} onChange={(e) => setNewPassword(e.target.value)} type="password" autoComplete="new-password" minLength={8} required />
            </label>
          </div>

          {error && ( <p style={{ color: "#c0392b", marginTop: "0.75rem" }}>{error}</p> )}

          <div className="btn-row">
            <button className="btn btn--primary" type="submit" disabled={loading}>
              {loading ? "Saving" : "Set new password"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}