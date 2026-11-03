import { Npc } from "../world-map/PixelScene";

const shrines = [
  "Root of Origin",
  "Branching Spire",
  "Whispering Prefix Grove",
  "Labyrinthine Arboretum",
  "Elder Heartwood Sanctum",
];
const runes = ["⛩", "⑂", "⸙", "⌕", "✦"];
const markerX = [29, 52, 73, 58, 31, 48, 71, 55, 30, 50];

export function TrieSanctuaryScene({ trail, onQuestion }) {
  const complete = trail.completed === 10;
  const rune = runes[trail.number - 1];

  return (
    <section
      className={`trie-sanctuary-trail trie-sanctuary-${trail.number} ${trail.unlocked ? "" : "trail-locked"} ${complete ? "trail-complete" : ""}`}
      id={`trie-trail-${trail.number}`}
      aria-label={`${trail.name} trail`}
    >
      <div className="trie-sanctuary-canopy" aria-hidden="true" />
      <div className="trie-sanctuary-roots trie-sanctuary-roots-back" aria-hidden="true" />
      <div className="trie-sanctuary-roots trie-sanctuary-roots-front" aria-hidden="true" />
      <div className="trie-sanctuary-path" aria-hidden="true" />
      <div className="trie-sanctuary-glow-node trie-sanctuary-glow-node-left" aria-hidden="true" />
      <div className="trie-sanctuary-glow-node trie-sanctuary-glow-node-right" aria-hidden="true" />
      <div className="trie-sanctuary-branches" aria-hidden="true">ROOT ─ [p] ─ [r] ─ [e] ─ [f] ─ [i] ─ [x] ✦</div>
      <header className="island-sign trie-sanctuary-sign">
        <small>SANCTUARY TRAIL 0{trail.number} · {trail.completed}/10 RESTORED</small>
        <h2>{trail.name}</h2>
        <p>
          {trail.unlocked
            ? `${shrines[trail.number - 1]} · Traverse the illuminated prefix branches to restore the ancient sanctuary.`
            : "Restore the previous sanctuary trail to open this sacred grove."}
        </p>
      </header>
      <div className="trie-sanctuary-landmark" aria-hidden="true">{rune}</div>
      <div className="island-guide trie-sanctuary-guide" aria-hidden="true">
        <Npc color="#5eead4" />
      </div>
      {trail.questions.map((question, index) => (
        <button
          key={question.id}
          type="button"
          aria-label={`${question.title}, ${question.status.toLowerCase()}, ${question.difficulty}, ${question.xpReward} XP`}
          className={`island-marker trie-sanctuary-marker ${question.status.toLowerCase()}`}
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
          ? trail.number === 5
            ? "✦ ELDER HEARTWOOD MASTER TRIAL COMPLETE"
            : "✓ SANCTUARY TRAIL RESTORED"
          : trail.unlocked
            ? "TRACE THE PREFIX · 4 EASY · 4 MEDIUM · 2 HARD"
            : "◇ SANCTUARY PASSAGE SEALED"}
      </footer>
    </section>
  );
}

