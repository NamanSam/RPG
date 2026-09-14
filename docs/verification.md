# Milestone 1 verification

Verified on 14 September 2026 in the new `D:\rpg game` workspace.

| Check | Result |
| --- | --- |
| React production build | Passed, 47 modules |
| npm dependency audit after patched versions | 0 reported vulnerabilities |
| Spring Boot integration suite | 3 tests, 0 failures/errors |
| Flyway migration on real MySQL | Passed on isolated CodeQuest MySQL 26.7 instance |
| HTTP registration → restore → logout → login | Passed with real MySQL and cookie jar |
| Actual CSRF token endpoint/cookie round trip | Passed; missing token rejected |
| Browser registration → protected world | Passed with synthetic local account |
| Browser refresh restores session | Passed |
| Browser logout and login | Passed |
| NPC dialogue | Opens with focus in modal and accessible close control |
| Desktop map | Five contiguous environment sections, no course-card grid |
| Mobile, 390 × 844 | Map, NPC controls, and registration flow reviewed |

Integration tests cover initial XP/level, normalized emails, duplicate registration, BCrypt password verification, cookie attributes, unauthenticated requests, validation errors, malformed JWTs, CSRF rejection, and the BCrypt UTF-8 byte limit.

The automated database is H2 with MySQL compatibility mode. The additional live checks verify the actual MySQL migration and authentication flow; they do not claim cross-version certification for every MySQL version.

Quest submission, rewards, progression, and unlock transitions are outside milestone 1 and have not been tested or presented as working. No compiler or arbitrary Java execution is included.

Port 8080 was occupied; CodeQuest uses 8085. Its database uses port 3307 and a separate data directory under `.tools/mysql-data`. Existing services were left running unchanged.
