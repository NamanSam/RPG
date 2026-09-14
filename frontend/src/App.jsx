import { useEffect, useState } from "react";
import { Link, Navigate, Route, Routes } from "react-router-dom";
import { api } from "./services/api";
import { AuthPage } from "./pages/AuthPage";
import { WorldMap } from "./pages/WorldMap";

export function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [serverError, setServerError] = useState("");
  useEffect(() => {
    let active = true;
    api("/auth/me")
      .then((data) => {
        if (active) setUser(data);
      })
      .catch((e) => {
        if (active && e.status !== 401) setServerError(e.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);
  if (loading)
    return (
      <div className="boot-screen">
        <span className="brand-icon">{"{ }"}</span>
        <p>Unfolding the map…</p>
      </div>
    );
  return (
    <Routes>
      <Route
        path="/"
        element={<Navigate to={user ? "/world" : "/preview"} replace />}
      />
      <Route
        path="/preview"
        element={<WorldMap user={user} setUser={setUser} preview />}
      />
      <Route
        path="/world"
        element={
          user ? (
            <WorldMap user={user} setUser={setUser} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/login"
        element={
          user ? (
            <Navigate to="/world" replace />
          ) : (
            <AuthPage onAuth={setUser} serverError={serverError} />
          )
        }
      />
      <Route
        path="/register"
        element={
          user ? (
            <Navigate to="/world" replace />
          ) : (
            <AuthPage register onAuth={setUser} serverError={serverError} />
          )
        }
      />
      <Route
        path="*"
        element={
          <div className="boot-screen">
            <h1>You wandered off the map.</h1>
            <Link to="/">Return to camp →</Link>
          </div>
        }
      />
    </Routes>
  );
}
