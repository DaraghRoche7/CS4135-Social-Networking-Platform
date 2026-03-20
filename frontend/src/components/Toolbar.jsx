import { NavLink, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../store/slices/authSlice.js";
import { useEffect, useMemo, useState } from "react";
import roundLogo from "../../images/round-logo.png";

function getStoredTheme() {
  return localStorage.getItem("theme");
}

function applyTheme(theme) {
  if (theme === "dark") {
    document.documentElement.setAttribute("data-theme", "dark");
  } else {
    document.documentElement.removeAttribute("data-theme");
  }
}

export function Toolbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const role = useSelector((s) => s.auth.role);
  const token = useSelector((s) => s.auth.token);

  const initialTheme = useMemo(() => {
    const stored = getStoredTheme();
    if (stored) return stored;
    if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
      return "dark";
    }
    return "light";
  }, []);

  const [theme, setTheme] = useState(initialTheme);

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  const navLinks = useMemo(() => {
    // The assignments flow is: after login -> student or admin dashboard.
    if (!token) {
      return [{ to: "/login", label: "Login" }];
    }

    if (role === "ADMIN") {
      return [{ to: "/admin", label: "Admin" }];
    }

    return [{ to: "/student", label: "Student" }];
  }, [token, role]);

  const onLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  const toggleTheme = () => {
    const next = theme === "dark" ? "light" : "dark";
    setTheme(next);
    localStorage.setItem("theme", next);
  };

  return (
    <header className="toolbar" role="banner">
      <div className="toolbar__brand">
        <img className="brand-logo" src={roundLogo} alt="StudyHub logo" />
        <span className="brand-text">StudyHub</span>
      </div>

      <nav className="toolbar__nav" aria-label="Main navigation">
        {navLinks.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            className={({ isActive }) =>
              isActive ? "toolbar__link toolbar__link--active" : "toolbar__link"
            }
          >
            {l.label}
          </NavLink>
        ))}
      </nav>

      <div style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
        <button
          id="theme-toggle"
          className="theme-toggle"
          type="button"
          onClick={toggleTheme}
          aria-label="Toggle dark mode"
        >
          Toggle theme
        </button>

        {token ? (
          <button
            className="theme-toggle"
            type="button"
            onClick={onLogout}
            aria-label="Logout"
          >
            Logout
          </button>
        ) : null}
      </div>
    </header>
  );
}

