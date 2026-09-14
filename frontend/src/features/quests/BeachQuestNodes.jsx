import { Npc } from "../world-map/PixelScene";

export const beachPreview = [
  {
    id: "first-variable",
    title: "First Variable",
    reward: 50,
    status: "AVAILABLE",
  },
  {
    id: "choose-the-type",
    title: "Choose the Type",
    reward: 50,
    status: "LOCKED",
  },
  {
    id: "operator-training",
    title: "Operator Training",
    reward: 75,
    status: "LOCKED",
  },
];
const positions = [
  { x: 28, y: 38 },
  { x: 72, y: 38 },
  { x: 50, y: 67 },
];

function QuestObject({ chest }) {
  return (
    <svg
      className="npc-sprite"
      viewBox="0 0 40 44"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      {chest ? (
        <>
          <path fill="#5c4130" d="M3 16h34v23H3z" />
          <path fill="#b46d42" d="M5 10h30v24H5z" />
          <path fill="#f1c472" d="M5 19h30v4H5zM8 10h4v24H8zM28 10h4v24h-4z" />
          <path fill="#4c4d38" d="M17 19h6v9h-6z" />
        </>
      ) : (
        <>
          <path fill="#805d40" d="M17 15h6v27h-6z" />
          <path fill="#e3bc72" d="M3 9h34v20H3z" />
          <path fill="#486c4c" d="M7 13h26v12H7z" />
          <path fill="#fce99b" d="M16 1h8v7h-8zM17 16h6v6h-6z" />
        </>
      )}
    </svg>
  );
}

export function BeachQuestNodes({ quests, onSelect, busy }) {
  return quests.map((quest, i) => (
    <button
      key={quest.id}
      className={`npc-node beach-quest-node ${quest.status.toLowerCase()}`}
      style={{ left: `${positions[i].x}%`, top: `${positions[i].y}%` }}
      disabled={busy || quest.status === "LOCKED"}
      onClick={() => onSelect(quest.id)}
      aria-label={`${quest.title}: ${busy ? "loading" : quest.status.toLowerCase()}, ${quest.reward} XP`}
    >
      <span
        className={`quest-marker ${quest.status === "LOCKED" ? "locked-marker" : ""}`}
        aria-hidden="true"
      >
        {quest.status === "COMPLETED"
          ? "✓"
          : quest.status === "LOCKED"
            ? "◆"
            : "!"}
      </span>
      {i === 0 ? <Npc /> : <QuestObject chest={i === 2} />}
      <span className="npc-name">{quest.title}</span>
      <span className="npc-action">
        {quest.status === "COMPLETED"
          ? "COMPLETE · REPLAY"
          : quest.status === "LOCKED"
            ? `FINISH QUEST ${i}`
            : `QUEST ${i + 1} · ${quest.reward} XP`}
      </span>
    </button>
  ));
}
