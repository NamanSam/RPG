import { useEffect, useRef } from "react";

export function BeachCelebration({ onClose }) {
  const dialog = useRef(null);
  useEffect(() => {
    const el = dialog.current;
    el.showModal();
    return () => el.close();
  }, []);
  return (
    <dialog
      ref={dialog}
      className="guide-dialog beach-celebration"
      aria-labelledby="celebration-title"
      onCancel={onClose}
    >
      <div className="celebration-sparks" aria-hidden="true">
        ✦ · ✧ · ✦
      </div>
      <div className="beach-badge" aria-hidden="true">
        ⚑
      </div>
      <p className="eyebrow">AREA COMPLETE · 3 / 3 QUESTS</p>
      <h2 id="celebration-title">Beginner Beach complete!</h2>
      <p>
        You lit the lighthouse, mastered the signal, and sorted the supplies.
      </p>
      <strong>Beginner Beach Explorer badge earned</strong>
      <p>175 quest XP earned. Your next destination is ready.</p>
      <div className="unlock-announcement">◇ → ◆ Loop Village unlocked</div>
      <button className="primary-button" onClick={onClose}>
        See Loop Village →
      </button>
    </dialog>
  );
}
