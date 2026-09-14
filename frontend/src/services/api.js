let csrf;

export async function api(path, options = {}) {
  const method = options.method || "GET";
  if (method !== "GET" && !csrf) csrf = await api("/auth/csrf");
  const response = await fetch(`/api${path}`, {
    credentials: "same-origin",
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(method !== "GET" ? { [csrf.headerName]: csrf.token } : {}),
      ...options.headers,
    },
  });
  const body = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 403) csrf = undefined;
    const error = new Error(
      body?.message ||
        (response.status === 401
          ? "Please sign in to continue."
          : "The game server is unavailable. Check that the backend is running."),
    );
    error.status = response.status;
    throw error;
  }
  return body;
}

export function resetCsrf() {
  csrf = undefined;
}
