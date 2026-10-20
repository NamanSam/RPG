# Beginner Beach Practice Trails — 20 October 2026

Implemented only Beginner Beach practice. All existing story quests remain milestone missions. No practice content was added to Loop Village, Array Forest, OOP Canyon or Collection Kingdom. No new DSA topic was added. No commit was created; awaiting approval.

## Content inventory

| Concept | Easy | Medium | Hard | Total | Practice XP |
| --- | ---: | ---: | ---: | ---: | ---: |
| Variables | 4 | 4 | 2 | 10 | 180 |
| Data Types | 4 | 4 | 2 | 10 | 180 |
| Operators | 4 | 4 | 2 | 10 | 180 |
| Input / Output | 4 | 4 | 2 | 10 | 180 |
| Mixed Beginner Challenge | 4 | 4 | 2 | 10 | 180 |
| Total | 20 | 20 | 10 | 50 | 900 |

There are 12 multiple-choice, 29 predict-output, 5 code-blank and 4 structured logic questions. Eleven identify classic beginner patterns, including swapping values, digit extraction/reversal, overflow, Scanner's line remainder, averaging, parity, temperature conversion, maximum selection and integer ceiling division. Every question has a unique title/description, concept, difficulty, type, ordered trail placement, original explanation, hint, example, constraints, validation and reward. Easy questions precede Medium, then Hard within each concept.

## Common architecture

The unchanged QuestionPack.Question model, AnswerValidator, PackService publisher, question/version/validation tables, per-user question progress and shared React QuestionPanel serve both Java and DSA. No question has its own React component. Both packs publish automatically at ordinary startup; the existing validate/import workflow still works.

PackService now derives the number of trails and questions from the pack instead of requiring exactly five trails globally. Each trail still requires ten questions with a 4/4/2 difficulty distribution and the existing 10/20/30 rewards. Immutable published versions, stable identities, pinned assignments, private validators and existing answer types are retained. LearningService completion counts are likewise derived from the assigned pack rather than hardcoded to fifty questions or the fifth trail.

New topic policies select XP pool, sequential-question behavior and independent trails. Topics without an override retain the existing DSA behavior: account XP, sequential trail unlocks and all ten questions available within an open trail. Array Isles content was not changed.

Beach uses the same detail, submit, progress and trail endpoints. Metadata identifies the region and whether XP is practice XP; the shared question panel labels these accurately. No code is executed. The island guide/return labels and success text remain unchanged for DSA.

## Database changes

V10__java_practice_trails.sql:

- Adds nonnegative player_stats.practice_xp, default 0, preserving total_xp and existing players.
- Creates topic_learning_rules with a topic FK and XP/progression options.
- Creates topic_trail_requirements linking a topic/trail to an existing story quest and optional prior-practice-trail requirement.
- Registers beach-practice in Beginner Beach's Java campaign region.
- Registers the separate beginner-beach-mastery topic badge, named Beginner Beach Mastery.

The shared publisher imports 50 question identities, one immutable versioned pack, five trail definitions and public/private question data into the existing tables. No second question engine, duplicate progress tables or story migration changes were introduced.

## Progression and XP

First Variable opens Variables. Choose the Type opens Data Types. Operator Training opens Operators and Input / Output. Those four concept locations can be practiced independently once their story prerequisites are met. Final Beginner Trial opens after all four are complete, with Operator Training complete. Inside every Beach practice trail, only the first unfinished question is available; completed questions can be replayed. Both detail and submit APIs enforce these locks before recording attempts.

Practice awards 10/20/30 once per Easy/Medium/Hard completion to practice_xp and records awarded_xp in the shared per-question progress. Incorrect answers and replays award 0. Practice never changes total_xp, Java story XP, player level, story badges, region unlocks or campaign completion. Topic progress exposes topicXp, practiceXp and account totalXp separately.

The first completion of a trail produces a small RPG celebration and completed location marker. Completing 50/50 records one Beginner Beach Mastery badge in user_topic_badges and the shared topic completion timestamp. It does not mark the Java campaign complete or unlock another region. The existing player_stats row lock and primary keys protect XP, badge and completion state against concurrent submissions. Three simultaneous final submissions were tested: their combined award is 30 XP and exactly one mastery event occurs.

Story progression remains optional with respect to mastery: Beach story still awards 175 XP; all five Java story regions still award exactly 2025 XP and unlock DSA. A regression test earns practice XP, leaves mastery at 1/50, then completes all Java regions and enters Array Isles normally. The separate mastery badge does not replace Beginner Beach Explorer.

## RPG interface

The existing Beach map says STORY QUESTS. Directly below it, the attached Beginner Beach training sands are clearly labeled PRACTICE TRAILS, with camps, a workshop, messenger pier, shrine, NPC markers and pixel paths. Locations display concept progress out of ten; the area displays mastery out of fifty and its separate practice XP. A trail journal shows a marked route of ten data-driven encounters. This is optional training geography, not a question-bank dashboard.

The shared question panel supports multiple-choice, output prediction, code blanks and structured results. It shows feedback, explanation and XP animation, then returns to the trail. Completed trails and final mastery have small dialogs. Return to camp returns to the location map; saved counters refresh on focus and after story progress changes. Loading progress is labeled explicitly rather than briefly reporting zero mastery. Environmental animation respects reduced motion.

## Files created

- backend/src/main/resources/db/migration/V10__java_practice_trails.sql
- backend/src/test/java/com/codequest/learning/BeachPracticeIntegrationTest.java
- content/java/beginner-beach/v1.json
- frontend/src/features/practice/BeachPractice.jsx
- frontend/src/features/practice/practice.css
- scripts/smoke-beach-practice.ps1
- docs/beach-practice-verification.md

## Files modified

- backend/src/main/java/com/codequest/content/ContentLoader.java
- backend/src/main/java/com/codequest/content/PackService.java
- backend/src/main/java/com/codequest/learning/LearningService.java
- frontend/src/features/questions/QuestionPanel.jsx
- frontend/src/pages/WorldMap.jsx

## Verification

- Final H2 backend suite: 25 reported, 24 passed, one opt-in fixture skipped, zero failures/errors. Log: .tools/practice-tests-final.log.
- MySQL 26.7 full integration suite: 25 reported, 24 passed, one opt-in fixture skipped, zero failures/errors. Log: .tools/practice-mysql-tests.log.
- Six new tests cover exact inventory/distribution, unique question text and reusable types, immutable/idempotent publication, private answer fields, story gates, sequential detail/submit locks, independent concept locations, final-trial prerequisites, cross-user isolation, all fifty incorrect/correct/replay scenarios, concurrent mastery award, MySQL persistence, logout/login, story progression with practice incomplete, normal DSA access and independently calculated tricky answers.
- Existing Beginner Beach, Loop Village, Array Forest, OOP Canyon, Collection Kingdom, authentication and Array Isles regressions pass. The Array Isles tests still exercise all fifty DSA questions and concurrent completion, and retain their original XP/unlock behavior.
- Fresh-account HTTP/MySQL practice smoke: 50/50, 900 practice XP, 175 story XP; wrong answers, sequential lock rejection, every reward, every replay, badge and logout/login persistence passed. Log: .tools/practice-http-smoke.log.
- Full Java-to-DSA HTTP smoke: all sixteen story quests, exactly 2025 Java XP, five story badges, Java completion, DSA and normal Array Isles access passed with no practice requirement.
- Frontend CSRF regression: one passed.
- Final production build: passed, 69 modules. The runner loader and fresh ignored output folder avoid the machine's existing Vite-temp/dist write restrictions. Output: .tools/practice-build-final.
- Browser: NPC map and progress labels render; a wrong multiple-choice answer gives 0, correction gives 10 practice XP and replay gives 0 while account XP stays 175. Output, code-blank and JSON-array questions submit through the shared panel. Sequential markers unlock correctly. Variables 10/10 produces PRACTICE TRAIL COMPLETE at 180 practice XP. The final logic answer produces BEGINNER BEACH MASTERY BADGE at 50/50 and 900 practice XP, with account XP still 175. Intermediate answers were completed through HTTP; representative inputs and completion interactions were tested in the browser.
- git diff --check passed. No outstanding failures.

Screenshot proof is saved locally in .tools/beach-mastery-complete.png (ignored artifact).

## Fixes and limits

Removed global five-trail/fifty-question completion assumptions and island-specific labels from shared behavior where needed for Java practice. Added separate XP accounting and server-enforced per-question locks. Visual checks also corrected loading mastery text and the completion dialog's Return to camp behavior. No previous story quest or DSA pack was modified. Only Beginner Beach has Java practice content in this milestone.

## Manual reproduction

1. Start the dedicated MySQL, scripts/backend.ps1 run and the frontend dev server. Register an ordinary account.
2. Before First Variable, practice locations are locked. Complete First Variable; open Variables and verify only its first question is available.
3. Submit a wrong choice, then int supplies = 6;, then replay it. Practice XP is 0 → 10 → 10; story XP stays 50. The second Variables question becomes available.
4. Complete the remaining Beach story quests; Loop Village opens at 175 story XP even when mastery is incomplete. Data Types, Operators and Input / Output open at their story milestones.
5. Complete all ten questions in a concept and inspect the small trail celebration. After all four concept trails, complete Final Beginner Trial for 50/50, 900 practice XP and the separate mastery badge.
6. Sign out and back in; counters, completed markers and badge remain. Replays still award zero. Continue the Java story normally and confirm its final total remains 2025.

Automated reproduction:

    ./scripts/backend.ps1 test
    ./scripts/smoke-beach-practice.ps1
    ./scripts/smoke-kingdom.ps1
    node --test frontend/src/services/api.test.js
    npm --prefix frontend run build -- --configLoader runner --outDir ../.tools/practice-review-build

The practice smoke script creates synthetic accounts via normal public APIs. -StopAfter 0 prepares a fresh Beach-complete account for browser testing; -Email can resume only its dedicated practice-smoke accounts. No fixture or direct database progress grant is used. Test accounts remain in local MySQL.

This milestone is safe to commit based on passing backend, MySQL, HTTP, frontend and browser checks. Story progression is intact. No commit was made. Stop here; await user approval before expanding practice to another region.
