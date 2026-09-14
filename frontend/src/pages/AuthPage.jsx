import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api, resetCsrf } from "../services/api";
import { PixelScene } from "../features/world-map/PixelScene";

export function AuthPage({ register = false, onAuth, serverError }) {
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const location = useLocation();
  useEffect(() => setError(""), [location.pathname]);
  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    const data = Object.fromEntries(new FormData(event.currentTarget));
    try {
      const user = await api(`/auth/${register ? "register" : "login"}`, {
        method: "POST",
        body: JSON.stringify(data),
      });
      resetCsrf();
      onAuth(user);
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <main className="auth-page">
      <div className="auth-landscape">
        <PixelScene theme="beach" />
        <Link to="/preview" className="brand">
          <span className="brand-icon">{"{ }"}</span> CODEQUEST RPG
        </Link>
        <div>
          <p className="eyebrow">YOUR NEXT CHAPTER</p>
          <h1>
            A little Java.
            <br />A grand adventure.
          </h1>
          <p>Five worlds. One path. A whole new skill.</p>
        </div>
      </div>
      <section className="auth-form-wrap">
        <Link className="back-link" to="/preview">
          ← Back to the world
        </Link>
        <form key={location.pathname} onSubmit={submit}>
          <p className="eyebrow">
            {register ? "CREATE YOUR ADVENTURER" : "WELCOME BACK, EXPLORER"}
          </p>
          <h1>{register ? "Begin your story." : "Return to the realm."}</h1>
          <p>
            {register
              ? "Your first quest is just over the horizon."
              : "Your next adventure is waiting for you."}
          </p>
          {register && (
            <label>
              Adventurer name
              <input
                name="displayName"
                autoComplete="nickname"
                minLength={2}
                maxLength={30}
                required
                placeholder="e.g. Byte Knight"
              />
            </label>
          )}
          <label>
            Email address
            <input
              name="email"
              type="email"
              autoComplete="email"
              maxLength={254}
              required
              placeholder="you@example.com"
            />
          </label>
          <label>
            Password
            <input
              name="password"
              type="password"
              autoComplete={register ? "new-password" : "current-password"}
              minLength={register ? 8 : 1}
              maxLength={64}
              required
              placeholder={register ? "At least 8 characters" : "Your password"}
            />
          </label>
          {register && (
            <small>Use 8–64 characters (up to 72 UTF-8 bytes).</small>
          )}
          {(error || serverError) && (
            <p role="alert" className="form-error">
              {error || serverError}
            </p>
          )}
          <button disabled={busy} className="primary-button" type="submit">
            {busy
              ? "Opening the gates…"
              : register
                ? "Create account →"
                : "Sign in →"}
          </button>
          <p className="auth-switch">
            {register ? "Already an adventurer?" : "New to the realm?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Sign in" : "Create an account"}
            </Link>
          </p>
        </form>
        <span className="auth-footnote">
          NO SHORTCUTS. JUST SMALL STEPS FORWARD.
        </span>
      </section>
    </main>
  );
}
