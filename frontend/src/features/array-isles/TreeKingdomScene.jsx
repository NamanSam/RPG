import { Npc } from "../world-map/PixelScene";

const places=["Root Village","Traversal Canopy","Height Observatory","Crown Viewpoint","Ancient Tree Throne"];
const crests=["⌄","⑂","↟","◉","♛"];

export function TreeKingdomScene({trail,onQuestion}) {
  return <section className={`kingdom-trail kingdom-${trail.number} ${trail.unlocked?"":"trail-locked"}`} id={`tree-trail-${trail.number}`} aria-label={trail.name}>
    <div className="kingdom-grove"/><div className="kingdom-branches"/><div className="kingdom-tree">♣</div><div className="kingdom-bridge">═╪═╪═</div>
    <header className="island-sign kingdom-sign"><small>BRANCH 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{trail.unlocked?`${places[trail.number-1]} · Follow each glowing branch in order.`:"Restore the branch below to grow this path."}</p></header>
    <div className="island-guide kingdom-guide"><Npc theme="forest"/></div>
    {trail.questions.map((q,i)=><Marker key={q.id} q={q} i={i} crest={crests[trail.number-1]} onClick={()=>onQuestion(q.id)}/>)}
    <footer>{trail.completed===10?"✓ BRANCH RESTORED":trail.unlocked?"4 EASY · 4 MEDIUM · 2 HARD":"◇ ROOTS DORMANT"}</footer>
  </section>;
}

function Marker({q,i,crest,onClick}) {
  const x=[29,52,73,57,31,47,70,55,29,51][i];
  return <button aria-label={`${q.title}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker kingdom-marker ${q.status.toLowerCase()}`} style={{left:`${x}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={onClick}>
    <span className="island-lantern">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone">{crest} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP</small>
  </button>;
}
