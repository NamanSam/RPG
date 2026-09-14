export async function api(path, options = {}) {
  const method = options.method || "GET";
  // Cookie authentication can rotate CSRF state between requests. Never reuse a
  // previous submission's token, including when retrying an incorrect answer.
  const csrf = method !== "GET" ? await api("/auth/csrf") : null;
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
