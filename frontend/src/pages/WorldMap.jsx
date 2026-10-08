import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../services/api";
import { worlds } from "../features/world-map/worlds";
import { Npc, PixelScene } from "../features/world-map/PixelScene";
import { GuideDialog } from "../features/world-map/GuideDialog";
import {
  BeachQuestNodes,
  beachPreview,
} from "../features/quests/BeachQuestNodes";
import { QuestDialog } from "../features/quests/QuestDialog";
import { BeachCelebration } from "../features/quests/BeachCelebration";
import {
  VillageQuestNodes,
  villagePreview,
  MillSprite,
} from "../features/quests/VillageQuestNodes";
import { VillageCelebration } from "../features/quests/VillageCelebration";
import { ForestQuestNodes, forestPreview } from "../features/quests/ForestQuestNodes";
import { ForestCelebration } from "../features/quests/ForestCelebration";

export function WorldMap({ user, setUser, preview = false }) {
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const [progress, setProgress] = useState(null);
  const [loadingProgress, setLoadingProgress] = useState(!preview);
  const [questId, setQuestId] = useState(null);
  const [pendingCelebration, setPendingCelebration] = useState(false);
  const [celebrating, setCelebrating] = useState(false);
  const [unlockAnimating, setUnlockAnimating] = useState(false);
  const expired = useCallback(() => {
    setUser(null);
    navigate("/login");
  }, [setUser, navigate]);
  const loadProgress = useCallback(async () => {
    if (!user?.id || preview) return;
    setLoadingProgress(true);
    try {
      const next = await api("/me/progress");
      setProgress(next);
      setError("");
      setUser((previous) =>
        previous
          ? { ...previous, totalXp: next.totalXp, level: next.level }
          : previous,
      );
    } catch (e) {
      if (e.status === 401) expired();
      else setError(e.message);
    } finally {
      setLoadingProgress(false);
    }
  }, [user?.id, preview, expired, setUser]);
  useEffect(() => {
    loadProgress();
  }, [loadProgress]);
  useEffect(() => {
    const refresh = () => {
      if (!questId && !celebrating) loadProgress();
    };
    window.addEventListener("focus", refresh);
    return () => window.removeEventListener("focus", refresh);
  }, [loadProgress, questId, celebrating]);
  useEffect(() => {
    if (!unlockAnimating) return;
    const timer = setTimeout(() => setUnlockAnimating(false), 2400);
    return () => clearTimeout(timer);
  }, [unlockAnimating]);
  const isUnlocked = (world) =>
    world.slug === "beginner-beach" ||
    (!preview &&
      progress?.worlds.some((w) => w.slug === world.slug && w.unlocked));
  function receiveResult(result) {
    setProgress(result.progress);
    setUser((previous) => ({
      ...previous,
      totalXp: result.progress.totalXp,
      level: result.progress.level,
    }));
    if (result.completedWorld) setPendingCelebration(result.completedWorld);
  }
  function closeQuest() {
    setQuestId(null);
    if (pendingCelebration) {
      setPendingCelebration(false);
      setCelebrating(pendingCelebration);
    }
  }
  function finishCelebration() {
    const index = worlds.findIndex(w=>w.slug===celebrating);
    const nextWorld = worlds[index+1].slug;
    setCelebrating(false);
    setUnlockAnimating(nextWorld);
    requestAnimationFrame(() => {
      const zone = document.getElementById(nextWorld);
      zone?.focus({ preventScroll: true });
      zone?.scrollIntoView({
        behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches
          ? "instant"
          : "smooth",
      });
    });
  }
  async function logout() {
    try {
      await api("/auth/logout", { method: "POST" });
      setUser(null);
      navigate("/preview");
    } catch (e) {
      setError(e.message);
    }
  }
  return (
    <div className={`game-shell ${!preview ? "active-adventure" : ""}`}>
      <header className="topbar">
        <Link className="brand" to="/">
          <span className="brand-icon">{"{ }"}</span>
          <span>
            CODEQUEST <small>RPG</small>
          </span>
        </Link>
        <nav aria-label="Main navigation">
          <a className="nav-active" href="#world-top">
            World map
          </a>
          <a href="#travel-guide">Field guide</a>
          {!preview && <Link to="/dsa">DSA crossing</Link>}
        </nav>
        <div className="account">
          {user ? (
            <>
              <span>{user.displayName}</span>
              <button className="text-button" onClick={logout}>
                Sign out
              </button>
            </>
          ) : (
            <Link className="text-button" to="/login">
              Sign in <span aria-hidden="true">↗</span>
            </Link>
          )}
        </div>
      </header>
      {error && (
        <p className="error-banner" role="alert">
          {error}{" "}
          {!preview && (
            <button className="text-button" onClick={loadProgress}>
              Retry progress
            </button>
          )}
        </p>
      )}
      <main id="world-top">
        <section className="intro">
          <div>
            <p className="eyebrow">
              <span className="online-dot" /> A NEW ADVENTURE AWAITS
            </p>
            <h1>
              Small steps. <br />
              Legendary <em>code.</em>
            </h1>
            <p className="intro-copy">
              From your first variable to a kingdom of collections.
              <br className="desktop-break" /> Learn Java, one quest at a time.
            </p>
            <a className="primary-button" href="#beginner-beach">
              Explore the world <span aria-hidden="true">↓</span>
            </a>
          </div>
          <div className="adventure-pass">
            <div className="pass-top">
              <span>ADVENTURER’S PASS</span>
              <span aria-hidden="true">✦</span>
            </div>
            <div className="pass-player">
              <div className="portrait">
                <Npc />
              </div>
              <div>
                <small>
                  {user ? "WELCOME, ADVENTURER" : "YOUR STORY STARTS HERE"}
                </small>
                <strong>{user?.displayName || "Java adventurer"}</strong>
                <span>Level {user?.level || 1} · Novice explorer</span>
              </div>
            </div>
            <div className="xp-label">
              <span>Experience</span>
              <span>{(user?.totalXp || 0) % 300} / 300 XP</span>
            </div>
            <div className="xp-track">
              <div style={{ width: `${((user?.totalXp || 0) % 300) / 3}%` }} />
            </div>
            {user?.totalXp >= 300 && (
              <div className="xp-total">
                Total XP: {user.totalXp} · progress toward level{" "}
                {user.level + 1}
              </div>
            )}
            {!preview && (
              <div className="beach-progress" aria-live="polite">
                {loadingProgress
                  ? "Loading your progress…"
                  : progress
                    ? `Beginner Beach · ${progress.beachCompleted} / 3 complete`
                    : "Progress unavailable"}
                {progress && (
                  <span className="village-progress-line">
                    Loop Village · {progress.villageCompleted} / 3 complete
                  </span>
                )}
                {progress && <span className="forest-progress-line">Array Forest · {progress.forestCompleted} / 3 complete</span>}
                {progress?.badges.map((badge) => (
                  <span className="earned-badge" key={badge.id}>
                    {badge.id === "loop-village" ? <MillSprite badge /> : "⚑"}{" "}
                    {badge.name}
                  </span>
                ))}
              </div>
            )}
            <p>
              {preview
                ? "Map preview · Sign in to save your adventure"
                : "Your adventure is saved to your account."}
            </p>
          </div>
        </section>
        <div className="map-heading">
          <div>
            <span className="eyebrow">THE JAVA REALM</span>
            <h2>A world of possibilities</h2>
          </div>
          <span className="map-meta">
            5 regions <i /> 9 playable quests
          </span>
        </div>
        <div className="map-layout">
          <aside className="region-nav" aria-label="World regions">
            <span className="eyebrow">YOUR JOURNEY</span>
            {worlds.map((world, i) => (
              <a key={world.slug} href={`#${world.slug}`}>
                <span
                  className={
                    isUnlocked(world) ? "region-dot current" : "region-dot"
                  }
                >
                  {isUnlocked(world) ? "◆" : "◇"}
                </span>
                <span>
                  <small>REGION {world.region}</small>
                  {world.name}
                </span>
              </a>
            ))}
            <div className="guide-note" id="travel-guide">
              <span aria-hidden="true">✧</span>
              <h3>
                A little courage.
                <br />A little curiosity.
              </h3>
              <p>
                Follow the path. Meet your guides. Every concept brings you
                closer to the next region.
              </p>
              <p className="milestone-note">
                {preview
                  ? "Sign in to begin"
                  : progress?.forestQuests?.some(q=>q.status==='AVAILABLE') ? "Array Forest" : progress?.villageQuests?.some(q=>q.status==='AVAILABLE') ? "Loop Village" : "Beginner Beach"}
                <br />
                Follow the markers to your next horizon.
              </p>
            </div>
          </aside>
          <div className="world-scroll">
            {worlds.map((world, i) => (
              <section
                className={`world-section ${world.theme} ${!isUnlocked(world) ? "zone-locked" : ""} ${world.slug === unlockAnimating ? "area-unlocking" : ""}`}
                id={world.slug}
                key={world.slug}
                aria-label={world.name}
                tabIndex={-1}
              >
                <PixelScene theme={world.theme} />
                <div className="world-caption">
                  <span className="world-kicker">
                    REGION {world.region} <span> / </span> {world.quests} QUESTS
                  </span>
                  <h2>{world.name}</h2>
                  <p>{world.subtitle}</p>
                </div>
                {i === 0 ? (
                  <BeachQuestNodes
                    quests={(!preview && progress?.quests) || beachPreview}
                    busy={!preview && (loadingProgress || !progress)}
                    onSelect={(id) => {
                      if (preview) {
                        if (user) navigate("/world");
                        else setSelected(world);
                      } else setQuestId(id);
                    }}
                  />
                ) : i === 1 ? (
                  <VillageQuestNodes
                    quests={
                      (!preview && progress?.villageQuests) || villagePreview
                    }
                    worldUnlocked={!!isUnlocked(world)}
                    busy={!preview && (loadingProgress || !progress)}
                    onSelect={setQuestId}
                  />
                ) : i === 2 ? (
                  <ForestQuestNodes quests={(!preview && progress?.forestQuests) || forestPreview} busy={!preview && (loadingProgress || !progress)} onSelect={setQuestId} />
                ) : null}
                {!isUnlocked(world) && i > 0 && (
                  <div className="zone-status">
                    <span aria-hidden="true">◇</span> Complete{" "}
                    {worlds[i - 1].name} to unlock
                  </div>
                )}
                <div className="world-footer">
                  <span>{world.topics}</span>
                  <span>
                    {i===2 && !preview && progress?.forestCompleted === 3 ? "✓ FOREST COMPLETE" : i === 0
                      ? !preview && progress?.beachCompleted === 3
                        ? "✓ BEACH COMPLETE"
                        : "✦ START HERE"
                      : i === 1 && !preview && progress?.villageCompleted === 3
                        ? "✓ VILLAGE COMPLETE"
                        : isUnlocked(world)
                          ? "◆ UNLOCKED"
                          : "◇ UNDISCOVERED"}
                  </span>
                </div>
              </section>
            ))}
            <div className="map-end">
              {!preview && <div className="dsa-portal"><p>Complete the Java campaign to cross the sea.</p><Link to="/dsa">View the DSA crossing ◇</Link></div>}
              <span aria-hidden="true">✦</span>
              <p>Every expert was once a beginner.</p>
              <span>YOUR ADVENTURE IS JUST GETTING STARTED</span>
            </div>
          </div>
        </div>
      </main>
      <footer className="site-footer">
        <span>CODEQUEST RPG</span>
        <span>Made for curious minds. Built one quest at a time.</span>
        <span>THE JAVA CAMPAIGN</span>
      </footer>
      {selected && (
        <GuideDialog
          world={selected}
          onClose={() => setSelected(null)}
          user={user}
        />
      )}
      {questId && (
        <QuestDialog
          key={questId}
          id={questId}
          onClose={closeQuest}
          onResult={receiveResult}
          onExpired={expired}
        />
      )}
      {celebrating &&
        (celebrating === "array-forest" ? <ForestCelebration totalXp={progress.totalXp} badge={progress.badges.find(b=>b.id===celebrating)?.name} onClose={finishCelebration}/> : celebrating === "loop-village" ? (
          <VillageCelebration
            totalXp={progress.totalXp}
            onClose={finishCelebration}
          />
        ) : (
          <BeachCelebration onClose={finishCelebration} />
        ))}
    </div>
  );
}
