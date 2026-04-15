import { useEffect, useMemo, useState } from "react";
import { api } from "../api/http.js";

const FILTER_OPTIONS = [
  { value: "all", label: "All" },
  { value: "unread", label: "Unread" },
  { value: "read", label: "Read" }
];

function normalizeNotification(item, index) {
  return {
    id: item?.id ?? item?.notificationId ?? item?.uuid ?? `notification-${index}`,
    title: item?.title || item?.subject || item?.type || "Notification",
    message: item?.message || item?.description || item?.body || "No details provided.",
    category: item?.category || item?.type || "General",
    createdAt: item?.createdAt || item?.timestamp || item?.sentAt || item?.date || null,
    read: Boolean(item?.read ?? item?.isRead ?? item?.seen ?? false)
  };
}

function formatNotificationDate(value) {
  if (!value) {
    return "No timestamp";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
}

function filterNotifications(notifications, filter) {
  if (filter === "unread") {
    return notifications.filter((notification) => !notification.read);
  }

  if (filter === "read") {
    return notifications.filter((notification) => notification.read);
  }

  return notifications;
}

export function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState("all");
  const [busyIds, setBusyIds] = useState([]);
  const [refreshNonce, setRefreshNonce] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadNotifications() {
      setStatus("loading");
      setError(null);

      try {
        const res = await api.get("/api/notifications");
        const items = res.data?.items || res.data || [];
        const normalized = Array.isArray(items)
          ? items.map((item, index) => normalizeNotification(item, index))
          : [];

        if (!cancelled) {
          setNotifications(normalized);
          setStatus("succeeded");
        }
      } catch (err) {
        if (cancelled) {
          return;
        }

        setError(
          err?.response?.data?.message ||
            err?.response?.data?.error ||
            err?.message ||
            "Failed to load notifications",
        );
        setStatus("failed");
      }
    }

    loadNotifications();

    return () => {
      cancelled = true;
    };
  }, [refreshNonce]);

  const unreadCount = useMemo(
    () => notifications.filter((notification) => !notification.read).length,
    [notifications],
  );

  const filteredNotifications = useMemo(
    () => filterNotifications(notifications, filter),
    [notifications, filter],
  );

  const refreshNotifications = () => {
    setRefreshNonce((value) => value + 1);
  };

  const markAsRead = async (notificationId) => {
    if (!notificationId || busyIds.includes(notificationId)) {
      return;
    }

    setError(null);
    setBusyIds((currentIds) => [...currentIds, notificationId]);

    try {
      await api.put(`/api/notifications/${notificationId}/read`);
      setNotifications((items) =>
        items.map((item) =>
          item.id === notificationId
            ? {
                ...item,
                read: true
              }
            : item,
        ),
      );
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          err?.message ||
          "Unable to mark the notification as read",
      );
    } finally {
      setBusyIds((currentIds) => currentIds.filter((itemId) => itemId !== notificationId));
    }
  };

  const markAllAsRead = async () => {
    const unreadIds = notifications.filter((notification) => !notification.read).map((item) => item.id);

    if (unreadIds.length === 0) {
      return;
    }

    setError(null);
    setBusyIds((currentIds) => Array.from(new Set([...currentIds, ...unreadIds])));

    try {
      const results = await Promise.allSettled(
        unreadIds.map((notificationId) => api.put(`/api/notifications/${notificationId}/read`)),
      );

      const succeededIds = unreadIds.filter((notificationId, index) => {
        return (
          results[index]?.status === "fulfilled" &&
          notificationId !== null &&
          notificationId !== undefined
        );
      });

      if (succeededIds.length > 0) {
        setNotifications((items) =>
          items.map((item) =>
            succeededIds.includes(item.id)
              ? {
                  ...item,
                  read: true
                }
              : item,
          ),
        );
      }

      if (succeededIds.length !== unreadIds.length) {
        setError("Some notifications could not be marked as read. Please try again.");
      }
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          err?.message ||
          "Unable to update notifications",
      );
    } finally {
      setBusyIds((currentIds) => currentIds.filter((itemId) => !unreadIds.includes(itemId)));
    }
  };

  return (
    <main className="page" role="main">
      <section className="page-header card">
        <div>
          <h1 style={{ marginTop: 0, marginBottom: "0.45rem" }}>Notifications</h1>
          <p className="muted page-header__copy">
            Stay on top of course activity, platform updates, and anything that still needs your
            attention.
          </p>
        </div>

        <div className="page-header__actions">
          <button className="btn btn--secondary" type="button" onClick={refreshNotifications}>
            Refresh
          </button>
          <button
            className="btn btn--primary"
            type="button"
            onClick={markAllAsRead}
            disabled={unreadCount === 0 || busyIds.length > 0}
          >
            Mark all as read
          </button>
        </div>
      </section>

      <section className="summary-grid" aria-label="Notification summary">
        <article className="summary-card">
          <span className="summary-card__label">Total</span>
          <strong className="summary-card__value">{notifications.length}</strong>
        </article>
        <article className="summary-card">
          <span className="summary-card__label">Unread</span>
          <strong className="summary-card__value">{unreadCount}</strong>
        </article>
        <article className="summary-card">
          <span className="summary-card__label">Read</span>
          <strong className="summary-card__value">{notifications.length - unreadCount}</strong>
        </article>
      </section>

      <section className="card">
        <div className="filter-row" role="tablist" aria-label="Notification filters">
          {FILTER_OPTIONS.map((option) => (
            <button
              key={option.value}
              className={
                option.value === filter ? "filter-chip filter-chip--active" : "filter-chip"
              }
              type="button"
              onClick={() => setFilter(option.value)}
              aria-pressed={option.value === filter}
            >
              {option.label}
            </button>
          ))}
        </div>

        {error ? <p className="error-banner">{String(error)}</p> : null}

        {status === "loading" ? <p className="muted">Loading notifications...</p> : null}

        {status === "succeeded" && filteredNotifications.length === 0 ? (
          <div className="empty-state">
            <h2>No notifications here</h2>
            <p className="muted">
              {filter === "all"
                ? "You are all caught up."
                : `There are no ${filter} notifications right now.`}
            </p>
          </div>
        ) : null}

        <div className="notifications-list">
          {filteredNotifications.map((notification) => {
            const isBusy = busyIds.includes(notification.id);
            return (
              <article
                key={notification.id}
                className={
                  notification.read
                    ? "notification-card"
                    : "notification-card notification-card--unread"
                }
              >
                <div className="notification-card__meta">
                  <span className="notification-category">{notification.category}</span>
                  <span
                    className={
                      notification.read
                        ? "notification-badge notification-badge--read"
                        : "notification-badge notification-badge--unread"
                    }
                  >
                    {notification.read ? "Read" : "Unread"}
                  </span>
                </div>

                <h2>{notification.title}</h2>
                <p>{notification.message}</p>

                <div className="notification-card__footer">
                  <span className="notification-timestamp">
                    {formatNotificationDate(notification.createdAt)}
                  </span>

                  {!notification.read ? (
                    <button
                      className="btn btn--secondary"
                      type="button"
                      onClick={() => markAsRead(notification.id)}
                      disabled={isBusy}
                    >
                      {isBusy ? "Updating..." : "Mark as read"}
                    </button>
                  ) : null}
                </div>
              </article>
            );
          })}
        </div>
      </section>
    </main>
  );
}
