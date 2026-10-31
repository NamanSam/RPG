import { Npc } from "../world-map/PixelScene";

const landmarks = [
  "Crumbling Archive",
  "Hall of Echoes",
  "Rootbound Library",
  "Divide Gate",
  "Final Oracle",
];
const runes = ["⌘", "↻", "⌁", "◈", "✦"];
const markerX = [28, 52, 73, 58, 31, 47, 71, 56, 30, 51];

export function RecursionRuinsScene({ trail, onQuestion }) {
  const complete = trail.completed === 10;

  return (
    <section
      className={`recursion-trail recursion-${trail.number} ${trail.unlocked ? "" : "trail-locked"} ${complete ? "trail-complete" : ""}`}
      id={`recursion-trail-${trail.number}`}
      aria-label={`${trail.name} trail`}
    >
      <div className="recursion-night" aria-hidden="true" />
      <div className="recursion-forest recursion-forest-back" aria-hidden="true" />
      <div className="recursion-forest recursion-forest-front" aria-hidden="true" />
      <div className="recursion-stairway" aria-hidden="true" />
      <div className="recursion-arch" aria-hidden="true">⌂</div>
      <div className="recursion-vines" aria-hidden="true">〰　〰　〰</div>
      <header className="island-sign recursion-sign">
        <small>RUIN TRAIL 0{trail.number} · {trail.completed}/10 RESTORED</small>
        <h2>{trail.name}</h2>
        <p>
          {trail.unlocked
            ? `${landmarks[trail.number - 1]} · Follow each rune to the heart of the ruins.`
            : "Restore the previous trail to wake these ancient stones."}
        </p>
      </header>
      <div className="recursion-monument" aria-hidden="true">{runes[trail.number - 1]}</div>
      <div className="island-guide recursion-guide" aria-hidden="true">
        <Npc color="#a873c5" />
      </div>
      {trail.questions.map((question, index) => (
        <button
          key={question.id}
          type="button"
          aria-label={`${question.title}, ${question.status.toLowerCase()}, ${question.difficulty}, ${question.xpReward} XP`}
          className={`island-marker recursion-marker ${question.status.toLowerCase()}`}
          style={{ left: `${markerX[index]}%`, top: `${235 + index * 91}px` }}
          disabled={question.status === "LOCKED"}
          onClick={() => onQuestion(question.id)}
        >
          <span className="island-lantern" aria-hidden="true">
            {question.status === "COMPLETED" ? "✓" : question.status === "LOCKED" ? "◆" : "!"}
          </span>
          <span className="island-stone" aria-hidden="true">
            {runes[trail.number - 1]} {String(index + 1).padStart(2, "0")}
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
          ? trail.number === 5 ? "✦ MASTER TRIAL COMPLETE" : "✓ TRAIL RESTORED"
          : trail.unlocked ? "EXPLORE · 4 EASY · 4 MEDIUM · 2 HARD" : "◇ TRAIL SEALED"}
      </footer>
    </section>
  );
}
