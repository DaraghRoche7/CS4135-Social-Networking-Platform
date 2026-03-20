import { Link, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";

export function Landing() {
  const token = useSelector((s) => s.auth.token);
  const role = useSelector((s) => s.auth.role);

  if (token) {
    if (role === "ADMIN") return <Navigate to="/admin" replace />;
    return <Navigate to="/student" replace />;
  }

  return (
    <main className="page" role="main">
      <section className="hero-card">
        <p className="eyebrow">StudyHub</p>
        <h1>Share notes and build your academic network</h1>
        <p>
          Create an account, join study groups, upload PDF notes, and get a personalised feed based on your modules.
        </p>
        <div className="hero-card__actions">
          <Link className="btn btn--primary" to="/login" style={{ textDecoration: "none", display: "inline-block" }}>
            Login / Register
          </Link>
          <Link
            className="btn btn--secondary"
            to="/login"
            style={{ textDecoration: "none", display: "inline-block" }}
          >
            View Design Tokens
          </Link>
        </div>
      </section>
    </main>
  );
}

