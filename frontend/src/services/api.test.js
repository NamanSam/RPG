import test from "node:test";
import assert from "node:assert/strict";
import { api } from "./api.js";

test("incorrect answer, correction, and replay each fetch a fresh CSRF token", async (t) => {
  let issued = 0;
  let submissions = 0;
  const used = new Set();
  t.mock.method(globalThis, "fetch", async (url, options) => {
    if (url === "/api/auth/csrf") {
      issued++;
      return Response.json({
        headerName: "X-XSRF-TOKEN",
        token: `token-${issued}`,
      });
    }
    assert.equal(url, "/api/quests/first-variable/submit");
    const token = options.headers["X-XSRF-TOKEN"];
    if (used.has(token))
      return Response.json({ message: "Stale CSRF token" }, { status: 403 });
    assert.equal(token, `token-${issued}`);
    used.add(token);
    submissions++;
    return Response.json({
      correct: submissions > 1,
      awardedXp: submissions === 2 ? 50 : 0,
    });
  });
  const submit = (answer) =>
    api("/quests/first-variable/submit", {
      method: "POST",
      body: JSON.stringify({ answer }),
    });
  assert.deepEqual(await submit("4"), { correct: false, awardedXp: 0 });
  assert.deepEqual(await submit("5"), { correct: true, awardedXp: 50 });
  assert.deepEqual(await submit("5"), { correct: true, awardedXp: 0 });
  assert.equal(issued, 3);
});
