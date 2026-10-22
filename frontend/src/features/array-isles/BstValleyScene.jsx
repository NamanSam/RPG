import { Npc } from "../world-map/PixelScene";

const places=["Root Camp","Search Fork","Validation Gate","Balance Ruins","BST Summit Shrine"];
const crests=["⌄","↙↘","✓","⚖","▲"];

export function BstValleyScene({trail,onQuestion}) {
  return <section className={`valley-trail valley-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`bst-trail-${trail.number}`} aria-label={trail.name}>
    <div className="valley-cliffs"/><div className="valley-forks"/><div className="valley-shrine">♜</div><div className="valley-bridge">╞══╪══╡</div>
    <header className="island-sign valley-sign"><small>VALLEY PATH 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${places[trail.number-1]} · Choose each ordered fork in sequence.`:"Balance the previous path to open this fork."}</p></header>
    <div className="island-guide valley-guide"><Npc theme="forest"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} crest={crests[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ PATH BALANCED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ FORK SEALED"}</footer>
  </section>;
}

function Marker({q,i,crest,onClick}) {
  const x=[50,31,70,39,62,28,72,42,64,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker valley-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{crest} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
