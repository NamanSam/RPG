import { useEffect, useRef } from "react";
export function CanyonCelebration({totalXp,onClose}) {
  const ref=useRef(null);
  useEffect(()=>{const d=ref.current;d.showModal();return ()=>d.close();},[]);
  return <dialog ref={ref} className="guide-dialog beach-celebration" aria-labelledby="canyon-complete-title" onCancel={onClose}>
    <div className="celebration-sparks" aria-hidden="true">✦ · ✧ · ✦</div>
    <div className="canyon-badge" aria-hidden="true">♜</div>
    <p className="eyebrow">THE FOUR BLUEPRINTS ARE UNITED</p>
    <h2 id="canyon-complete-title">OOP CANYON COMPLETE</h2>
    <p>OOP Canyon Architect badge earned</p><p>{totalXp} total XP</p>
    <div className="unlock-announcement">◇ → ◆ Collection Kingdom unlocked</div>
    <button className="primary-button" onClick={onClose}>See Collection Kingdom →</button>
  </dialog>;
}
