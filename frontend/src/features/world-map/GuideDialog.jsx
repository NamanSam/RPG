import { useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import { Npc } from "./PixelScene";

export function GuideDialog({ world, onClose, user }) {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    el.showModal();
    return () => el.close();
  }, []);
  return (
    <dialog
      ref={ref}
      aria-labelledby="guide-title"
      className="guide-dialog"
      onCancel={onClose}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <button
        className="dialog-close"
        aria-label="Close guide"
        onClick={onClose}
      >
        ×
      </button>
      <div className="dialog-npc">
        <Npc />
      </div>
      <p className="eyebrow">{world.role}</p>
      <h2 id="guide-title">{world.npc}</h2>
      <p>
        “
        {world.slug === "beginner-beach"
          ? "Welcome ashore, adventurer. Every great program begins with something small. Let’s make your first line count."
          : world.subtitle}
        ”
      </p>
      <div className="dialog-note">
        <strong>{world.name}</strong>
        <span>{world.topics}</span>
        <p>
          {world.dialogMessage ||
            (world.slug === "beginner-beach"
              ? "Sign in to help Captain Byte through three Java quests and earn your first area badge."
              : "This region’s quests are not playable yet. Continue your adventure in Beginner Beach.")}
        </p>
      </div>
      {!user ? (
        <Link className="primary-button" to="/register">
          Create your adventurer →
        </Link>
      ) : (
        <button className="primary-button" onClick={onClose}>
          Back to the world →
        </button>
      )}
    </dialog>
  );
}
