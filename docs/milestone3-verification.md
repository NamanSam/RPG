# Milestone 3 — Loop Village verification

Verified 2026-09-16 against the existing repository at b66c9d0, which already contains the Loop Village implementation. No application changes were needed during this final verification pass. Only this report and README were added/updated. No Milestone 4 work was performed.

## Implemented files

Already committed implementation:

- `backend/src/main/java/com/codequest/quest/QuestRepository.java`: world-aware quest definitions and structured options.
- `backend/src/main/java/com/codequest/quest/QuestService.java`: village progression, access checks, transactional XP/badge/unlock awards; preserves beach response fields.
- `backend/src/main/resources/db/migration/V3__loop_village_quests.sql`: three village quests, eight choices in the new `quest_options` table, and Loop Village Pathfinder badge.
- `backend/src/test/java/com/codequest/quest/VillageProgressIntegrationTest.java`: progression, security, persistence and concurrency coverage.
- `frontend/src/features/quests/QuestDialog.jsx`: world-specific headings and accessible code-choice answers.
- `frontend/src/features/quests/VillageQuestNodes.jsx`: gatekeeper, animated mill, well, wooden paths and quest markers.
- `frontend/src/features/quests/VillageCelebration.jsx`: completion dialog and pixel badge.
- `frontend/src/features/quests/village.css`: village objects, effects, responsive layout and reduced-motion support.
- `frontend/src/pages/WorldMap.jsx`: village interactions, progression, badges and forest map unlock; level-relative XP bar plus total XP.
- `scripts/smoke-village.ps1`: full HTTP/MySQL walkthrough, lock bypass attempts and concurrent final submissions.

V3 is additive. Existing beach migrations and player data are retained. Progress uses the existing player stats, quest progress, earned badge and world unlock tables. Answers are compared against fixed server-side tokens/options; Java is never executed. Player-row locking serializes submissions, and database keys enforce unique awards.

## Results

| Check | Result |
| --- | --- |
| Backend suite | PASS: 9 tests, zero failures/errors (3 auth, 3 beach, 3 village) |
| Frontend production build | PASS: Vite build, 54 modules |
| Frontend CSRF regression | PASS: 1 test, incorrect/correct/replay mutations fetch fresh tokens |
| Beginner Beach HTTP/MySQL smoke | PASS: all 10 checks and concurrent final completion |
| Loop Village HTTP/MySQL smoke | PASS: all 16 checks and concurrent final completion |
| Browser quest flow | PASS: incorrect feedback, +75/+100/+125, replay +0, sequential unlocks, celebration, badge and forest unlock |
| Browser logout/login | PASS: 475 XP, both areas 3/3, both badges and forest unlock persisted |
| Visual checks | PASS: desktop village/celebration and 390px mobile village/code-choice dialog |

The browser test account was created fresh during the initial walkthrough on September 14 (0 XP, village locked), completed the first two beach quests, and was resumed on September 16. Its 100 XP survived the service restart. The remaining beach quest and all village quests were then completed through the browser. Today's smoke tests each created a separate fresh account and exercised the full progression without relying on that earlier browser state.

Security checks include locked GET/POST returning 403 before region/quest prerequisites, no progression from rejected requests, authentication and CSRF enforcement, and no duplicate XP/badge/unlock under concurrent submissions. Incorrect answers and replays award zero XP. Forest stays locked until all three village quests are complete. Canyon and Kingdom remain locked.

Initial verification attempts encountered a stopped local API and a sandbox file-permission error writing Vite temporary files. The CodeQuest-only database/API were restarted and the build rerun with filesystem access. Both then passed. The existing frontend was reused after detecting that port 5173 was already occupied. No application defect was found in the final pass; no Beginner Beach behavior changed.

## XP progression

| Completion | Award | Total |
| --- | ---: | ---: |
| New account | 0 | 0 |
| First Variable | 50 | 50 |
| Choose the Type | 50 | 100 |
| Operator Training | 75 | 175 |
| The Gatekeeper | 75 | 250 |
| The Looping Mill | 100 | 350 |
| The Endless Well | 125 | 475 |

Final level is 2. The XP bar shows 175/300 progress toward level 3 and separately displays total XP 475.

## Reproduce automated checks

From `D:\rpg game`, with PowerShell 7 and the local database/API running:

```powershell
.\scripts\backend.ps1 test
.\scripts\smoke-beach.ps1
.\scripts\smoke-village.ps1
```

From `D:\rpg game\frontend`:

```powershell
npm.cmd run build
node --test src/services/api.test.js
```

Backend test log for this pass: `.tools/milestone3-final-tests.log` (ignored local artifact).

## Manual test

1. Start the database, API and frontend using the README instructions if they are not running. Open `http://127.0.0.1:5173/register` and create a new account.
2. Confirm 0 XP, only beach Quest 1 available, and all village quests locked.
3. Complete beach quests in order: First Variable `5`, Choose the Type `boolean`, Operator Training `2`. Confirm 175 XP, beach badge and beach celebration. Click **See Loop Village**.
4. Confirm the Gatekeeper is available while the mill and well are locked.
5. Click the Gatekeeper. Choose `coins > 10` and submit: incorrect feedback, +0 XP. Choose `coins >= 10` and submit: +75 XP, total 250.
6. Click **Practice again**: +0 XP. Return to the map. Confirm the mill is now available and the well remains locked.
7. Click the mill and choose `for (int i = 0; i < 3; i++)`. Submit: +100 XP, total 350, level 2. Return to the map; the well is available.
8. Click the well. Try `7`: +0 XP. Submit `8`: +125 XP, total 475.
9. Click **Celebrate your adventure**. Confirm **LOOP VILLAGE COMPLETE**, Loop Village Pathfinder badge, 300 village XP and 475 total XP. Click **See Array Forest** and confirm its map section unlocks.
10. Sign out and sign back in. Confirm 475 total XP, both areas 3/3 complete, both badges and Array Forest unlocked. Replay any quest: XP remains 475.
11. Run `scripts/smoke-village.ps1` to reproduce direct API lock-bypass and concurrent submission checks using a separate fresh account.

No known blocking Milestone 3 issues remain. Array Forest is unlocked geography only; no forest, canyon or kingdom challenges were added. Synthetic verification accounts remain in the local CodeQuest database. Milestone 4 requires user approval.
