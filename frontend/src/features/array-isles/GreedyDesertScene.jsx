import { Npc } from "../world-map/PixelScene";

const places=["Desert Camp","Merchant Route","Scheduling Oasis","Golden Ruins","Greedy Temple"];
const crests=["◒","¤","⌚","◆","♜"];

export function GreedyDesertScene({trail,onQuestion}) {
  return <section className={`desert-trail desert-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`greedy-trail-${trail.number}`} aria-label={trail.name}>
    <div className="desert-dunes"/><div className="desert-route"/><div className="desert-sun">☀</div><div className="desert-caravan">♞ · ♞ · ♞</div>
    <header className="island-sign desert-sign"><small>DESERT ROUTE 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${places[trail.number-1]} · Choose each safe route in order.`:"Complete the previous route to clear the sandstorm."}</p></header>
    <div className="island-guide desert-guide"><Npc theme="canyon"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} crest={crests[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ ROUTE SECURED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ SANDSTORM BLOCKED"}</footer>
  </section>;
}

function Marker({q,i,crest,onClick}) {
  const x=[28,52,73,58,31,47,71,56,30,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker desert-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{crest} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
