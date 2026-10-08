import { useEffect, useRef } from "react";
import { ForestShrine } from "./ForestQuestNodes";

export function ForestCelebration({ totalXp, badge, onClose }) {
  const ref = useRef(null);
  useEffect(() => { const dialog=ref.current; dialog.showModal(); return () => dialog.close(); }, []);
  return <dialog ref={ref} className="guide-dialog beach-celebration" aria-labelledby="forest-complete-title" onCancel={onClose}>
    <div className="celebration-sparks" aria-hidden="true">✦ · ✧ · ✦</div>
    <div className="beach-badge forest-badge"><ForestShrine /></div>
    <p className="eyebrow">THE GROVE IS RESTORED</p>
    <h2 id="forest-complete-title">ARRAY FOREST COMPLETE</h2>
    <p>{badge} badge earned</p><p>{totalXp} total XP</p>
    <div className="unlock-announcement">◇ → ◆ OOP Canyon unlocked</div>
    <button className="primary-button" onClick={onClose}>See OOP Canyon →</button>
  </dialog>;
}
