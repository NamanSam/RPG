import { Npc } from "../world-map/PixelScene";

const landmarks = ["⚓", "▤", "◇", "▲", "♜"];
const locations = ["Harbor Entrance", "Character Dock", "Mirror Lagoon", "Pattern Lighthouse", "Harbor Master Tower"];

export function StringHarborScene({ trail, onQuestion }) {
  return <section className={`harbor-trail harbor-${trail.number} ${trail.unlocked ? "" : "trail-locked"}`} aria-label={trail.name} id={`strings-trail-${trail.number}`}>
    <div className="harbor-water" aria-hidden="true"/><div className="harbor-dock" aria-hidden="true"/>
    <div className="harbor-boat boat-one" aria-hidden="true">◢▰◣</div><div className="harbor-boat boat-two" aria-hidden="true">◢▰◣</div>
    <div className="harbor-landmark" aria-hidden="true">{landmarks[trail.number-1]}</div>
    <header className="island-sign harbor-sign"><small>STRING TRAIL 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked ? `${locations[trail.number-1]} · Follow the dock lanterns in order.` : "Complete all ten encounters in the previous harbor trail."}</p></header>
    <div className="island-guide harbor-guide" aria-hidden="true"><Npc theme="village"/></div>
    {trail.questions.map((q,i)=><QuestionMarker key={q.id} question={q} index={i} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10 ? "✓ HARBOR TRAIL RESTORED" : trail.unlocked ? "4 EASY · 4 MEDIUM · 2 HARD" : "◇ FOGBOUND"}</footer>
  </section>;
}
function QuestionMarker({question:q,index,onClick}) {
  const x=[24,51,76,61,31,49,73,57,27,49][index];
  return <button className={`island-marker harbor-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+index*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick} aria-label={`${q.title}: ${q.status.toLowerCase()}, ${q.xpReward} XP`}>
    <span className="island-lantern" aria-hidden="true">{q.status==="COMPLETED" ? "✓" : q.status==="LOCKED" ? "◆" : "!"}</span><span className="island-stone" aria-hidden="true">{String(index+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP{q.status==="COMPLETED" ? " · REPLAY" : ""}</small>
  </button>;
}
