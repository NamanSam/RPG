import { useEffect, useRef, useState } from "react";
import { api } from "../../services/api";
import { Npc } from "../world-map/PixelScene";
import { QuestionPanel } from "../questions/QuestionPanel";
import "./practice.css";

const sites = [
  {
    name: "Classes & Objects",
    place: "Architect Workshop",
    guide: "Master Architect",
    gate: "Complete Blueprint Maker",
    x: 19,
    y: 32,
  },
  {
    name: "Encapsulation",
    place: "Sealed Vault",
    guide: "Vault Keeper",
    gate: "Complete The Hidden Vault",
    x: 49,
    y: 23,
  },
  {
    name: "Inheritance",
    place: "Ancestral Ruins",
    guide: "Ancestral Sage",
    gate: "Complete Bloodline of Classes",
    x: 79,
    y: 32,
  },
  {
    name: "Polymorphism",
    place: "Shifting Shrine",
    guide: "Form Weaver",
    gate: "Complete Many Forms",
    x: 31,
    y: 68,
  },
  {
    name: "OOP Master Trial",
    place: "Canyon Summit",
    guide: "Summit Architect",
    gate: "Complete the first four practice trails",
    x: 68,
    y: 68,
  },
];

export function OopPractice({ storyCompleted, preview }) {
  const [progress, setProgress] = useState(null),
    [error, setError] = useState("");
  const [trail, setTrail] = useState(null),
    [question, setQuestion] = useState(null);
  const [pending, setPending] = useState(null),
    [celebration, setCelebration] = useState(null);
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    if (preview) return;
    let active = true;
    api("/topics/oop-canyon-practice/progress")
      .then((p) => {
        if (active) {
          setProgress(p);
          setError("");
        }
      })
      .catch((e) => {
        if (active) setError(e.message);
      });
    return () => {
      active = false;
    };
  }, [preview, storyCompleted, retry]);
  useEffect(() => {
    const refresh = () => {
      if (!question && !celebration) setRetry((r) => r + 1);
    };
    window.addEventListener("focus", refresh);
    return () => window.removeEventListener("focus", refresh);
  }, [question, celebration]);
  function result(r) {
    setProgress(r.progress);
    if (r.trailCompletedNow || r.topicCompletedNow) setPending(r);
  }
  function closeQuestion() {
    setQuestion(null);
    if (pending) {
      setCelebration(pending);
      setPending(null);
    }
  }
  const current = progress?.trails.find((t) => t.number === trail);
  return (
    <section className="beach-training oop-training" aria-labelledby="oop-canyon-practice-title">
      <header className="practice-caption">
        <p className="eyebrow">OOP CANYON · PRACTICE TRAILS</p>
        <h2 id="oop-canyon-practice-title">The canyon mastery paths</h2>
        <p>Optional paths to mastery. Your story continues at your own pace.</p>
        <p className="mastery-progress" aria-live="polite">
          {!preview && !progress ? (
            "Loading mastery progress…"
          ) : (
            <>
              OOP Canyon Mastery · {progress?.completed || 0} / 50{" "}
              <span>· {progress?.topicXp || 0} practice XP</span>
            </>
          )}
        </p>
        {progress?.badge && (
          <p className="practice-badge">⚑ OOP CANYON MASTERY BADGE</p>
        )}
        {preview && (
          <p>
            Sign in to save practice. OOP story quests open the training locations.
          </p>
        )}
        {error && (
          <p role="alert">
            {error}{" "}
            <button
              className="text-button"
              onClick={() => setRetry((r) => r + 1)}
            >
              Retry training progress
            </button>
          </p>
        )}
      </header>
      <div className="training-sands">
        <svg
          className="training-geography"
          viewBox="0 0 800 520"
          preserveAspectRatio="none"
          shapeRendering="crispEdges"
          aria-hidden="true"
        >
          <rect width="800" height="520" fill="#8f5f49" />
          <path fill="#563b3b" d="M0 420h120v30h150v25h170v25h360v20H0z" />
          <path
            fill="none"
            stroke="#d0a16d"
            strokeWidth="24"
            strokeDasharray="16 7"
            d="M150 170L390 120L630 170L540 350L250 350L150 170"
          />
          {[
            [100, 140],
            [340, 90],
            [580, 140],
            [200, 330],
          ].map(([x, y], i) => (
            <g key={i} transform={`translate(${x} ${y})`}>
              <path fill="#6d5148" d="M0 10h100v55H0z" />
              <path
                fill={["#755b67", "#8c694e", "#66556f", "#9e694f"][i]}
                d="M-12 10L50-38l62 48z"
              />
              <path fill="#e5d2a1" d="M40 24h22v41H40z" />
            </g>
          ))}
          <g transform="translate(540 335)">
            <path fill="#7c8270" d="M-50 30h100v15h-100zM-35-20h70v50h-70z" />
            <path fill="#b4b49a" d="M-43-30h86v15h-86z" />
            <path
              className="practice-rune"
              fill="#ffe19a"
              d="M-5-10h10v10h10v10H5v10H-5V10h-10V0h10z"
            />
          </g>
          <path
            fill="#917748"
            d="M0 470h220v15H0zM20 440h12v65H20zM90 440h12v65H90zM160 440h12v65h-12z"
          />
        </svg>
        {sites.map((site, i) => {
          const t = progress?.trails[i];
          return (
            <button
              key={site.name}
              className={`training-location ${t?.unlocked ? "open" : "locked"}`}
              style={{ left: `${site.x}%`, top: `${site.y}%` }}
              disabled={preview || !t?.unlocked}
              onClick={() => setTrail(i + 1)}
              aria-label={`${site.name} practice trail: ${t?.unlocked ? "unlocked" : "locked"}, ${t?.completed || 0} of 10 complete`}
            >
              <span className="quest-marker" aria-hidden="true">
                {t?.completed === 10 ? "✓" : t?.unlocked ? "!" : "◇"}
              </span>
              <Npc
                color={
                  ["#7c9b63", "#8a79a1", "#b07649", "#548d9c", "#a38156"][i]
                }
              />
              <strong>{site.name}</strong>
              <span>{site.place}</span>
              <span>{t?.completed || 0} / 10</span>
              {!t?.unlocked && <small>{site.gate}</small>}
            </button>
          );
        })}
      </div>
      {current && !question && !celebration && (
        <TrailJournal
          trail={current}
          site={sites[trail - 1]}
          onClose={() => setTrail(null)}
          onQuestion={setQuestion}
        />
      )}
      {question && (
        <QuestionPanel
          key={question}
          id={question}
          onClose={closeQuestion}
          onResult={result}
        />
      )}
      {celebration && (
        <PracticeCompletion
          result={celebration}
          onClose={() => {
            setCelebration(null);
            setTrail(null);
          }}
        />
      )}
    </section>
  );
}
function TrainingDialog({ children, onClose, label }) {
  const ref = useRef(null);
  useEffect(() => {
    const d = ref.current;
    d.showModal();
    return () => d.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className="guide-dialog training-dialog"
      aria-label={label}
      onCancel={onClose}
    >
      <button
        className="dialog-close"
        aria-label="Close training journal"
        onClick={onClose}
      >
        ×
      </button>
      {children}
    </dialog>
  );
}
function TrailJournal({ trail, site, onClose, onQuestion }) {
  return (
    <TrainingDialog onClose={onClose} label={trail.name}>
      <p className="eyebrow">
        {site.guide} · {site.place}
      </p>
      <h2>{trail.name}</h2>
      <p>
        {trail.completed} / 10 · Follow the markers in order. Completed
        encounters are open for practice.
      </p>
      <ol className="training-route">
        {trail.questions.map((q, i) => (
          <li key={q.id} className={q.status.toLowerCase()}>
            <button
              disabled={q.status === "LOCKED"}
              onClick={() => onQuestion(q.id)}
              aria-label={`${q.title}: ${q.status.toLowerCase()}`}
            >
              <span aria-hidden="true">
                {q.status === "COMPLETED"
                  ? "✓"
                  : q.status === "LOCKED"
                    ? "◇"
                    : i + 1}
              </span>
              <strong>{q.title}</strong>
              <small>
                {q.difficulty} · {q.xpReward} practice XP
              </small>
            </button>
          </li>
        ))}
      </ol>
    </TrainingDialog>
  );
}
function PracticeCompletion({ result, onClose }) {
  return (
    <TrainingDialog
      onClose={onClose}
      label={
        result.topicCompletedNow
          ? "Beginner Beach mastery complete"
          : "Training trail complete"
      }
    >
      <div className="practice-emblem" aria-hidden="true">
        ✦
      </div>
      <p className="eyebrow">THE FOREST SHRINE AWAKENS</p>
      <h2>
        {result.topicCompletedNow
          ? "OOP CANYON MASTERY BADGE"
          : "PRACTICE TRAIL COMPLETE"}
      </h2>
      <p>
        {result.topicCompletedNow
          ? "All 50 OOP encounters mastered. Your OOP Canyon Mastery badge is saved."
          : "Ten encounters restored. This training location now bears your mark."}
      </p>
      <p>
        {result.progress.completed} / 50 · {result.progress.topicXp} practice XP
      </p>
      <p>Story quests and region unlocks continue independently.</p>
      <button className="primary-button" onClick={onClose}>
        Return to the canyon →
      </button>
    </TrainingDialog>
  );
}



