import { Npc } from "../world-map/PixelScene";

const places=["Station Entrance","Boarding Platform","Circular Rail Yard","Transit Control Room","Grand Terminal"];
const marks=["◫","⇥","◉","⌁","★"];

export function QueueStationScene({trail,onQuestion}) {
  return <section className={`station-trail station-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`queue-trail-${trail.number}`} aria-label={trail.name}>
    <div className="station-sky"/><div className="station-track"/><div className="station-clock">◷</div><div className="station-train">▰▰▰</div>
    <header className="island-sign station-sign"><small>PLATFORM 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${places[trail.number-1]} · Dispatch each challenge in order.`:"Complete the previous platform to clear this signal."}</p></header>
    <div className="island-guide station-guide"><Npc theme="village"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} mark={marks[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ PLATFORM CLEARED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ SIGNAL STOPPED"}</footer>
  </section>;
}

function Marker({q,i,mark,onClick}) {
  const x=[28,51,72,55,31,48,72,58,29,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker station-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{mark} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
