import { useEffect, useState } from "react";
import { api } from "../api/http.js";

export function AdminDashboard() {
  const [notifications, setNotifications] = useState([]);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setStatus("loading");
      setError(null);
      try {
        const res = await api.get("/api/notifications");
        const items = res.data?.items || res.data || [];
        if (!cancelled) setNotifications(Array.isArray(items) ? items : []);
        if (!cancelled) setStatus("succeeded");
      } catch (e) {
        if (cancelled) return;
        setError(e?.response?.data?.message || e?.message || "Failed to load notifications");
        setStatus("failed");
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="page" role="main">
      <section className="card">
        <h1 style={{ marginTop: 0 }}>Admin Panel</h1>
        <p className="muted" style={{ marginTop: 0 }}>
          Moderate content and handle user reports. (Starter UI; hook up moderation endpoints next.)
        </p>

        {status === "loading" ? <p className="muted">Loading...</p> : null}
        {status === "failed" ? <p style={{ color: "#c0392b" }}>{String(error)}</p> : null}

        <h2 style={{ marginTop: "1.25rem" }}>User Notifications</h2>
        {notifications.length === 0 && status === "succeeded" ? (
          <p className="muted">No notifications yet.</p>
        ) : null}

        <div className="feed-list">
          {notifications.map((n) => (
            <article className="feed-item" key={n.id || JSON.stringify(n)}>
              <h3>{n.title || "Notification"}</h3>
              <p className="muted" style={{ margin: 0 }}>
                {n.message || n.description || "No details."}
              </p>
              <p className="muted" style={{ margin: "0.6rem 0 0" }}>
                Status: {n.read ? "Read" : "Unread"}
              </p>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}

