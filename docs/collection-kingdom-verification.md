# Collection Kingdom — verification, 20 October 2026

Collection Kingdom and the normal Java → DSA crossing are complete. No additional DSA topic was started. No commit was created; awaiting approval.

## Quests and XP

| Quest | Topic | Difficulty | Reward | Java XP after completion |
| --- | --- | --- | ---: | ---: |
| The Dynamic Scroll | ArrayList | Easy | 150 | 1650 |
| The Key Keeper | HashMap | Medium | 175 | 1825 |
| Hall of Uniques | Set / HashSet | Medium | 200 | 2025 |

Starting Java XP is 1500. Kingdom adds 525, yielding exactly 2025. The lessons cover list creation/add/get/remove/size; map key/value pairs/put/get/containsKey; and set creation/add/contains/duplicates/unique membership. Challenges use the existing structured integer answer validation; no Java execution was introduced.

Kingdom has a dedicated original SVG pixel castle, towers, banners, courtyard, stone paths and archive/registry buildings. Scroll Keeper, Key Keeper and Keeper of Sets open the shared quest panel from map locations. Available, locked and completed markers track the server response. Environmental animation respects reduced-motion settings.

## Completion and persistence

The first quest requires OOP Canyon and all preceding Java regions. Later quests require earlier Kingdom quests. Both quest detail and submission endpoints reject locked access. Incorrect answers and replays award zero XP.

The existing player_stats row lock serializes mutations. The final quest atomically awards 200 XP, records one Kingdom badge, completes the Java campaign row and inserts the DSA campaign unlock row. Existing Java campaign rows are completed without duplicating them; already-complete timestamps are preserved. Replays do not update completion timestamps or unlocks. The final Java region no longer tries to unlock a nonexistent sixth world.

Normal Array Isles access uses the existing LearningService prerequisite check against Java campaign completion. No fixture, direct database grant, special email or demo account is needed. Array Isles content and validators are unchanged. Its locked-map message now accurately names the five-region Java prerequisite.

Progress exposes kingdomCompleted, kingdomQuests, javaCampaignComplete, dsaUnlocked and javaXp in addition to the existing fields. javaXp is calculated from completed Java quests, so DSA XP does not inflate the Java campaign total. After the first Array Isles encounter, account XP is 2035 while Java XP remains 2025.

Closing the final quest opens COLLECTION KINGDOM COMPLETE / JAVA CAMPAIGN COMPLETE, 2025 Java XP, all five region badges, DSA REALM UNLOCKED and Array Isles available. Its entry button navigates to the working DSA map. Returning to Java retains the completed campaign status and DSA entry link.

## Database changes

V9__collection_kingdom.sql is the only new migration. It restores missing Kingdom quest definitions and badge if absent, sets the planned quest count to three, and expands the lessons and examples. It does not alter existing player progress or schemas. Campaign and badge state use the existing tables and primary keys. Earlier migrations are unchanged.

## Files created

- backend/src/main/resources/db/migration/V9__collection_kingdom.sql
- backend/src/test/java/com/codequest/quest/CollectionKingdomIntegrationTest.java
- frontend/src/features/quests/KingdomQuestNodes.jsx
- frontend/src/features/quests/KingdomCelebration.jsx
- frontend/src/features/quests/kingdom.css
- scripts/smoke-kingdom.ps1
- docs/collection-kingdom-verification.md

## Files modified

- backend/src/main/java/com/codequest/quest/QuestRepository.java — exposes Region 5 definitions.
- backend/src/main/java/com/codequest/quest/QuestService.java — Kingdom progress, final campaign completion and DSA unlock.
- backend/src/main/java/com/codequest/learning/LearningService.java — updated locked crossing message.
- backend/src/test/java/com/codequest/auth/AuthIntegrationTest.java — unique registration email retains case normalization/conflict checks and allows repeated runs against persistent MySQL.
- frontend/src/pages/WorldMap.jsx — Kingdom map interactions, progress and campaign celebration/DSA navigation.
- frontend/src/features/world-map/worlds.js — correct three-quest count.

## Tests

- Full backend H2 suite: 19 reported; 18 passed, one opt-in demo fixture skipped; zero failures/errors. Final log: .tools/kingdom-tests-final.log.
- Full backend MySQL 26.7 suite: 19 reported; 18 passed, one opt-in demo fixture skipped; zero failures/errors. Log: .tools/kingdom-mysql-tests.log.
- Both suites include Beginner Beach, Loop Village, Array Forest, OOP Canyon, authentication and all fifty Array Isles answer/progression regressions.
- New normal-account journey test: registration → all four earlier Java regions → Kingdom → exactly 2025 XP → five badges → Java complete → DSA → Array Isles → first correct answer → logout/login. Checks locks throughout the journey, wrong answer, replay, three simultaneous final submissions, one completion event, one campaign row, one DSA unlock row, unchanged completion timestamp and Java XP after DSA earnings.
- HTTP/MySQL scripts/smoke-kingdom.ps1: all 16 Java quest rewards, wrong answers, replay and lock rejection; five badges; campaign completion; ordinary Array Isles enrollment and login persistence passed.
- HTTP/MySQL scripts/smoke-auth.ps1: registration, JWT cookie, restore, logout, login and CSRF rejection passed.
- Frontend CSRF regression: one test passed.
- Final production build: passed, 67 modules. Used runner config loader and a fresh ignored output folder to avoid the existing machine's Vite-temp/dist write restrictions. Output: .tools/kingdom-build-final.
- git diff --check: passed.
- Browser: normal account earned the first 1500 XP through public quest APIs, then completed all three Kingdom quests through NPCs and the visible quest panels. Verified initial two locked quests, wrong answer +0 XP, first quest +150 XP, replay +0 XP, next unlocks, +175 XP and +200 XP. Final celebration displays 2025, all five badges and unlocked DSA. Enter Array Isles opened the normal map with Trail 1 available and later trails locked; submitting crates.length to its first question earned +10 XP and displayed 2035 account XP. No fixture was used.

Screenshot proof: ../.tools/kingdom-complete.png (ignored local artifact).

No outstanding failures. The first live smoke attempt reached the previous API process and received 404 for Kingdom; restarting that known project process with the current code resolved it. No unrelated service was stopped. Synthetic test accounts remain in local MySQL.

## Bugs fixed

- Region 5 was excluded from the quest repository and had no map interactions.
- Final-region completion would attempt an invalid sixth-world unlock.
- Normal Java progression never recorded the prerequisite campaign completion needed by DSA.
- Kingdom map count was two instead of three.
- The DSA gate said OOP Canyon and Kingdom were unplayable.
- Existing auth integration test's fixed email prevented repeat MySQL runs (test-only fix).
- Badge contrast on the campaign celebration was corrected after visual inspection.

## Manual steps and reproduction

1. Start the dedicated CodeQuest MySQL, scripts/backend.ps1 run and frontend npm run dev. Register a fresh ordinary account.
2. Complete Beach, Village, Forest and Canyon; verify 1500 XP. Before finishing Canyon, Kingdom and direct Kingdom quest APIs should return locked/403. DSA question detail and submission must return 403 until Kingdom is finished.
3. At Scroll Keeper, try 4 (+0), then 3 (+150; 1650). Replay for +0. Key Keeper should unlock while Keeper of Sets remains locked.
4. At Key Keeper answer 7 (+175; 1825). At Keeper of Sets answer 3 (+200; 2025).
5. Close the final quest and inspect the campaign celebration, five badges and 2025 Java XP. Enter Array Isles and verify Trail 1 is playable. Answer the first question with crates.length; total account XP becomes 2035.
6. Sign out and back in. Java campaign stays complete, all five badges remain, Java XP stays 2025, and Array Isles retains its progress. Replays award no additional XP.

Repeat automated checks:

    ./scripts/backend.ps1 test
    ./scripts/smoke-kingdom.ps1
    ./scripts/smoke-auth.ps1
    node --test frontend/src/services/api.test.js
    npm --prefix frontend run build -- --configLoader runner --outDir ../.tools/kingdom-review-build

The smoke script's optional -PrepareBrowser stops after the first thirteen quests at 1500 XP and prints credentials for its newly registered synthetic account; it grants no fixture state. The full integration suite can target MySQL by supplying spring.datasource.url/username/password through Maven system properties using the existing .env values.

Collection Kingdom is safe to commit based on passing H2, MySQL, HTTP, frontend and browser checks. Previous Java regions and Array Isles pass their regressions. No additional DSA topic, application redesign or unrelated region change was made. Awaiting approval before committing.
