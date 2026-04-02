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

  const onSubmit = async (e) => {
    e.preventDefault();
    const res = await dispatch(register({ name, email, password }));
    if (res.meta.requestStatus === "fulfilled") {
      navigate("/student");
    }
  };

  useEffect(() => {
    if (!role) return;
    navigate(role === "ADMIN" ? "/admin" : "/student", { replace: true });
  }, [role, navigate]);

  return (
    <main className="page login-page" role="main">
      <section className="card login-card">
        <img className="login-logo" src={roundLogo} alt="StudyHub round logo" />
        <h1 style={{ marginTop: 0, marginBottom: "0.25rem", textAlign: "center" }}>Sign up</h1>
        <p className="muted login-subtitle" style={{ marginTop: 0 }}>
          UL student email required
        </p>

        <form onSubmit={onSubmit}>
          <div className="form-row">
            <label>
              Name
              <input value={name} onChange={(e) => setName(e.target.value)} type="text" autoComplete="name" required />
            </label>
          </div>

          <div className="form-row">
            <label>
              Email
              <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="email" placeholder="Use your student email" required />
            </label>
          </div>

          <div className="form-row">
            <label>
              Password
              <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" autoComplete="new-password" minLength={8} required />
            </label>
          </div>

          {error ? ( <p style={{ color: "#c0392b", marginTop: "0.75rem" }}>{String(error)}</p>) : null}

          <div className="btn-row">
            <button className="btn btn--primary" type="submit" disabled={status === "loading"}>
              {status === "loading" ? "Creating account" : "Create account"}
            </button>
          </div>
        </form>

        <p className="muted" style={{ textAlign: "center", marginTop: "1rem", fontSize: "0.9rem" }}>
          Already have an account?{" "}
          <Link to="/login" style={{ color: "var(--color-primary)", fontWeight: 600 }}>
            Log in
          </Link>
        </p>
      </section>
    </main>
  );
}