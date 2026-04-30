import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, Link } from "react-router-dom";
import { register } from "../store/slices/authSlice.js";
import roundLogo from "../../images/round-logo.png";

export function Register() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const role = useSelector((s) => s.auth.role);
  const status = useSelector((s) => s.auth.status);
  const error = useSelector((s) => s.auth.error);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [localError, setLocalError] = useState(null);

  useEffect(() => {
    if (!role) return;
    navigate(role === "ADMIN" ? "/admin" : "/student", { replace: true });
  }, [role, navigate]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLocalError(null);
    if (password !== confirm) {
      setLocalError("Passwords do not match.");
      return;
    }
    const res = await dispatch(register({ name, email, password }));
    if (res.meta.requestStatus === "fulfilled") {
      navigate(res.payload.role === "ADMIN" ? "/admin" : "/student");
    }
  };

  const displayError = localError || (status === "failed" ? error : null);

  return (
    <main className="page login-page" role="main">
      <section className="card login-card">
        <img className="login-logo" src={roundLogo} alt="StudyHub round logo" />

        <h1 style={{ marginTop: 0, marginBottom: "0.25rem", textAlign: "center" }}>Create account</h1>
        <p className="muted login-subtitle" style={{ marginTop: 0 }}>
          Use your UL email to sign up
        </p>

        {displayError && <p className="error-banner">{String(displayError)}</p>}

        <form onSubmit={onSubmit}>
          <div className="form-row">
            <label>
              Full name
              <input value={name} onChange={(e) => setName(e.target.value)} type="text" autoComplete="name" required />
            </label>
          </div>
          <div className="form-row">
            <label>
              Email <span className="muted" style={{ fontWeight: 400 }}>(@ul.ie or @studentmail.ul.ie)</span>
              <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="email" required />
            </label>
          </div>
          <div className="form-row">
            <label>
              Password
              <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" autoComplete="new-password" required />
            </label>
          </div>
          <div className="form-row">
            <label>
              Confirm password
              <input value={confirm} onChange={(e) => setConfirm(e.target.value)} type="password" autoComplete="new-password" required />
            </label>
          </div>

          <div className="btn-row">
            <button className="btn btn--primary" type="submit" disabled={status === "loading"}>
              {status === "loading" ? "Creating account..." : "Create account"}
            </button>
          </div>
        </form>

        <p style={{ textAlign: "center", marginTop: "1rem", fontSize: "0.95rem" }}>
          Already have an account?{" "}
          <Link to="/login" style={{ color: "var(--color-primary)", fontWeight: 600 }}>Login</Link>
        </p>
      </section>
    </main>
  );
}