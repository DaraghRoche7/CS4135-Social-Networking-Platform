import { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/http.js";
import roundLogo from "../../images/round-logo.png";

export function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post("/api/auth/forgot-password", { email });
    } catch {}
    finally {
      setSubmitted(true);
      setLoading(false);
    }
  };

  return (
    <main className="page login-page" role="main">
      <section className="card login-card">
        <img className="login-logo" src={roundLogo} alt="StudyHub round logo" />

        <h1 style={{ marginTop: 0, marginBottom: "0.25rem", textAlign: "center" }}>Reset password</h1>

        {submitted ? (
          <>
            <p style={{ textAlign: "center" }}>
              If that email is registered you will receive a reset link shortly.
            </p>
            <p className="muted" style={{ textAlign: "center", fontSize: "0.9rem" }}>
              <Link to="/login" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
                Back to login
              </Link>
            </p>
          </>
        ) : (
          <form onSubmit={onSubmit}>
            <div className="form-row">
              <label>
                Email
                <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="email" required />
              </label>
            </div>

            <div className="btn-row">
              <button className="btn btn--primary" type="submit" disabled={loading}>
                {loading ? "Sending" : "Send reset link"}
              </button>
            </div>

            <p className="muted" style={{ textAlign: "center", marginTop: "1rem", fontSize: "0.9rem" }}>
              <Link to="/login" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
                Back to login
              </Link>
            </p>
          </form>
        )}
      </section>
    </main>
  );
}