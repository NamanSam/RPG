import { Npc } from "../world-map/PixelScene";
import "./village.css";

export const villagePreview = [
  {
    id: "the-gatekeeper",
    title: "The Gatekeeper",
    reward: 75,
    status: "LOCKED",
  },
  {
    id: "the-looping-mill",
    title: "The Looping Mill",
    reward: 100,
    status: "LOCKED",
  },
  {
    id: "the-endless-well",
    title: "The Endless Well",
    reward: 125,
    status: "LOCKED",
  },
];
const positions = [
  { x: 47, y: 29 },
  { x: 25, y: 57 },
  { x: 74, y: 57 },
];

export function MillSprite({ badge = false }) {
  return (
    <svg
      className={badge ? "village-badge-sprite" : "village-object"}
      viewBox="0 0 96 96"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <path fill="#4f6042" opacity=".3" d="M10 87h78v7H10z" />
      <path fill="#e5c38a" d="M24 35h49v53H24z" />
      <path
        fill="#987044"
        d="M24 77h49v11H24zM30 58h9v10h-9zM59 58h9v10h-9zM43 66h13v22H43z"
      />
      <path fill="#a65743" d="M17 35h64v-8H69v-8H57v-8H40v8H28v8H17z" />
      <path fill="#f1d295" d="M31 60h6v5h-6zM60 60h6v5h-6z" />
      <g className={badge ? "" : "mill-sails"}>
        <path fill="#684c38" d="M45 2h6v73h-6zM12 35h73v6H12z" />
        <path
          fill="#f6dfaa"
          d="M34 3h11v27H34zM51 46h11v27H51zM13 41h26v11H13zM57 24h26v11H57z"
        />
        <path
          fill="#ae8c59"
          d="M34 10h11v3H34zM34 21h11v3H34zM51 53h11v3H51zM51 64h11v3H51zM20 41h3v11h-3zM31 41h3v11h-3zM64 24h3v11h-3zM75 24h3v11h-3z"
        />
      </g>
      <path fill="#77533c" d="M43 33h10v10H43z" />
      <path fill="#e9bb67" d="M46 36h4v4h-4z" />
    </svg>
  );
}
function WellSprite() {
  return (
    <svg
      className="village-object"
      viewBox="0 0 96 96"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <path fill="#506346" opacity=".3" d="M11 86h77v7H11z" />
      <path fill="#805835" d="M20 24h7v58h-7zM70 24h7v58h-7zM25 40h46v6H25z" />
      <path fill="#b76443" d="M12 25h72v-8H72V9H24v8H12z" />
      <path fill="#e2ad68" d="M24 12h47v4H24z" />
      <path fill="#d0c3a2" d="M14 64h68v22H14zM21 58h55v6H21z" />
      <path
        fill="#7f867a"
        d="M14 75h68v3H14zM28 64h3v11h-3zM54 64h3v11h-3zM42 78h3v8h-3zM68 78h3v8h-3z"
      />
      <path fill="#345f63" d="M24 61h48v6H24z" />
      <path
        className="well-shimmer"
        fill="#9bd2bd"
        d="M29 63h15v2H29zM54 62h12v2H54z"
      />
      <path fill="#e7c387" d="M47 43h2v16h-2z" />
      <path fill="#936943" d="M41 51h14v10H41z" />
      <path fill="#efce87" d="M41 52h14v2H41z" />
    </svg>
  );
}
function GateSprite() {
  return (
    <span className="village-gate">
      <svg viewBox="0 0 96 96" shapeRendering="crispEdges" aria-hidden="true">
        <path fill="#a98553" d="M7 23h11v63H7zM78 23h11v63H78zM7 25h82v9H7z" />
        <path fill="#594b35" d="M7 79h11v7H7zM78 79h11v7H78z" />
        <path fill="#416948" d="M27 10h43v20H27z" />
        <path fill="#f1d28c" d="M32 14h33v3H32zM41 20h15v3H41z" />
        <path
          fill="#d6b174"
          d="M0 53h23v6H0zM73 53h23v6H73zM0 68h23v6H0zM73 68h23v6H73z"
        />
      </svg>
      <Npc color="#68869a" />
    </span>
  );
}

export function VillageQuestNodes({ quests, worldUnlocked, busy, onSelect }) {
  return (
    <>
      <svg
        className="village-paths"
        viewBox="0 0 800 480"
        preserveAspectRatio="none"
        aria-hidden="true"
      >
        <path
          d="M376 130V235H200V328M376 235H592V326"
          fill="none"
          stroke="#8b704c"
          strokeWidth="23"
        />
        <path
          d="M376 130V235H200V328M376 235H592V326"
          fill="none"
          stroke="#d7b581"
          strokeWidth="19"
          strokeDasharray="4 3"
        />
      </svg>
      {quests.map((quest, i) => (
        <button
          key={quest.id}
          className={`npc-node village-quest-node ${quest.status.toLowerCase()}`}
          style={{ left: `${positions[i].x}%`, top: `${positions[i].y}%` }}
          disabled={busy || !worldUnlocked || quest.status === "LOCKED"}
          onClick={() => onSelect(quest.id)}
          aria-label={`${quest.title}: ${busy ? "loading" : !worldUnlocked ? "locked" : quest.status.toLowerCase()}, ${quest.reward} XP`}
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
          {i === 0 ? <GateSprite /> : i === 1 ? <MillSprite /> : <WellSprite />}
          <span className="npc-name">{quest.title}</span>
          <span className="npc-action">
            {quest.status === "COMPLETED"
              ? "COMPLETE · REPLAY"
              : !worldUnlocked
                ? "FINISH BEGINNER BEACH"
                : quest.status === "LOCKED"
                  ? `FINISH VILLAGE QUEST ${i}`
                  : `QUEST ${i + 1} · ${quest.reward} XP`}
          </span>
        </button>
      ))}
    </>
  );
}
