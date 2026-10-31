import { Npc } from "../world-map/PixelScene";
const places=["Highland Base Camp","Heap Quarry","Priority Fortress","Top-K Crystal Mine","Summit Trial"];
const crests=["♜","◆","⚑","✦","♛"];
const markerX=[28,52,73,58,31,47,71,56,30,51];

export function HeapHighlandsScene({trail,onQuestion}) {
  const unlocked=Boolean(trail.unlocked);
  const complete=trail.completed===10;
  return <section className={`heap-trail heap-${trail.number} ${unlocked?"":"trail-locked"} ${complete?"trail-complete":""}`} id={`heap-trail-${trail.number}`} aria-label={`${trail.name} trail`}>
    <div className="heap-sky" aria-hidden="true"/><div className="heap-range heap-range-back" aria-hidden="true"/><div className="heap-range heap-range-front" aria-hidden="true"/><div className="heap-ascent" aria-hidden="true"/><div className="heap-snowcap" aria-hidden="true">▲ ▲ ▲</div>
    <header className="island-sign heap-sign"><small>HIGHLAND PASS 0{trail.number} · {trail.completed}/10 COMPLETE</small><h2>{trail.name}</h2><p>{unlocked?`${places[trail.number-1]} · Follow the cairns to the summit.`:"Complete the lower pass to open this trail."}</p></header>
    <div className="heap-landmark" aria-hidden="true">{crests[trail.number-1]}</div><div className="island-guide heap-guide" aria-hidden="true"><Npc theme="canyon"/></div>
    {trail.questions.map((q,i)=><button key={q.id} type="button" aria-label={`${q.title}, ${q.status.toLowerCase()}, ${q.difficulty}, ${q.xpReward} XP`} className={`island-marker heap-marker ${q.status.toLowerCase()}`} style={{left:`${markerX[i]}%`,top:`${235+i*91}px`}} disabled={q.status==="LOCKED"} onClick={()=>onQuestion(q.id)}>
      <span className="island-lantern" aria-hidden="true">{q.status==="COMPLETED"?"✓":q.status==="LOCKED"?"◆":"!"}</span><span className="island-stone" aria-hidden="true">{crests[trail.number-1]} {String(i+1).padStart(2,"0")}</span><span className="island-marker-title">{q.title}</span><small>{q.difficulty} · {q.xpReward} XP{q.status==="COMPLETED"?" · REPLAY":""}</small>
    </button>)}
    <footer>{complete?(trail.number===5?"♛ SUMMIT MASTER TRIAL COMPLETE":"✓ PASS COMPLETE"):unlocked?"ASCEND · 4 EASY · 4 MEDIUM · 2 HARD":"◇ PASS SEALED"}</footer>
  </section>;
}
