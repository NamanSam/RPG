# Milestone 2 — final acceptance verification

Verified against the existing repository and live CodeQuest frontend/API/MySQL. No redesign or new region content was added.

A new browser tab opened signed out, and a new synthetic Final Beach Check account was registered. The complete browser journey was verified; direct backend bypass checks used a separate synthetic account through the existing real-HTTP smoke script.

| Requested check | Result |
| --- | --- |
| 1. Register a new user | Passed |
| 2. Starting XP is 0 | Passed: 0 XP and 0/3 complete |
| 3. Quest 1 unlocked | Passed: available marker |
| 4. Quests 2 and 3 initially locked | Passed: disabled markers |
| 5. Incorrect answer gives no XP | Passed: answer `4` to Quest 1 gives +0 XP |
| 6. Quest 1 completion gives +50 XP | Passed: answer `5`, total 50 XP |
| 7. Quest 2 unlocks | Passed: available marker; Quest 3 remains locked |
| 8. Quest 1 replay gives no duplicate XP | Passed: reopened quest, submitted `5`, +0 XP and total remains 50 |
| 9. Complete Quest 2 | Passed: `boolean`, +50 XP, total 100 |
| 10. Complete Quest 3 | Passed: `2`, +75 XP, total 175 |
| 11. Completion celebration | Passed: shown after Celebrate your adventure |
| 12. Beach badge | Passed: Beginner Beach Explorer displayed in celebration and player pass |
| 13. Loop Village unlock | Passed: restored scenery color, active marker, unlocked label and navigation indicator |
| 14. Logout/login | Passed through browser forms |
| 15. Persistence | Passed: 175 XP, 3/3 completed, one badge, and village unlocked after login |
| 16. Locked quest API bypass | Passed: correct answers sent directly to locked Quests 2 and 3 return HTTP 403 |

## Bug fixed

The API client cached a CSRF token across mutations. During the initial browser check, an incorrect answer succeeded, but the following correction received a CSRF rejection. Each mutation now obtains a fresh token. Obsolete cache-reset calls were removed from authentication and world-map code. The incorrect → correct → replay sequence was retested successfully in the browser.

Added a focused API-client regression test that simulates single-use CSRF tokens and checks three consecutive submissions. No automatic replay of state-changing requests and no disabling of CSRF protection were introduced.

## Verification commands

From the project root:

```powershell
node --test frontend/src/services/api.test.js
.\scripts\smoke-beach.ps1
```

From `frontend`:

```powershell
npm.cmd run build
```

All passed. The MySQL smoke test also verifies concurrent final submissions: exactly 75 XP total, one area-completion event, and one badge. No backend source or schema changed during this verification pass.

## Final behavior and scope

Rewards: 50 + 50 + 75 = 175 XP. Incorrect answers and replays give 0 additional XP. Sequential availability, completions, badge, XP, and world unlocks persist in MySQL. The existing level formula keeps a beach graduate at level 1 until 300 XP.

No unresolved failures were found in the requested checks. Loop Village is unlocked but its quests remain outside Milestone 2. Array Forest, OOP Canyon, and Collection Kingdom remain unchanged and locked. Milestone 3 has not started.
