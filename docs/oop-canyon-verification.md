# OOP Canyon verification — 20 October 2026

Implemented Blueprint Maker (Classes and Objects, Easy, 125 XP), The Hidden Vault (Encapsulation, Medium, 150 XP), Bloodline of Classes (Inheritance, Medium, 175 XP), and Many Forms (Polymorphism, Hard, 200 XP).

The existing structured choice validator is used; no Java execution was added. Canyon exposes four NPC/location interactions in a dedicated vector pixel scene with cliffs, a bridge, stone ruins, shrine runes and subtle dust animation. Reduced motion is respected. Existing quest feedback and XP animation remain shared. Completion displays OOP CANYON COMPLETE, the OOP Canyon Architect badge and Collection Kingdom unlocked.

XP: 850 -> 975 -> 1125 -> 1300 -> 1500. Wrong answers and replays give zero. Existing player-row transaction locking serializes concurrent submissions. API access requires all prior regions and earlier quests. Collection Kingdom remains excluded from the playable quest repository; no new DSA content was added.

## Files

Created:
- backend/src/main/resources/db/migration/V6__oop_canyon_lessons.sql
- backend/src/main/resources/db/migration/V7__restore_missing_canyon_definitions.sql
- backend/src/main/resources/db/migration/V8__restore_canyon_badge.sql
- backend/src/test/java/com/codequest/quest/OopCanyonIntegrationTest.java
- frontend/src/features/quests/CanyonQuestNodes.jsx
- frontend/src/features/quests/CanyonCelebration.jsx
- frontend/src/features/quests/canyon.css
- docs/oop-canyon-verification.md

Modified:
- backend/src/main/java/com/codequest/quest/QuestRepository.java
- backend/src/main/java/com/codequest/quest/QuestService.java
- frontend/src/features/world-map/worlds.js
- frontend/src/pages/WorldMap.jsx

## Database and fixes

V6 expands the Canyon lessons and examples to cover fields/methods/object use, getters/setters and inherited state/behavior. V7 restores only missing Canyon quest and option rows. V8 restores a missing Canyon badge and corrects its planned count. These are forward migrations; earlier migration files and player progress are preserved. The existing local MySQL database reported V5 applied while lacking its Canyon quests and badge; the repair migrations resolve that discrepancy. No schema changes or progress resets were required. Synthetic integration/smoke accounts remain in the local database.

Fixed API exclusion of Canyon, missing map interactions and completion screen, stale two-quest map count, and small-screen label positioning.

## Validation

- Full H2 backend suite: 18 reported, 17 passed, one opt-in demo fixture skipped, zero failures/errors (final rerun after all migrations).
- MySQL 26.7 integration: 14 tests passed, zero failures/errors: Canyon, Beginner Beach, Loop Village, Array Forest, Array Isles question pack. Covers direct API locks, sequential availability, wrong answers, all rewards, replay prevention, concurrent final submissions, exactly one badge, Collection Kingdom unlock, logout/login persistence.
- Authentication HTTP/MySQL smoke: registration, cookie, session restore, logout/login and CSRF rejection passed.
- Frontend CSRF regression: one test passed.
- Production build passed (64 modules). Existing dist and Vite temporary folders have local write restrictions; final command uses a fresh ignored output directory and the runner config loader.
- Browser preview: Canyon has four disabled NPCs before unlock; rocky scene, bridge, ruins and guide labels render. Browser testing covered map layout/locked state; the full interactive answer/completion sequence is verified by integration tests and remains a manual UI acceptance step.
- git diff --check passed.

Intermediate failures resolved: generated test quest-ID typo; overlapping Maven invocations caused transient missing compiled classes (rerun serially); local missing Canyon seed/badge data; malformed initial repair SQL corrected before successful migration. Full-suite repeat runs against persistent MySQL also hit the existing auth test's fixed Knight@example.com account (409); auth was verified separately through the unique-account HTTP smoke script. No application authentication change was needed.

## Reproduce and manual acceptance

Run scripts/backend.ps1 test. For production output run:

    npm --prefix frontend run build -- --configLoader runner --outDir ../.tools/canyon-build-review
    node --test frontend/src/services/api.test.js
    ./scripts/smoke-auth.ps1

With the local MySQL/API/Vite services running, register a fresh account, complete Beach, Village and Forest and confirm 850 XP. Canyon should enable only The Architect. Try an incorrect choice (zero XP), then choose Bridge bridge = new Bridge(); for 975 XP. Replay it (zero XP). At Vault Keeper choose private for 1125 XP; at Lineage Guardian choose extends for 1300 XP; at Shape Master choose Rope for 1500 XP. Close the quest panel to see the Canyon celebration and one badge. Continue to the unlocked Collection Kingdom map placeholder. Sign out and back in; verify four completed Canyon markers, badge and 1500 XP persist. Before each unlock, try opening/submitting the next quest API and expect 403.

Previous Java regions and Array Isles pass their integration regressions. Collection Kingdom unlocks but has no playable content added. No commit was created. Safe to commit subject to the remaining manual UI acceptance check; awaiting user approval.
