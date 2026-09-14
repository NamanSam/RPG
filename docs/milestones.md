# CodeQuest RPG milestones

## Scope

React + Vite, Java + Spring Boot, MySQL, Spring Security + JWT. One scrollable pixel world with five connected environments. Twelve curated quests: Beach 3, Village 3, Forest 2, Canyon 2, Kingdom 2. No compiler, movement controls, multiplayer, chat, leaderboard, inventory, AI tutor, or admin panel.

## Milestone 1 — foundation (Day 1)

- Original pixel scenery and a continuous vertical map across all five regions.
- Animated guide NPCs, quest markers, water, fireflies, and locked-region previews.
- Register/login/logout, cookie authentication, CSRF, initial player stats.
- MySQL schema migration and seeded geography.
- Local startup instructions and authentication tests.
- Public `/preview` supports visual review without a running database.

Map lock states are explicit preview data in this milestone. NPC dialogues introduce the guides; they do not submit answers or award XP. No learning progress is simulated.

## Milestone 2 — playable vertical slice (Days 2–4)

- Quest schema and curated beginner content.
- Server-checked code blanks and multiple choice; no code execution.
- Authenticated world/quest endpoints; server-authoritative availability.
- Quest lesson, hint, submission, and feedback views.
- Transactional first-completion XP; replay and concurrent requests cannot double-award.
- Next quest and area unlocks, including XP and unlock animations.
- End-to-end browser verification of register → map → NPC → quest → answer → feedback → XP → next area.

The entire core flow must work by Day 4. Unlocking Loop Village is the acceptance test for this milestone.

## Milestone 3 — full content and player progress (Day 5)

Complete all 12 quests and add five world badges and the profile/progress page. Two OOP quests introduce classes/objects and encapsulation/inheritance/polymorphism; they are introductory coverage, not a replacement for a full OOP course.

## Milestone 4 — release (Days 6–7)

Accessibility/responsive polish, grading review, access/reward tests, production configuration, deployment, smoke tests, and documentation. Avoid feature additions on Day 7.

## Reward rules

100 XP for first completion. No incorrect-answer penalty or replay rewards. Level = 1 + floor(totalXp / 300); twelve quests yield 1,200 XP and level 5. Finish each world's quests to open the next region. Badges follow world completion.
