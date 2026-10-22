import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../services/api";
import { QuestionPanel } from "../features/questions/QuestionPanel";
import { ArrayIslesScene } from "../features/array-isles/ArrayIslesScene";
import { StringHarborScene } from "../features/array-isles/StringHarborScene";
import { LinkedListWoodsScene } from "../features/array-isles/LinkedListWoodsScene";
import { StackTowerScene } from "../features/array-isles/StackTowerScene";
import { QueueStationScene } from "../features/array-isles/QueueStationScene";
import { TreeKingdomScene } from "../features/array-isles/TreeKingdomScene";
import { BstValleyScene } from "../features/array-isles/BstValleyScene";
import { GraphMountainsScene } from "../features/array-isles/GraphMountainsScene";
import "../features/array-isles/isles.css";

export function DsaWorldMap() {
  const [map,setMap]=useState(null),[topics,setTopics]=useState({}),[error,setError]=useState("");
  const [question,setQuestion]=useState(null),[celebration,setCelebration]=useState(null),[pending,setPending]=useState(null);
  useEffect(()=>{let active=true;(async()=>{try{const value=await api("/campaigns/dsa/map");if(!active)return;setMap(value);if(value.unlocked){const arrays=await api("/topics/arrays/progress"),next={arrays};if(arrays.complete){next.strings=await api("/topics/strings/progress");if(next.strings.complete){next.linked=await api("/topics/linked-lists/progress");if(next.linked.complete){next.stacks=await api("/topics/stacks/progress");if(next.stacks.complete){next.queues=await api("/topics/queues/progress");if(next.queues.complete){next.trees=await api("/topics/trees/progress");if(next.trees.complete){next.bst=await api("/topics/binary-search-trees/progress");if(next.bst.complete)next.graphs=await api("/topics/graphs/progress");}}}}}}if(active)setTopics(next);}}catch(e){if(active)setError(e.message);}})();return()=>{active=false;};},[]);
  async function result(r){const key=r.progress.topic==="linked-lists"?"linked":r.progress.topic==="binary-search-trees"?"bst":r.progress.topic;setTopics(old=>({...old,[key]:r.progress}));if(r.topicCompletedNow||r.trailCompletedNow)setPending(r);const next={arrays:"strings",strings:"linked-lists","linked-lists":"stacks",stacks:"queues",queues:"trees",trees:"binary-search-trees","binary-search-trees":"graphs"}[r.progress.topic];if(r.topicCompletedNow&&next){const p=await api(`/topics/${next}/progress`);setTopics(old=>({...old,[next==="linked-lists"?"linked":next==="binary-search-trees"?"bst":next]:p}));}}
  function close(){setQuestion(null);if(pending){setCelebration(pending);setPending(null);}}
  const arrays=topics.arrays,strings=topics.strings,linked=topics.linked,stacks=topics.stacks,queues=topics.queues,trees=topics.trees,bst=topics.bst,graphs=topics.graphs;
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
      {linked&&<section className="tower-region"><RegionHeader eyebrow="DSA REGION 04" title="Stack Tower" description="Climb fifty rune encounters across five magical floors."/>
        {!linked.complete&&<div className="tower-locked"><span aria-hidden="true">◇</span><h3>TOWER STAIRS SEALED</h3><p>Complete all 50 Linked List Woods encounters to open the tower.</p></div>}
        {stacks&&<><TopicStatus progress={stacks}/><TrailNav progress={stacks} prefix="stack-trail"/><div className="island-map tower-map">{stacks.trails.map(t=><StackTowerScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      </section>}
      {stacks&&<section className="station-region"><RegionHeader eyebrow="DSA REGION 05" title="Queue Station" description="Dispatch fifty challenges across five enchanted platforms."/>
        {!stacks.complete&&<div className="station-locked"><span aria-hidden="true">◇</span><h3>DEPARTURE SIGNAL STOPPED</h3><p>Complete all 50 Stack Tower encounters to open the station gates.</p></div>}
        {queues&&<><TopicStatus progress={queues}/><TrailNav progress={queues} prefix="queue-trail"/><div className="island-map station-map">{queues.trails.map(t=><QueueStationScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      </section>}
      {queues&&<section className="kingdom-region"><RegionHeader eyebrow="DSA REGION 06" title="Tree Kingdom" description="Restore fifty branches across the ancient tree realm."/>
        {!queues.complete&&<div className="kingdom-locked"><span aria-hidden="true">◇</span><h3>ANCIENT ROOTS DORMANT</h3><p>Complete all 50 Queue Station encounters to awaken the kingdom.</p></div>}
        {trees&&<><TopicStatus progress={trees}/><TrailNav progress={trees} prefix="tree-trail"/><div className="island-map kingdom-map">{trees.trails.map(t=><TreeKingdomScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      </section>}
      {trees&&<section className="valley-region"><RegionHeader eyebrow="DSA REGION 07" title="BST Valley" description="Navigate fifty ordered encounters across five branching paths."/>
        {!trees.complete&&<div className="valley-locked"><span aria-hidden="true">◇</span><h3>VALLEY FORKS UNBALANCED</h3><p>Complete all 50 Tree Kingdom encounters to reveal the ordered trail.</p></div>}
        {bst&&<><TopicStatus progress={bst}/><TrailNav progress={bst} prefix="bst-trail"/><div className="island-map valley-map">{bst.trails.map(t=><BstValleyScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div></>}
      </section>}
      {bst&&<section className="mountain-region"><RegionHeader eyebrow="DSA REGION 08" title="Graph Mountains" description="Connect fifty routes across five mountain ascents."/>
        {!bst.complete&&<div className="mountain-locked"><span aria-hidden="true">◇</span><h3>MOUNTAIN PASS DISCONNECTED</h3><p>Complete all 50 BST Valley encounters to reveal the bridge network.</p></div>}
        {graphs&&<><TopicStatus progress={graphs}/><TrailNav progress={graphs} prefix="graph-trail"/><div className="island-map mountain-map">{graphs.trails.map(t=><GraphMountainsScene key={t.number} trail={t} onQuestion={setQuestion}/>)}</div>{graphs.complete&&<p className="island-future">Graph Mountains restored. Its completion is ready to serve as the prerequisite for a future Greedy Desert milestone.</p>}</>}
      </section>}
    </main>
    {question&&<QuestionPanel key={question} id={question} onClose={close} onResult={result}/>} {celebration&&<Completion result={celebration} onClose={()=>{const p=celebration.progress,prefix=p.topic==="graphs"?"graph-trail":p.topic==="binary-search-trees"?"bst-trail":p.topic==="trees"?"tree-trail":p.topic==="queues"?"queue-trail":p.topic==="stacks"?"stack-trail":p.topic==="linked-lists"?"linked-trail":p.topic==="strings"?"strings-trail":"trail";setCelebration(null);if(!celebration.topicCompletedNow)document.getElementById(`${prefix}-${p.unlockedTrail}`)?.scrollIntoView({behavior:window.matchMedia("(prefers-reduced-motion: reduce)").matches?"auto":"smooth"});}}/>}
  </div>;
}
function RegionHeader({eyebrow,title,description}){return <div className="region-heading"><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p>{description}</p></div>}
function TopicStatus({progress}){return <div className="isles-pass"><strong>{progress.completed}/50 encounters restored</strong><span>Level {progress.level} · {progress.totalXp} total XP</span>{progress.badge&&<span>⚑ {progress.badge}</span>}</div>}
function TrailNav({progress,prefix}){return <nav className="trail-navigation" aria-label={`${progress.regionName} trails`}>{progress.trails.map(t=><a key={t.number} href={`#${prefix}-${t.number}`}>{t.unlocked?"◆":"◇"} {t.name}</a>)}</nav>}
function Completion({result,onClose}){const ref=useRef(null);useEffect(()=>{const el=ref.current;el.showModal();return()=>el.close();},[]);const names={arrays:"ARRAY ISLES",strings:"STRING HARBOR","linked-lists":"LINKED LIST WOODS",stacks:"STACK TOWER",queues:"QUEUE STATION",trees:"TREE KINGDOM","binary-search-trees":"BST VALLEY",graphs:"GRAPH MOUNTAINS"},icons={arrays:"▥",strings:"⚓","linked-lists":"♣",stacks:"✦",queues:"⇥",trees:"♛","binary-search-trees":"⚖",graphs:"▲"};return <dialog ref={ref} className="guide-dialog island-completion" aria-labelledby="dsa-complete" onCancel={onClose}><div className="island-badge" aria-hidden="true">{icons[result.progress.topic]}<br/>◆ ◆ ◆</div><h2 id="dsa-complete">{result.topicCompletedNow?`${names[result.progress.topic]} COMPLETE`:"TRAIL RESTORED"}</h2><p>{result.topicCompletedNow?`50 discoveries. ${result.progress.badge} badge earned.`:"All ten encounters completed. The next trail is open."}</p><p>{result.progress.totalXp} total XP</p><button className="primary-button" onClick={onClose}>{result.topicCompletedNow?"Return to the region":"Explore the next trail →"}</button></dialog>}
