import { Npc } from "../world-map/PixelScene";
import "./forest.css";

export const forestPreview = [
  { id: "the-lost-index", title: "The Lost Index", reward: 100, status: "LOCKED" },
  { id: "forest-traversal", title: "Forest Traversal", reward: 125, status: "LOCKED" },
  { id: "hidden-maximum", title: "Hidden Maximum", reward: 150, status: "LOCKED" },
];

export function ForestQuestNodes({ quests, busy, onSelect }) {
  const positions = [[40, 32], [25, 65], [73, 65]];
  return <>{quests.map((quest, index) => (
    <button key={quest.id} className={`npc-node forest-node ${quest.status.toLowerCase()}`}
      style={{ left: `${positions[index][0]}%`, top: `${positions[index][1]}%` }}
      disabled={busy || quest.status === "LOCKED"} onClick={() => onSelect(quest.id)}
      aria-label={`${quest.title}: ${quest.status.toLowerCase()}, ${quest.reward} XP`}>
      <span className={`quest-marker ${quest.status === "LOCKED" ? "locked-marker" : ""}`} aria-hidden="true">
        {quest.status === "COMPLETED" ? "✓" : quest.status === "LOCKED" ? "◆" : "!"}
      </span>
      {index === 0 ? <Npc color="#759554" /> : index === 1 ? <ForestSign /> : <ForestShrine />}
      <span className="npc-name">{quest.title}</span>
      <span className="npc-action">{quest.status === "COMPLETED" ? "COMPLETE · REPLAY" : quest.status === "LOCKED" ? "FINISH PREVIOUS QUEST" : `${quest.difficulty} · ${quest.reward} XP`}</span>
    </button>
  ))}</>;
}

function ForestSign() {
  return <svg className="npc-sprite forest-relic" viewBox="0 0 48 48" shapeRendering="crispEdges" aria-hidden="true">
    <path fill="#5b3b28" d="M21 20h7v27h-7zM5 8h38v24H5z"/><path fill="#b47b45" d="M8 11h32v17H8z"/>
    <path fill="#f1d39a" d="M13 15h21v4H13zM13 22h14v3H13z"/><path fill="#447347" d="M2 39h44v8H2z"/>
  </svg>;
}

export function ForestShrine() {
  return <svg className="npc-sprite forest-relic shrine" viewBox="0 0 48 48" shapeRendering="crispEdges" aria-hidden="true">
    <path fill="#3c6c44" d="M2 32h9v15H2zM37 27h9v20h-9z"/><path fill="#686e55" d="M5 39h38v7H5zM9 14h30v26H9z"/>
    <path fill="#afb993" d="M6 9h36v7H6zM14 18h20v19H14z"/><path className="shrine-rune" fill="#ffe399" d="M21 22h6v4h4v6h-4v4h-6v-4h-4v-6h4z"/>
  </svg>;
}
