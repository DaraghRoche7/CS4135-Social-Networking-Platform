import { useEffect, useState } from "react";
import { api } from "../api/http.js";

function uploadPostFieldsLabel(s) {
  return s ? String(s) : "";
}

export function StudentDashboard() {
  const [feed, setFeed] = useState([]);
  const [feedStatus, setFeedStatus] = useState("idle");
  const [feedError, setFeedError] = useState(null);

  const [moduleFilter, setModuleFilter] = useState("");
  const [feedRefreshNonce, setFeedRefreshNonce] = useState(0);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [module, setModule] = useState("");
  const [pdfFile, setPdfFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState("idle");
  const [uploadError, setUploadError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setFeedStatus("loading");
      setFeedError(null);
      try {
        const res = await api.get("/api/feed", {
          params: moduleFilter ? { module: moduleFilter } : undefined
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

  const onSubmitUpload = async (e) => {
    e.preventDefault();
    if (!pdfFile) {
      setUploadError("Please choose a PDF file.");
      return;
    }
    setUploadStatus("loading");
    setUploadError(null);
    try {
      const form = new FormData();
      form.append("title", title);
      form.append("description", description);
      form.append("module", module);
      form.append("file", pdfFile);

      await api.post("/api/posts", form, {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      });

      setTitle("");
      setDescription("");
      setModule("");
      setPdfFile(null);

      // Refresh feed after successful upload.
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
                likesCount: (it.likesCount || it.likes || 0) + 1
              }
            : it,
        ),
      );
    } catch {
      // For the starter UI we keep error handling minimal.
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
                likesCount: Math.max(0, (it.likesCount || it.likes || 0) - 1)
              }
            : it,
        ),
      );
    } catch {
      // keep minimal
    }
  };

  return (
    <div className="app-shell">
      <main className="page" role="main">
        <div className="grid-2">
          <section className="card">
            <h1 style={{ marginTop: 0 }}>Student Dashboard</h1>
            <p className="muted" style={{ marginTop: 0 }}>
              Upload PDF notes and like posts in your personalised feed.
            </p>

            <div className="form-row">
              <label>
                Filter feed by module (optional)
                <input
                  value={moduleFilter}
                  onChange={(e) => setModuleFilter(e.target.value)}
                  placeholder="e.g. CS4135"
                />
              </label>
            </div>

            <div style={{ marginTop: "1rem" }}>
              <h2 style={{ margin: "0 0 0.5rem" }}>Personalised Feed</h2>
              {feedStatus === "loading" ? <p className="muted">Loading feed...</p> : null}
              {feedStatus === "failed" ? (
                <p style={{ color: "#c0392b" }}>{String(feedError)}</p>
              ) : null}
              {feedStatus === "succeeded" && feed.length === 0 ? (
                <p className="muted">No feed items yet.</p>
              ) : null}
              <div className="feed-list">
                {feed.map((item) => {
                  const postId = item.id;
                  const titleText = uploadPostFieldsLabel(item.title);
                  const desc = uploadPostFieldsLabel(item.description);
                  const likes = item.likesCount ?? item.likes ?? 0;
                  const liked = Boolean(item.likedByUser || item.liked);
                  return (
                    <article key={postId} className="feed-item">
                      <h3>{titleText || "Untitled note"}</h3>
                      <p className="muted" style={{ margin: 0 }}>
                        {desc || "No description."}
                      </p>
                      <div className="btn-row">
                        <button
                          className="btn btn--primary"
                          type="button"
                          onClick={() => (liked ? unlikePost(postId) : likePost(postId))}
                        >
                          {liked ? `Unlike (${likes})` : `Like (${likes})`}
                        </button>
                      </div>
                    </article>
                  );
                })}
              </div>
            </div>
          </section>

          <aside className="card">
            <h2 style={{ marginTop: 0 }}>Upload a PDF Note</h2>
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
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                  />
                </label>
              </div>

              <div className="form-row">
                <label>
                  Module
                  <input value={module} onChange={(e) => setModule(e.target.value)} required />
                </label>
              </div>

              <div className="form-row">
                <label>
                  PDF file
                  <input
                    type="file"
                    accept="application/pdf"
                    onChange={(e) => setPdfFile(e.target.files?.[0] || null)}
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
          </aside>
        </div>
      </main>
    </div>
  );
}

