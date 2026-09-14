import { useEffect, useRef } from "react";
import { MillSprite } from "./VillageQuestNodes";

export function VillageCelebration({ onClose, totalXp }) {
  const dialog = useRef(null);
  useEffect(() => {
    const el = dialog.current;
    el.showModal();
    return () => el.close();
  }, []);
  return (
    <dialog
      ref={dialog}
      className="guide-dialog beach-celebration village-celebration"
      aria-labelledby="village-complete-title"
      onCancel={onClose}
    >
      <div className="celebration-sparks" aria-hidden="true">
        ✦ · ✧ · ✦
      </div>
      <div className="beach-badge village-badge">
        <MillSprite badge />
      </div>
      <p className="eyebrow">THREE QUESTS · A VILLAGE RESTORED</p>
      <h2 id="village-complete-title">LOOP VILLAGE COMPLETE</h2>
      <p>
        You opened the gate, set the mill turning, and brought water to the
        village.
      </p>
      <strong>Loop Village Pathfinder badge earned</strong>
      <p>300 village XP earned · {totalXp} total XP</p>
      <div className="unlock-announcement">◇ → ◆ Array Forest unlocked</div>
      <button className="primary-button" onClick={onClose}>
        See Array Forest →
      </button>
    </dialog>
  );
}
