import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../services/api";
import { QuestionPanel } from "../features/questions/QuestionPanel";
import { ArrayIslesScene } from "../features/array-isles/ArrayIslesScene";
import "../features/array-isles/isles.css";

export function DsaWorldMap() {
  const [map,setMap]=useState(null), [progress,setProgress]=useState(null), [error,setError]=useState("");
  const [question,setQuestion]=useState(null), [celebration,setCelebration]=useState(null), [pending,setPending]=useState(null);
  useEffect(()=>{ let active=true;
    api("/campaigns/dsa/map").then(async value=>{if(!active)return;setMap(value);if(value.unlocked){const p=await api("/topics/arrays/progress");if(active)setProgress(p);}}).catch(e=>{if(active)setError(e.message);});
    return ()=>{active=false;};
  },[]);
  function result(r){setProgress(r.progress);if(r.topicCompletedNow||r.trailCompletedNow)setPending(r);}
  function close(){setQuestion(null);if(pending){setCelebration(pending);setPending(null);}}
  return <div className="archipelago"><header className="isles-nav"><Link to="/world">← The Java Realm</Link><span>CODEQUEST · DSA CAMPAIGN</span></header>
    <main className="isles-main"><p className="eyebrow">THE ALGORITHM ARCHIPELAGO</p><h1>Array Isles</h1><p>Fifty discoveries. Five trails. One island to restore.</p>
      {error && <p role="alert">{error} <Link to="/login">Return to sign in</Link></p>}
      {!map && !error && <p>Charting the sea…</p>}
      {map && !map.unlocked && <section className="isles-gate"><span aria-hidden="true">◇</span><h2>The crossing awaits</h2><p>{map.message}</p><Link to="/world">Continue your Java adventure →</Link></section>}
      {progress && <><div className="isles-pass"><strong>{progress.completed}/50 encounters restored</strong><span>Level {progress.level} · {progress.totalXp} total XP</span>{progress.badge && <span>⚑ {progress.badge}</span>}</div>
      <nav className="trail-navigation" aria-label="Island trails">{progress.trails.map(t=><a key={t.number} href={`#trail-${t.number}`}>{t.unlocked ? "◆" : "◇"} {t.name}</a>)}</nav>
      <div className="island-map">{progress.trails.map(t=><ArrayIslesScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div>
      {progress.complete && <p className="island-future">Array Isles restored. Your next destination will appear when a new topic is published.</p>}</>}
    </main>
    {question && <QuestionPanel key={question} id={question} onClose={close} onResult={result}/>}
    {celebration && <Completion result={celebration} onClose={()=>{const next=celebration.progress.unlockedTrail;setCelebration(null);if(!celebration.topicCompletedNow)document.getElementById(`trail-${next}`)?.scrollIntoView({behavior:window.matchMedia("(prefers-reduced-motion: reduce)").matches?"auto":"smooth"});}}/>}
  </div>;
}
function Completion({result,onClose}) {
  const ref=useRef(null);useEffect(()=>{const el=ref.current;el.showModal();return()=>el.close();},[]);
  return <dialog ref={ref} className="guide-dialog island-completion" aria-labelledby="isles-complete" onCancel={onClose}>
    <div className="island-badge" aria-hidden="true">▥<br/>◆ ◆ ◆</div><h2 id="isles-complete">{result.topicCompletedNow ? "ARRAY ISLES COMPLETE" : "TRAIL RESTORED"}</h2>
    <p>{result.topicCompletedNow ? "50 discoveries. Array Isles Pathfinder badge earned." : "All ten encounters completed. The next island trail is open."}</p><p>{result.progress.totalXp} total XP</p><button className="primary-button" onClick={onClose}>{result.topicCompletedNow ? "Return to the island" : "Explore the next trail →"}</button>
  </dialog>;
}
