import { Npc } from "../world-map/PixelScene";

const chambers = [
  "Echo Geode",
  "Shiftstone Shaft",
  "XOR Crystal Grotto",
  "Maskwright Hall",
  "Obsidian Core",
];
const runes = ["◈", "⇧", "⊕", "▦", "✦"];
const markerX = [28, 51, 73, 59, 31, 47, 70, 55, 30, 51];

export function BitwiseCavernsScene({ trail, onQuestion }) {
  const complete = trail.completed === 10;
  const rune = runes[trail.number - 1];

  return (
    <section
      className={`bitwise-cavern-trail bitwise-cavern-${trail.number} ${trail.unlocked ? "" : "trail-locked"} ${complete ? "trail-complete" : ""}`}
      id={`bitwise-trail-${trail.number}`}
      aria-label={`${trail.name} trail`}
    >
      <div className="bitwise-cave-sky" aria-hidden="true" />
      <div className="bitwise-cave-wall bitwise-cave-wall-back" aria-hidden="true" />
      <div className="bitwise-cave-wall bitwise-cave-wall-front" aria-hidden="true" />
      <div className="bitwise-crystal-vein bitwise-crystal-vein-left" aria-hidden="true" />
      <div className="bitwise-crystal-vein bitwise-crystal-vein-right" aria-hidden="true" />
      <div className="bitwise-cave-path" aria-hidden="true" />
      <div className="bitwise-cave-runes" aria-hidden="true">0101　⊕　1010　◇　0011</div>
      <header className="island-sign bitwise-cavern-sign">
        <small>CAVERN DESCENT 0{trail.number} · {trail.completed}/10 SOLVED</small>
        <h2>{trail.name}</h2>
        <p>
          {trail.unlocked
            ? `${chambers[trail.number - 1]} · Follow the glowing bit marks through the crystal passage.`
            : "Clear the previous cavern trail to light this passage."}
        </p>
      </header>
      <div className="bitwise-cavern-landmark" aria-hidden="true">{rune}</div>
      <div className="island-guide bitwise-cavern-guide" aria-hidden="true">
        <Npc color="#62d9c0" />
      </div>
      {trail.questions.map((question, index) => (
        <button
          key={question.id}
          type="button"
          aria-label={`${question.title}, ${question.status.toLowerCase()}, ${question.difficulty}, ${question.xpReward} XP`}
          className={`island-marker bitwise-cavern-marker ${question.status.toLowerCase()}`}
          style={{ left: `${markerX[index]}%`, top: `${235 + index * 91}px` }}
          disabled={question.status === "LOCKED"}
          onClick={() => onQuestion(question.id)}
        >
          <span className="island-lantern" aria-hidden="true">
            {question.status === "COMPLETED" ? "✓" : question.status === "LOCKED" ? "◆" : "!"}
          </span>
          <span className="island-stone" aria-hidden="true">
            {rune} {String(index + 1).padStart(2, "0")}
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
          ? trail.number === 5 ? "✦ OBSIDIAN CORE MASTER TRIAL COMPLETE" : "✓ CAVERN TRAIL CLEARED"
          : trail.unlocked ? "TRACE THE BITS · 4 EASY · 4 MEDIUM · 2 HARD" : "◇ CAVERN PASSAGE SEALED"}
      </footer>
    </section>
  );
}
