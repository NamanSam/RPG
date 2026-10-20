import { useEffect, useRef } from "react";
export function KingdomCelebration({ progress, onClose }) {
  const ref = useRef(null);
  useEffect(() => {
    const d = ref.current;
    d.showModal();
    return () => d.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className="guide-dialog beach-celebration"
      aria-labelledby="java-complete-title"
      onCancel={onClose}
    >
      <div className="celebration-sparks" aria-hidden="true">
        ✦ · ✧ · ✦
      </div>
      <div className="kingdom-crown" aria-hidden="true">
        ♛
      </div>
      <p className="eyebrow">COLLECTION KINGDOM COMPLETE</p>
      <p>Collection Kingdom Keeper badge earned</p>
      <h2 id="java-complete-title">JAVA CAMPAIGN COMPLETE</h2>
      <p>
        Total Java XP: <strong>{progress.javaXp}</strong>
      </p>
      <div className="campaign-badges" aria-label="Java region badges">
        {progress.badges.map((b) => (
          <span className="earned-badge" key={b.id}>
            ⚑ {b.name}
          </span>
        ))}
      </div>
      <div className="unlock-announcement">
        DSA REALM UNLOCKED
        <br />
        Array Isles available
      </div>
      <button className="primary-button" onClick={onClose}>
        Enter Array Isles →
      </button>
    </dialog>
  );
}
