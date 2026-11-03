import { Npc } from "../world-map/PixelScene";

const chambers = [
  "The Forking Gate",
  "Subterranean Gallery",
  "Hall of Turning Paths",
  "The Constraint Vault",
  "Mazeheart Sanctum",
];
const emblems = ["⌘", "↗", "✣", "▦", "✧"];
const markerX = [29, 51, 72, 59, 32, 48, 70, 55, 31, 50];

export function BacktrackingMazeScene({ trail, onQuestion }) {
  const complete = trail.completed === 10;

  return (
    <section
      className={`backtracking-trail backtracking-${trail.number} ${trail.unlocked ? "" : "trail-locked"} ${complete ? "trail-complete" : ""}`}
      id={`backtracking-trail-${trail.number}`}
      aria-label={`${trail.name} trail`}
    >
      <div className="maze-gloom" aria-hidden="true" />
      <div className="maze-wall maze-wall-back" aria-hidden="true" />
      <div className="maze-wall maze-wall-front" aria-hidden="true" />
      <div className="maze-path" aria-hidden="true" />
      <div className="maze-branches" aria-hidden="true">╱　╲　╱　╲</div>
      <div className="maze-gate" aria-hidden="true">▥</div>
      <header className="island-sign backtracking-sign">
        <small>MAZE ROUTE 0{trail.number} · {trail.completed}/10 SOLVED</small>
        <h2>{trail.name}</h2>
        <p>
          {trail.unlocked
            ? `${chambers[trail.number - 1]} · Choose a passage, explore it, then retrace a dead end.`
            : "Complete the previous route to unseal this maze branch."}
        </p>
      </header>
      <div className="maze-landmark" aria-hidden="true">{emblems[trail.number - 1]}</div>
      <div className="maze-torch maze-torch-left" aria-hidden="true">♨</div>
      <div className="maze-torch maze-torch-right" aria-hidden="true">♨</div>
      <div className="island-guide maze-guide" aria-hidden="true">
        <Npc color="#71c49a" />
      </div>
      {trail.questions.map((question, index) => (
        <button
          key={question.id}
          type="button"
          aria-label={`${question.title}, ${question.status.toLowerCase()}, ${question.difficulty}, ${question.xpReward} XP`}
          className={`island-marker maze-marker ${question.status.toLowerCase()}`}
          style={{ left: `${markerX[index]}%`, top: `${235 + index * 91}px` }}
          disabled={question.status === "LOCKED"}
          onClick={() => onQuestion(question.id)}
        >
          <span className="island-lantern" aria-hidden="true">
            {question.status === "COMPLETED" ? "✓" : question.status === "LOCKED" ? "◆" : "!"}
          </span>
          <span className="island-stone" aria-hidden="true">
            {emblems[trail.number - 1]} {String(index + 1).padStart(2, "0")}
          </span>
          <span className="island-marker-title">{question.title}</span>
          <small>
            {question.difficulty} · {question.xpReward} XP
            {question.status === "COMPLETED" ? " · REPLAY" : ""}
          </small>
        </button>
      ))}
      <footer>
        {complete
          ? trail.number === 5 ? "✧ MAZEHEART MASTER TRIAL COMPLETE" : "✓ ROUTE MAPPED"
          : trail.unlocked ? "CHOOSE · EXPLORE · BACKTRACK · 4 EASY · 4 MEDIUM · 2 HARD" : "◇ PASSAGE SEALED"}
      </footer>
    </section>
  );
}
