import { Npc } from "../world-map/PixelScene";

export function ArrayIslesScene({trail,onQuestion}) {
  return <section className={`island-trail trail-${trail.number} ${trail.unlocked ? "" : "trail-locked"}`} aria-label={trail.name} id={`trail-${trail.number}`}>
    <div className="island-coast" aria-hidden="true"/>
    <div className="island-path" aria-hidden="true"/>
    <div className="island-palm palm-one" aria-hidden="true">♠</div><div className="island-palm palm-two" aria-hidden="true">♠</div>
    <div className="island-landmark" aria-hidden="true">{["⚓","◈","▲","⛰","▥"][trail.number-1]}</div>
    <header className="island-sign"><small>TRAIL 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked ? "Follow the lanterns. Each encounter teaches a new pattern." : "Complete all ten encounters on the previous trail."}</p></header>
    <div className="island-guide" aria-hidden="true"><Npc theme="village"/></div>
    {trail.questions.map((q,i)=><QuestionMarker key={q.id} question={q} index={i} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10 ? "✓ TRAIL RESTORED" : trail.unlocked ? "4 EASY · 4 MEDIUM · 2 HARD" : "◇ UNDISCOVERED"}</footer>
  </section>;
}
function QuestionMarker({question:q,index,onClick}) {
  const x=[28,58,75,45,22,52,77,57,29,50][index];
  return <button className={`island-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+index*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick} aria-label={`${q.title}: ${q.status.toLowerCase()}, ${q.xpReward} XP`}>
    <span className="island-lantern" aria-hidden="true">{q.status==="COMPLETED" ? "✓" : q.status==="LOCKED" ? "◆" : "!"}</span>
    <span className="island-stone" aria-hidden="true">{String(index+1).padStart(2,"0")}</span>
    <span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP{q.status==="COMPLETED" ? " · REPLAY" : ""}</small>
  </button>;
}
