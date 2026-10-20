import { Npc } from "../world-map/PixelScene";
import "./kingdom.css";
const guides = ["Scroll Keeper", "Key Keeper", "Keeper of Sets"];
const locations = ["Royal Library", "Registry Hall", "Crown Chamber"];
export const kingdomPreview = [
  {
    id: "the-dynamic-scroll",
    title: "The Dynamic Scroll",
    difficulty: "Easy",
    reward: 150,
    status: "LOCKED",
  },
  {
    id: "the-key-keeper",
    title: "The Key Keeper",
    difficulty: "Medium",
    reward: 175,
    status: "LOCKED",
  },
  {
    id: "hall-of-uniques",
    title: "Hall of Uniques",
    difficulty: "Medium",
    reward: 200,
    status: "LOCKED",
  },
];
export function KingdomQuestNodes({ quests, busy, onSelect }) {
  const positions = [
    [22, 59],
    [50, 43],
    [78, 59],
  ];
  return (
    <>
      {quests.map((q, i) => (
        <button
          key={q.id}
          className={`npc-node kingdom-node ${q.status.toLowerCase()}`}
          style={{ left: `${positions[i][0]}%`, top: `${positions[i][1]}%` }}
          disabled={busy || q.status === "LOCKED"}
          onClick={() => onSelect(q.id)}
          aria-label={`${guides[i]}, ${locations[i]}, ${q.title}: ${q.status.toLowerCase()}, ${q.reward} XP`}
        >
          <span className="quest-marker" aria-hidden="true">
            {q.status === "COMPLETED" ? "✓" : q.status === "LOCKED" ? "◆" : "!"}
          </span>
          <Npc color={["#537b9b", "#a58a47", "#9c637c"][i]} />
          <span className="npc-name">{guides[i]}</span>
          <span className="kingdom-location">{locations[i]}</span>
          <span className="npc-action">
            {q.title}
            <br />
            {q.status === "COMPLETED"
              ? "COMPLETE · REPLAY"
              : q.status === "LOCKED"
                ? "LOCKED"
                : `${q.difficulty} · ${q.reward} XP`}
          </span>
        </button>
      ))}
    </>
  );
}
export function KingdomScene() {
  return (
    <svg
      className="pixel-scene kingdom"
      viewBox="0 0 800 480"
      preserveAspectRatio="none"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <rect width="800" height="480" fill="#718b76" />
      <path fill="#8fa282" d="M0 270h800v210H0z" />
      <path
        fill="#b9b4a0"
        d="M365 230h70v100h230v36H435v114h-70V366H130v-36h235z"
      />
      <path
        fill="#606b70"
        d="M255 80h290v195H255zM200 50h75v240h-75zM525 50h75v240h-75z"
      />
      <path
        fill="#a2aa9c"
        d="M210 75h55v200h-55zM535 75h55v200h-55zM275 100h250v160H275z"
      />
      <path
        fill="#d1d0b3"
        d="M195 50h85v30h-85zM520 50h85v30h-85zM285 80h230v28H285zM195 35h18v25h-18zM226 35h18v25h-18zM260 35h20v25h-20zM520 35h18v25h-18zM552 35h18v25h-18zM587 35h18v25h-18z"
      />
      <path
        fill="#3f5358"
        d="M372 197h56v80h-56zM304 135h24v35h-24zM472 135h24v35h-24z"
      />
      <g className="royal-banners" fill="#a95c63">
        <path d="M221 100h30v65l-15-10-15 10zM549 100h30v65l-15-10-15 10z" />
      </g>
      <path
        fill="#e6ca75"
        d="M232 112h8v30h-8zM560 112h8v30h-8zM384 125h32v15h-32zM380 116h8v15h-8zM396 112h8v19h-8zM412 116h8v15h-8z"
      />
      {[
        [95, 240],
        [585, 240],
      ].map(([x, y], i) => (
        <g key={i} transform={`translate(${x} ${y})`}>
          <path fill="#626d73" d="M0 0h120v90H0z" />
          <path fill="#bdc2ae" d="M7 8h106v75H7z" />
          <path
            fill="#5d5361"
            d="M-8 0h136v-12h-10v-12H10v12H-8zM45 35h30v55H45z"
          />
          <path fill="#d8bd7e" d="M14 20h22v32H14zM85 20h22v32H85z" />
          <path stroke="#7b6857" strokeWidth="4" d="M15 30h20M85 30h20" />
        </g>
      ))}
      <g fill="#547661">
        <path d="M30 400h80v30H30zM690 400h80v30h-80zM30 190h70v20H30zM700 190h70v20h-70z" />
      </g>
      <g className="kingdom-glints" fill="#ffe6a3">
        <path d="M65 370h5v5h-5zM720 365h5v5h-5zM340 290h5v5h-5zM460 300h5v5h-5z" />
      </g>
    </svg>
  );
}
