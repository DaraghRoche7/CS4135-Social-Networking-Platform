import { useEffect, useState } from "react";
import { api } from "../api/http.js";

function uploadPostFieldsLabel(s) {
  return s ? String(s) : "";
}

function formatInstant(iso) {
  if (!iso) return "";
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return String(iso);
  }
}

function FeedPostRow({ item, likePost, unlikePost, onFeedRefresh }) {
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [commentDraft, setCommentDraft] = useState("");
  const [commentError, setCommentError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function loadComments() {
      setCommentsLoading(true);
      try {
        const res = await api.get(`/api/posts/${item.id}/comments`);
        if (!cancelled) setComments(Array.isArray(res.data) ? res.data : []);
      } catch {
        if (!cancelled) setComments([]);
      } finally {
        if (!cancelled) setCommentsLoading(false);
      }
    }
    loadComments();
    return () => {
      cancelled = true;
    };
  }, [item.id]);

  const submitComment = async (e) => {
    e.preventDefault();
    const text = commentDraft.trim();
    if (!text) return;
    setCommentError(null);
    try {
      await api.post(`/api/posts/${item.id}/comments`, { body: text });
      setCommentDraft("");
      const res = await api.get(`/api/posts/${item.id}/comments`);
      setComments(Array.isArray(res.data) ? res.data : []);
      onFeedRefresh();
    } catch (err) {
      setCommentError(
        err?.response?.data?.message || err?.response?.data?.error || err?.message || "Could not post comment",
      );
    }
  };

  const likes = item.likesCount ?? item.likes ?? item.likeCount ?? 0;
  const liked = Boolean(item.likedByUser);

  return (
    <article className="feed-item">
      <h3>{uploadPostFieldsLabel(item.title) || "Untitled note"}</h3>
      <p className="muted" style={{ margin: 0 }}>
        {uploadPostFieldsLabel(item.description) || "No description."}
      </p>
      <p className="muted" style={{ margin: "0.35rem 0 0", fontSize: "0.85rem" }}>
        Module: <strong>{uploadPostFieldsLabel(item.moduleCode)}</strong>
      </p>
      <div className="btn-row" style={{ marginTop: "0.75rem" }}>
        <button
          className="btn btn--primary"
          type="button"
          onClick={() => (liked ? unlikePost(item.id) : likePost(item.id))}
        >
          {liked ? `Unlike (${likes})` : `Like (${likes})`}
        </button>
      </div>

      <div style={{ marginTop: "1rem" }}>
        <h4 style={{ margin: "0 0 0.5rem", fontSize: "1rem" }}>Comments</h4>
        {commentsLoading ? <p className="muted">Loading comments…</p> : null}
        {!commentsLoading && comments.length === 0 ? <p className="muted">No comments yet.</p> : null}
        <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
          {comments.map((c) => (
            <div
              key={c.id}
              style={{
                borderTop: "1px solid var(--border, #ddd)",
                paddingTop: "0.5rem",
                fontSize: "0.9rem",
              }}
            >
              <div className="muted" style={{ fontSize: "0.8rem" }}>
                User <code>{uploadPostFieldsLabel(c.userId)}</code> · {formatInstant(c.createdAt)}
              </div>
              <div style={{ marginTop: "0.25rem", whiteSpace: "pre-wrap" }}>{uploadPostFieldsLabel(c.body)}</div>
            </div>
          ))}
        </div>

        <form onSubmit={submitComment} style={{ marginTop: "0.75rem" }}>
          <div className="form-row">
            <label>
              Add a comment
              <textarea
                value={commentDraft}
                onChange={(e) => setCommentDraft(e.target.value)}
                rows={2}
                placeholder="Write a comment…"
              />
            </label>
          </div>
          {commentError ? <p style={{ color: "#c0392b" }}>{String(commentError)}</p> : null}
          <div className="btn-row">
            <button className="btn btn--secondary" type="submit" disabled={!commentDraft.trim()}>
              Post comment
            </button>
          </div>
        </form>
      </div>
    </article>
  );
}

export function StudentDashboard() {
  const [feed, setFeed] = useState([]);
  const [feedStatus, setFeedStatus] = useState("idle");
  const [feedError, setFeedError] = useState(null);

  const [moduleFilter, setModuleFilter] = useState("");
  const [feedRefreshNonce, setFeedRefreshNonce] = useState(0);

  const [followedModules, setFollowedModules] = useState([]);
  const [modulesStatus, setModulesStatus] = useState("idle");
  const [modulesError, setModulesError] = useState(null);
  const [newModuleCode, setNewModuleCode] = useState("");

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [module, setModule] = useState("");
  const [pngFile, setPngFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState("idle");
  const [uploadError, setUploadError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function loadModules() {
      setModulesStatus("loading");
      setModulesError(null);
      try {
        const res = await api.get("/api/modules");
        const items = res.data || [];
        if (!cancelled) setFollowedModules(Array.isArray(items) ? items : []);
        if (!cancelled) setModulesStatus("succeeded");
      } catch (e) {
        if (cancelled) return;
        setModulesError(e?.response?.data?.message || e?.message || "Failed to load modules");
        setModulesStatus("failed");
      }
    }
    loadModules();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setFeedStatus("loading");
      setFeedError(null);
      try {
        const res = await api.get("/api/feed", {
          params: moduleFilter ? { module: moduleFilter } : undefined,
        });
        const items = res.data?.items || res.data || [];
        if (!cancelled) setFeed(Array.isArray(items) ? items : []);
        if (!cancelled) setFeedStatus("succeeded");
      } catch (e) {
        if (cancelled) return;
        setFeedError(e?.response?.data?.message || e?.message || "Failed to load feed");
        setFeedStatus("failed");
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [moduleFilter, feedRefreshNonce]);

  const followModule = async (e) => {
    e.preventDefault();
    const code = (newModuleCode || "").trim();
    if (!code) return;
    try {
      await api.post("/api/modules/follow", { moduleCode: code });
      const res = await api.get("/api/modules");
      setFollowedModules(Array.isArray(res.data) ? res.data : []);
      setNewModuleCode("");
      setFeedRefreshNonce((n) => n + 1);
    } catch (e2) {
      setModulesError(e2?.response?.data?.message || e2?.message || "Failed to follow module");
    }
  };

  const unfollowModule = async (code) => {
    const normalized = (code || "").trim().toUpperCase();
    if (!normalized) return;
    try {
      await api.delete(`/api/modules/${encodeURIComponent(normalized)}`);
      const res = await api.get("/api/modules");
      setFollowedModules(Array.isArray(res.data) ? res.data : []);
      setFeedRefreshNonce((n) => n + 1);
    } catch (e2) {
      setModulesError(e2?.response?.data?.message || e2?.message || "Failed to unfollow module");
    }
  };

  const onSubmitUpload = async (e) => {
    e.preventDefault();
    if (!pngFile) {
      setUploadError("Please choose a PNG file.");
      return;
    }
    setUploadStatus("loading");
    setUploadError(null);
    try {
      const form = new FormData();
      form.append("title", title);
      form.append("description", description);
      form.append("module", module);
      form.append("file", pngFile);

      await api.post("/api/posts", form);

      setTitle("");
      setDescription("");
      setModule("");
      setPngFile(null);

      setUploadStatus("succeeded");
      setFeedRefreshNonce((n) => n + 1);
    } catch (err) {
      setUploadError(
        err?.response?.data?.message || err?.response?.data?.error || err?.message || "Upload failed",
      );
      setUploadStatus("failed");
    }
  };

  const likePost = async (postId) => {
    if (!postId) return;
    try {
      await api.post(`/api/posts/${postId}/like`);
      setFeed((items) =>
        items.map((it) =>
          it.id === postId
            ? {
                ...it,
                likedByUser: true,
                likesCount: (it.likesCount ?? it.likeCount ?? it.likes ?? 0) + 1,
              }
            : it,
        ),
      );
    } catch {
      // minimal
    }
  };

  const unlikePost = async (postId) => {
    if (!postId) return;
    try {
      await api.delete(`/api/posts/${postId}/like`);
      setFeed((items) =>
        items.map((it) =>
          it.id === postId
            ? {
                ...it,
                likedByUser: false,
                likesCount: Math.max(0, (it.likesCount ?? it.likeCount ?? it.likes ?? 0) - 1),
              }
            : it,
        ),
      );
    } catch {
      // minimal
    }
  };

  return (
    <div className="app-shell">
      <main className="page" role="main">
        <div className="grid-2">
          <section className="card">
            <h1 style={{ marginTop: 0 }}>Student Dashboard</h1>
            <p className="muted" style={{ marginTop: 0 }}>
              Follow a module (try <strong>4135</strong> or <strong>CS4135</strong>), view your feed, like posts, and
              add comments. Uploads must be <strong>PNG</strong> images only.
            </p>

            <div className="form-row">
              <label>
                Filter feed by module (optional)
                <input
                  value={moduleFilter}
                  onChange={(e) => setModuleFilter(e.target.value)}
                  placeholder="e.g. 4135 or CS4135"
                />
              </label>
            </div>

            <div style={{ marginTop: "1rem" }}>
              <h2 style={{ margin: "0 0 0.5rem" }}>Personalised Feed</h2>
              {feedStatus === "loading" ? <p className="muted">Loading feed...</p> : null}
              {feedStatus === "failed" ? <p style={{ color: "#c0392b" }}>{String(feedError)}</p> : null}
              {feedStatus === "succeeded" && feed.length === 0 ? (
                <p className="muted">No feed items yet. Follow a module that has posts (e.g. CS4135).</p>
              ) : null}
              <div className="feed-list">
                {feed.map((item) => (
                  <FeedPostRow
                    key={item.id}
                    item={item}
                    likePost={likePost}
                    unlikePost={unlikePost}
                    onFeedRefresh={() => setFeedRefreshNonce((n) => n + 1)}
                  />
                ))}
              </div>
            </div>
          </section>

          <aside style={{ display: "grid", gap: "1rem", alignContent: "start" }}>
            <section className="card">
              <h2 style={{ marginTop: 0 }}>Follow Modules</h2>
              <p className="muted" style={{ marginTop: 0 }}>
                Typing <strong>4135</strong> is treated as <strong>CS4135</strong> on the server.
              </p>

              <form onSubmit={followModule}>
                <div className="form-row">
                  <label>
                    Module code
                    <input
                      value={newModuleCode}
                      onChange={(e) => setNewModuleCode(e.target.value)}
                      placeholder="e.g. 4135 or CS4135"
                    />
                  </label>
                </div>
                <div className="btn-row">
                  <button className="btn btn--secondary" type="submit" disabled={modulesStatus === "loading"}>
                    Follow
                  </button>
                </div>
              </form>

              {modulesStatus === "loading" ? <p className="muted">Loading modules...</p> : null}
              {modulesError ? <p style={{ color: "#c0392b" }}>{String(modulesError)}</p> : null}

              <div style={{ marginTop: "0.75rem" }}>
                {followedModules.length === 0 ? (
                  <p className="muted">No followed modules yet.</p>
                ) : (
                  <div className="feed-list">
                    {followedModules.map((m) => (
                      <div
                        key={m}
                        className="feed-item"
                        style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
                      >
                        <div>
                          <strong>{m}</strong>
                        </div>
                        <button className="btn btn--danger" type="button" onClick={() => unfollowModule(m)}>
                          Unfollow
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </section>

            <section className="card">
              <h2 style={{ marginTop: 0 }}>Upload a PNG note</h2>
              <form onSubmit={onSubmitUpload}>
                <div className="form-row">
                  <label>
                    Title
                    <input value={title} onChange={(e) => setTitle(e.target.value)} required />
                  </label>
                </div>

                <div className="form-row">
                  <label>
                    Description
                    <textarea value={description} onChange={(e) => setDescription(e.target.value)} required />
                  </label>
                </div>

                <div className="form-row">
                  <label>
                    Module
                    <input value={module} onChange={(e) => setModule(e.target.value)} required placeholder="CS4135" />
                  </label>
                </div>

                <div className="form-row">
                  <label>
                    PNG file
                    <input
                      type="file"
                      accept="image/png,.png"
                      onChange={(e) => setPngFile(e.target.files?.[0] || null)}
                      required
                    />
                  </label>
                </div>

                {uploadError ? <p style={{ color: "#c0392b" }}>{String(uploadError)}</p> : null}

                <div className="btn-row">
                  <button className="btn btn--primary" type="submit" disabled={uploadStatus === "loading"}>
                    {uploadStatus === "loading" ? "Uploading..." : "Upload"}
                  </button>
                </div>
              </form>
            </section>
          </aside>
        </div>
      </main>
    </div>
  );
}
