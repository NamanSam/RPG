import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../services/api";
import { QuestionPanel } from "../features/questions/QuestionPanel";
import { ArrayIslesScene } from "../features/array-isles/ArrayIslesScene";
import { StringHarborScene } from "../features/array-isles/StringHarborScene";
import { LinkedListWoodsScene } from "../features/array-isles/LinkedListWoodsScene";
import "../features/array-isles/isles.css";

export function DsaWorldMap() {
  const [map,setMap]=useState(null),[topics,setTopics]=useState({}),[error,setError]=useState("");
  const [question,setQuestion]=useState(null),[celebration,setCelebration]=useState(null),[pending,setPending]=useState(null);
  useEffect(()=>{let active=true;(async()=>{try{const value=await api("/campaigns/dsa/map");if(!active)return;setMap(value);if(value.unlocked){const arrays=await api("/topics/arrays/progress");if(!active)return;const next={arrays};if(arrays.complete){next.strings=await api("/topics/strings/progress");if(next.strings.complete)next.linked=await api("/topics/linked-lists/progress");}if(active)setTopics(next);}}catch(e){if(active)setError(e.message);}})();return()=>{active=false;};},[]);
  async function result(r){setTopics(old=>({...old,[r.progress.topic==="linked-lists"?"linked":r.progress.topic]:r.progress}));if(r.topicCompletedNow||r.trailCompletedNow)setPending(r);if(r.topicCompletedNow&&r.progress.topic==="arrays"){const strings=await api("/topics/strings/progress");setTopics(old=>({...old,strings}));}if(r.topicCompletedNow&&r.progress.topic==="strings"){const linked=await api("/topics/linked-lists/progress");setTopics(old=>({...old,linked}));}}
  function close(){setQuestion(null);if(pending){setCelebration(pending);setPending(null);}}
  const arrays=topics.arrays,strings=topics.strings,linked=topics.linked;
  return <div className="archipelago"><header className="isles-nav"><Link to="/world">← The Java Realm</Link><span>CODEQUEST · DSA CAMPAIGN</span></header>
    <main className="isles-main"><p className="eyebrow">THE ALGORITHM ARCHIPELAGO</p><h1>DSA Realm</h1><p>Restore each region to reveal the next route across the sea.</p>
      {error&&<p role="alert">{error} <Link to="/login">Return to sign in</Link></p>}{!map&&!error&&<p>Charting the sea…</p>}
      {map&&!map.unlocked&&<section className="isles-gate"><span aria-hidden="true">◇</span><h2>The crossing awaits</h2><p>{map.message}</p><Link to="/world">Continue your Java adventure →</Link></section>}
      {arrays&&<><RegionHeader eyebrow="DSA REGION 01" title="Array Isles" description="Fifty discoveries. Five trails. One island to restore."/><TopicStatus progress={arrays}/><TrailNav progress={arrays} prefix="trail"/><div className="island-map">{arrays.trails.map(t=><ArrayIslesScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      {arrays&&<section className="harbor-region"><RegionHeader eyebrow="DSA REGION 02" title="String Harbor" description="Decode fifty messages across five coastal trails."/>
        {!arrays.complete&&<div className="harbor-locked"><span aria-hidden="true">◇</span><h3>HARBOR ROUTE FOGBOUND</h3><p>Complete all 50 Array Isles encounters to reveal the harbor passage.</p></div>}
        {strings&&<><TopicStatus progress={strings}/><TrailNav progress={strings} prefix="strings-trail"/><div className="island-map harbor-map">{strings.trails.map(t=><StringHarborScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      </section>}
      {strings&&<section className="woods-region"><RegionHeader eyebrow="DSA REGION 03" title="Linked List Woods" description="Reconnect fifty node paths through five forest trails."/>
        {!strings.complete&&<div className="woods-locked"><span aria-hidden="true">◇</span><h3>WOODS PATH DISCONNECTED</h3><p>Complete all 50 String Harbor encounters to reveal the root path.</p></div>}
        {linked&&<><TopicStatus progress={linked}/><TrailNav progress={linked} prefix="linked-trail"/><div className="island-map woods-map">{linked.trails.map(t=><LinkedListWoodsScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div>{linked.complete&&<p className="island-future">Linked List Woods restored. The valid route toward Stack Tower is ready for the next milestone.</p>}</>}
      </section>}
    </main>
    {question&&<QuestionPanel key={question} id={question} onClose={close} onResult={result}/>} {celebration&&<Completion result={celebration} onClose={()=>{const p=celebration.progress,prefix=p.topic==="linked-lists"?"linked-trail":p.topic==="strings"?"strings-trail":"trail";setCelebration(null);if(!celebration.topicCompletedNow)document.getElementById(`${prefix}-${p.unlockedTrail}`)?.scrollIntoView({behavior:window.matchMedia("(prefers-reduced-motion: reduce)").matches?"auto":"smooth"});}}/>}
  </div>;
}
function RegionHeader({eyebrow,title,description}){return <div className="region-heading"><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p>{description}</p></div>}
function TopicStatus({progress}){return <div className="isles-pass"><strong>{progress.completed}/50 encounters restored</strong><span>Level {progress.level} · {progress.totalXp} total XP</span>{progress.badge&&<span>⚑ {progress.badge}</span>}</div>}
function TrailNav({progress,prefix}){return <nav className="trail-navigation" aria-label={`${progress.regionName} trails`}>{progress.trails.map(t=><a key={t.number} href={`#${prefix}-${t.number}`}>{t.unlocked?"◆":"◇"} {t.name}</a>)}</nav>}
function Completion({result,onClose}){const ref=useRef(null);useEffect(()=>{const el=ref.current;el.showModal();return()=>el.close();},[]);const names={arrays:"ARRAY ISLES",strings:"STRING HARBOR","linked-lists":"LINKED LIST WOODS"},icons={arrays:"▥",strings:"⚓","linked-lists":"♣"};return <dialog ref={ref} className="guide-dialog island-completion" aria-labelledby="dsa-complete" onCancel={onClose}><div className="island-badge" aria-hidden="true">{icons[result.progress.topic]}<br/>◆ ◆ ◆</div><h2 id="dsa-complete">{result.topicCompletedNow?`${names[result.progress.topic]} COMPLETE`:"TRAIL RESTORED"}</h2><p>{result.topicCompletedNow?`50 discoveries. ${result.progress.badge} badge earned.`:"All ten encounters completed. The next trail is open."}</p><p>{result.progress.totalXp} total XP</p><button className="primary-button" onClick={onClose}>{result.topicCompletedNow?"Return to the region":"Explore the next trail →"}</button></dialog>}
