import { Npc } from "../world-map/PixelScene";
import "./campaign.css";

export const campaignPreview = {
  "array-forest": [
    {
      "id": "the-lost-index",
      "title": "The Lost Index",
      "reward": 100,
      "status": "LOCKED"
    },
    {
      "id": "forest-traversal",
      "title": "Forest Traversal",
      "reward": 125,
      "status": "LOCKED"
    },
    {
      "id": "hidden-maximum",
      "title": "Hidden Maximum",
      "reward": 150,
      "status": "LOCKED"
    }
  ],
  "oop-canyon": [
    {
      "id": "blueprint-maker",
      "title": "Blueprint Maker",
      "reward": 125,
      "status": "LOCKED"
    },
    {
      "id": "the-hidden-vault",
      "title": "The Hidden Vault",
      "reward": 150,
      "status": "LOCKED"
    },
    {
      "id": "bloodline-of-classes",
      "title": "Bloodline of Classes",
      "reward": 175,
      "status": "LOCKED"
    },
    {
      "id": "many-forms",
      "title": "Many Forms",
      "reward": 200,
      "status": "LOCKED"
    }
  ],
  "collection-kingdom": [
    {
      "id": "the-dynamic-scroll",
      "title": "The Dynamic Scroll",
      "reward": 150,
      "status": "LOCKED"
    },
    {
      "id": "the-key-keeper",
      "title": "The Key Keeper",
      "reward": 175,
      "status": "LOCKED"
    },
    {
      "id": "hall-of-uniques",
      "title": "Hall of Uniques",
      "reward": 200,
      "status": "LOCKED"
    }
  ]
};

export function CampaignQuestNodes({world,quests,busy,onSelect}) {
  const positions=quests.length===4 ? [[34,32],[73,32],[30,65],[69,65]] : [[40,32],[25,65],[73,65]];
  return <>{quests.map((q,i)=><button key={q.id} className={`npc-node campaign-node ${q.status.toLowerCase()}`} style={{left:`${positions[i][0]}%`,top:`${positions[i][1]}%`}} disabled={busy||q.status==="LOCKED"} onClick={()=>onSelect(q.id)} aria-label={`${q.title}: ${q.status.toLowerCase()}, ${q.reward} XP`}>
    <span className={`quest-marker ${q.status==='LOCKED'?'locked-marker':''}`} aria-hidden="true">{q.status==='COMPLETED'?'✓':q.status==='LOCKED'?'◆':'!'}</span>
    {i===0 ? <Npc color={world.theme==='forest'?'#759554':world.theme==='canyon'?'#a66a64':'#8c7298'}/> : <Relic theme={world.theme} index={i}/>}
    <span className="npc-name">{q.title}</span><span className="npc-action">{q.status==='COMPLETED'?'COMPLETE · REPLAY':q.status==='LOCKED'?'FINISH PREVIOUS QUESTS':`${q.difficulty||'QUEST'} · ${q.reward} XP`}</span>
  </button>)}</>;
}
export function Relic({theme,index=1}) {
  return <svg className="npc-sprite campaign-relic" viewBox="0 0 48 48" shapeRendering="crispEdges" aria-hidden="true">
    {theme==='forest'?<><path fill="#686e55" d="M5 9h38v34H5z"/><path fill="#afb993" d="M8 5h32v7H8zM9 15h30v23H9z"/><path fill="#3c6c44" d="M3 24h8v21H3zM33 3h9v14h-9z"/><path fill="#506848" d="M15 20h18v4H15zM15 28h12v4H15z"/></>:theme==='canyon'?<><path fill="#855c50" d="M4 41h40v5H4zM8 13h7v28H8zM33 13h7v28h-7z"/><path fill="#dfb681" d="M4 8h40v8H4zM16 18h16v23H16z"/><path fill="#715861" d={index===1?'M20 22h8v15h-8z':'M18 23h12v4H18zM21 29h6v8h-6z'}/></>:<><path fill="#634d62" d="M4 6h40v38H4z"/><path fill="#d2b57c" d="M7 9h34v5H7zM7 26h34v4H7zM7 40h34v4H7z"/><path fill="#859b80" d="M9 15h5v10H9zM25 15h6v10h-6zM15 31h6v8h-6z"/><path fill="#bd8671" d="M16 16h6v9h-6zM33 14h5v11h-5zM24 31h6v8h-6z"/></>}
  </svg>;
}
