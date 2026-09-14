import { useEffect, useRef, useState } from "react";
import { api } from "../../services/api";

export function QuestDialog({ id, onClose, onResult, onExpired }) {
  const dialog = useRef(null);
  const feedback = useRef(null);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const el = dialog.current;
    el.showModal();
    return () => el.close();
  }, []);
  useEffect(() => {
    let active = true;
    api(`/quests/${id}`)
      .then((value) => {
        if (active) setData(value);
      })
      .catch((e) => {
        if (!active) return;
        if (e.status === 401) onExpired();
        else setError(e.message);
      });
    return () => {
      active = false;
    };
  }, [id, onExpired]);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setResult(null);
    try {
      const next = await api(`/quests/${id}/submit`, {
        method: "POST",
        body: JSON.stringify({ answer }),
      });
      setResult(next);
      setAttempt((n) => n + 1);
      onResult(next);
    } catch (e) {
      if (e.status === 401) onExpired();
      else setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  useEffect(() => {
    if (result) feedback.current?.focus();
  }, [result]);
  return (
    <dialog
      ref={dialog}
      className="guide-dialog quest-dialog"
      aria-labelledby="quest-title"
      onCancel={(e) => {
        if (busy) e.preventDefault();
        else onClose();
      }}
    >
      <button
        className="dialog-close"
        aria-label="Close quest"
        disabled={busy}
        onClick={onClose}
      >
        ×
      </button>
      {data ? (
        <>
          <p className="eyebrow">
            BEGINNER BEACH · {data.quest.difficulty.toUpperCase()}
          </p>
          <h2 id="quest-title">{data.quest.title}</h2>
          <div className="quest-meta">
            <span>{data.quest.topic}</span>
            <strong>{data.quest.reward} XP · first completion</strong>
          </div>
          {(data.quest.status === "COMPLETED" || result?.correct) && (
            <p className="replay-note">
              ✓ Quest complete. You can practice again; XP is awarded only once.
            </p>
          )}
          <p className="quest-story">“{data.story}”</p>
          <p className="quest-lesson">{data.lesson}</p>
          <form onSubmit={submit}>
            <label className="quest-task" htmlFor="quest-answer">
              {data.task}
            </label>
            <pre className="quest-code">
              <code>{data.starterCode}</code>
            </pre>
            {data.kind === "TYPE_CHOICE" ? (
              <select
                id="quest-answer"
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                required
                disabled={busy}
              >
                <option value="">Choose a Java type…</option>
                {data.options.map((option) => (
                  <option key={option}>{option}</option>
                ))}
              </select>
            ) : (
              <input
                id="quest-answer"
                aria-label="Your answer"
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                maxLength={80}
                autoComplete="off"
                spellCheck={false}
                placeholder="Enter the whole-number value"
                required
                disabled={busy}
              />
            )}
            <details className="quest-hint">
              <summary>Ask your guide for a hint</summary>
              <p>{data.hint}</p>
            </details>
            <button
              className="primary-button"
              type="submit"
              disabled={busy || !answer.trim()}
            >
              {busy
                ? "Checking your answer…"
                : result?.correct
                  ? "Practice again →"
                  : "Submit answer →"}
            </button>
          </form>
          {result && (
            <div
              ref={feedback}
              tabIndex={-1}
              key={attempt}
              role="status"
              className={`quest-feedback ${result.correct ? "correct" : "incorrect"}`}
            >
              <strong>
                {result.correct ? "✦ Correct!" : "Not quite — try again"}
              </strong>
              <p>{result.feedback}</p>
              {result.awardedXp > 0 ? (
                <span className="xp-gain">+{result.awardedXp} XP</span>
              ) : (
                <span>
                  +0 XP{result.correct ? " · already earned" : " · keep trying"}
                </span>
              )}
              {result.correct && (
                <button className="primary-button" onClick={onClose}>
                  {result.beachCompletedNow
                    ? "Celebrate your adventure →"
                    : "Return to the map →"}
                </button>
              )}
            </div>
          )}
        </>
      ) : (
        <>
          <h2 id="quest-title">Opening your quest…</h2>
          <p>Unrolling Captain Byte’s instructions.</p>
        </>
      )}
      {error && (
        <p className="form-error" role="alert">
          {error} Close this panel and try again.
        </p>
      )}
    </dialog>
  );
}
