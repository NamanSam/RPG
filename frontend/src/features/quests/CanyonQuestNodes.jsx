import { Npc } from "../world-map/PixelScene";
import "./canyon.css";

const guides = ["The Architect", "Vault Keeper", "Lineage Guardian", "Shape Master"];
const locations = ["Ancient workshop", "Sealed stone vault", "Ancestral ruins", "Transformation shrine"];
export const canyonPreview = [
  {id:"blueprint-maker", title:"Blueprint Maker", difficulty:"Easy", reward:125, status:"LOCKED"},
  {id:"the-hidden-vault", title:"The Hidden Vault", difficulty:"Medium", reward:150, status:"LOCKED"},
  {id:"bloodline-of-classes", title:"Bloodline of Classes", difficulty:"Medium", reward:175, status:"LOCKED"},
  {id:"many-forms", title:"Many Forms", difficulty:"Hard", reward:200, status:"LOCKED"},
];
export function CanyonQuestNodes({quests,busy,onSelect}) {
  const positions=[[19,43],[41,59],[65,43],[83,59]];
  return <>{quests.map((q,i)=><button key={q.id} className={`npc-node canyon-node ${q.status.toLowerCase()}`} style={{left:`${positions[i][0]}%`,top:`${positions[i][1]}%`}} disabled={busy || q.status==='LOCKED'} onClick={()=>onSelect(q.id)} aria-label={`${guides[i]}, ${locations[i]}, ${q.title}: ${q.status.toLowerCase()}, ${q.reward} XP`}>
    <span className="quest-marker" aria-hidden="true">{q.status==='COMPLETED'?'✓':q.status==='LOCKED'?'◆':'!'}</span>
    <Npc color={['#d69348','#708e92','#89719a','#65a99e'][i]}/>
    <span className="npc-name">{guides[i]}</span><span className="canyon-location">{locations[i]}</span>
    <span className="npc-action">{q.title}<br/>{q.status==='COMPLETED'?'COMPLETE · REPLAY':q.status==='LOCKED'?'LOCKED':`${q.difficulty} · ${q.reward} XP`}</span>
  </button>)}</>;
}
export function CanyonScene(){
  return <svg className="pixel-scene canyon" viewBox="0 0 800 480" preserveAspectRatio="none" shapeRendering="crispEdges" aria-hidden="true">
    <rect width="800" height="480" fill="#c99268"/>
    <path fill="#694c49" d="M0 140h170v90h100v80h190v-75h150v70h190v175H0z"/>
    <path fill="#a8654c" d="M0 150h160v45h100v65h210v-75h140v45h190v65H620v-40H490v80H240v-65H140v-45H0z"/>
    <path fill="#e6b77c" d="M0 125h180v65h90v65h200v-75h150v50h180v28H598v-50H492v75H248v-65H158v-65H0z"/>
    <path fill="#e0ae75" d="M0 355h180v-30h130v45h170v-35h120v30h200v115H0z"/>
    <path stroke="#563d33" strokeWidth="8" fill="none" d="M165 242L310 340M170 268L305 368"/>
    <path stroke="#c79b66" strokeWidth="28" strokeDasharray="5 5" d="M170 255L305 354"/>
    {[ [110,198],[295,300],[488,180],[650,305] ].map(([x,y],i)=><g key={i} transform={`translate(${x} ${y})`}>
      <path fill="#725951" d="M-25 30h90v12h-90zM-18-15h14v45h-14zM42-15h14v45H42zM-24-28h86v14h-86z"/>
      <path fill="#cfb18b" d="M-14-12h6v40h-6zM46-12h6v40h-6zM-20-25h78v6h-78z"/>
      <path className="canyon-rune" fill="#ffe09a" d="M14-7h12v8h8v12h-8v8H14v-8H6V1h8z"/>
    </g>)}
    <g className="canyon-dust" fill="#ffe2ac"><path d="M70 310h5v4h-5zM360 200h6v4h-6zM570 390h5v4h-5zM720 180h5v4h-5z"/></g>
  </svg>;
}

