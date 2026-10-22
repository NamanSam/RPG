import { Npc } from "../world-map/PixelScene";

const places=["Base Camp","BFS/DFS Crossing","Connected Caverns","Pathfinder Summit","Graph Oracle Peak"];
const crests=["●—●","⇄","⬡","△","✧"];

export function GraphMountainsScene({trail,onQuestion}) {
  return <section className={`mountain-trail mountain-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`graph-trail-${trail.number}`} aria-label={trail.name}>
    <div className="mountain-range"/><div className="mountain-routes"/><div className="mountain-peak">▲</div><div className="mountain-bridge">●══●══●</div>
    <header className="island-sign mountain-sign"><small>ASCENT 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${places[trail.number-1]} · Connect each glowing route in order.`:"Complete the lower ascent to reveal this route."}</p></header>
    <div className="island-guide mountain-guide"><Npc theme="canyon"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} crest={crests[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ ROUTE CONNECTED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ PASS BLOCKED"}</footer>
  </section>;
}

function Marker({q,i,crest,onClick}) {
  const x=[27,50,73,59,32,47,72,56,30,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker mountain-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{crest} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
