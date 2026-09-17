import {useEffect,useRef} from "react";
import {Relic} from "./CampaignQuestNodes";

export function CampaignCelebration({world,next,totalXp,badge,onClose}) {
  const ref=useRef(null);useEffect(()=>{const el=ref.current;el.showModal();return()=>el.close();},[]);
  return <dialog ref={ref} className="guide-dialog beach-celebration" aria-labelledby="campaign-celebration-title" onCancel={onClose}>
    <div className="celebration-sparks" aria-hidden="true">✦ · ✧ · ✦</div><div className="beach-badge"><Relic theme={world.theme}/></div>
    <p className="eyebrow">{next?'AREA RESTORED':'JAVA CAMPAIGN COMPLETE'}</p><h2 id="campaign-celebration-title">{world.name.toUpperCase()} COMPLETE</h2>
    <p>{badge} badge earned</p><p>{totalXp} total XP</p><div className="unlock-announcement">◇ → ◆ {next?.name||'DSA Realm / Array Isles'} unlocked</div>
    <button className="primary-button" onClick={onClose}>{next?`See ${next.name} →`:'Enter the DSA Realm →'}</button>
  </dialog>;
}
