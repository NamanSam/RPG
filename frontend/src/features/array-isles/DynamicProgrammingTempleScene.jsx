import { Npc } from "../world-map/PixelScene";

const chambers=["Foundation Hall","Recurrence Gallery","Knapsack Vault","Sequence Sanctuary","Oracle Chamber"];
const runes=["◇","↻","▣","⌁","✦"];

export function DynamicProgrammingTempleScene({trail,onQuestion}) {
  return <section className={`temple-trail temple-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`dp-trail-${trail.number}`} aria-label={trail.name}>
    <div className="temple-walls"/><div className="temple-path"/><div className="temple-crystal">♦</div><div className="temple-runes">◇ · ↻ · ▣</div>
    <header className="island-sign temple-sign"><small>TEMPLE CHAMBER 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${chambers[trail.number-1]} · Restore each state in dependency order.`:"Complete the previous chamber to awaken these runes."}</p></header>
    <div className="island-guide temple-guide"><Npc theme="canyon"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} rune={runes[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ CHAMBER RESTORED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ RUNES DORMANT"}</footer>
  </section>;
}

function Marker({q,i,rune,onClick}) {
  const x=[28,52,73,58,31,47,71,56,30,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker temple-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{rune} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
