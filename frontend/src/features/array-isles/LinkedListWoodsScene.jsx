import { Npc } from "../world-map/PixelScene";

const landmarks=["♣","↝","◉","⛓","♜"];
const locations=["Root Path","Ranger Trail","Twin-Pointer Grove","Ancient Chain Ruins","Canopy Master Shrine"];

export function LinkedListWoodsScene({trail,onQuestion}){
 return <section className={`woods-trail woods-${trail.number} ${trail.unlocked?"":"trail-locked"}`} aria-label={trail.name} id={`linked-trail-${trail.number}`}>
  <div className="woods-depth" aria-hidden="true"/><div className="woods-chain" aria-hidden="true"/>
  <div className="woods-tree tree-one" aria-hidden="true">♠</div><div className="woods-tree tree-two" aria-hidden="true">♠</div>
  <div className="woods-landmark" aria-hidden="true">{landmarks[trail.number-1]}</div>
  <header className="island-sign woods-sign"><small>WOODS TRAIL 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${locations[trail.number-1]} · Follow each connected rune in order.`:"Restore the previous chain before entering this trail."}</p></header>
  <div className="island-guide woods-guide" aria-hidden="true"><Npc theme="forest"/></div>
  {trail.questions.map((q,i)=><Marker key={q.id} question={q} index={i} onClick={()=>onQuestion(q.id)}/>)}
  <footer>{trail.completed===10?"✓ NODE TRAIL RESTORED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ PATH DISCONNECTED"}</footer>
 </section>;
}
function Marker({question:q,index,onClick}){const x=[25,48,73,62,32,50,76,58,28,50][index];return <button className={`island-marker woods-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+index*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick} aria-label={`${q.title}: ${q.status.toLowerCase()}, ${q.xpReward} XP`}><span className="island-lantern" aria-hidden="true">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone" aria-hidden="true">{String(index+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP{q.status==="COMPLETED"?" · REPLAY":""}</small></button>}
