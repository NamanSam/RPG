package com.codequest.quest;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestService {
    public record QuestSummary(String id, String title, String topic, String difficulty, int reward, String status) {}
    public record QuestView(QuestSummary quest, String story, String lesson, String task, String starterCode,
        String hint, String kind, List<String> options) {}
    public record WorldView(String slug, boolean unlocked) {}
    public record BadgeView(String id, String name) {}
    public record Progress(int totalXp, int level, int beachCompleted, List<QuestSummary> quests,
        List<WorldView> worlds, List<BadgeView> badges) {}
    public record Submission(boolean correct, boolean firstCompletion, int awardedXp, String feedback,
        boolean beachCompletedNow, Progress progress) {}

    private final QuestRepository quests;
    private final JdbcTemplate db;
    public QuestService(QuestRepository quests, JdbcTemplate db) { this.quests = quests; this.db = db; }

    @Transactional(readOnly = true)
    public Progress progress(long userId) { return snapshot(userId); }

    @Transactional(readOnly = true)
    public QuestView detail(long userId, String id) {
        var definition = definition(id);
        var done = completed(userId);
        requireAccessible(userId, definition, done);
        return new QuestView(summary(definition, done), definition.story(), definition.lesson(), definition.task(),
            definition.starterCode(), definition.hint(), definition.kind(),
            definition.kind().equals("TYPE_CHOICE") ? List.of("int", "double", "boolean", "char") : List.of());
    }

    @Transactional
    public Submission submit(long userId, String id, String answer) {
        // Every progress mutation for a player takes this same row lock first.
        // Concurrent requests cannot both observe an unfinished quest and award XP.
        var player = db.queryForList("SELECT total_xp FROM player_stats WHERE user_id = ? FOR UPDATE", userId);
        if (player.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        var quest = definition(id);
        var done = completed(userId);
        requireAccessible(userId, quest, done);
        boolean correct = answer.strip().equals(quest.expectedAnswer());
        boolean first = correct && !done.contains(id);
        int attemptsUpdated = db.update("UPDATE user_quest_progress SET attempt_count = attempt_count + 1 WHERE user_id = ? AND quest_id = ?", userId, id);
        if (attemptsUpdated == 0) db.update("INSERT INTO user_quest_progress (user_id, quest_id, attempt_count) VALUES (?, ?, 1)", userId, id);
        if (first) {
            db.update("UPDATE user_quest_progress SET completed_at = CURRENT_TIMESTAMP WHERE user_id = ? AND quest_id = ?", userId, id);
            db.update("UPDATE player_stats SET total_xp = total_xp + ? WHERE user_id = ?", quest.reward(), userId);
            done.add(id);
        }
        boolean beachCompletedNow = first && done.size() == 3;
        if (beachCompletedNow) {
            db.update("INSERT INTO user_badges (user_id, badge_id, earned_at) VALUES (?, 'beginner-beach', CURRENT_TIMESTAMP)", userId);
            db.update("INSERT INTO user_world_unlocks (user_id, world_id, unlocked_at) VALUES (?, 2, CURRENT_TIMESTAMP)", userId);
        }
        String feedback = correct ? quest.successFeedback() : quest.incorrectFeedback();
        if (correct && !first) feedback += " You already earned this quest's XP; practice does not award it again.";
        return new Submission(correct, first, first ? quest.reward() : 0, feedback, beachCompletedNow, snapshot(userId));
    }

    private QuestRepository.Definition definition(String id) {
        return quests.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found."));
    }
    private Set<String> completed(long userId) {
        return new HashSet<>(db.queryForList("SELECT p.quest_id FROM user_quest_progress p JOIN quests q ON q.id = p.quest_id WHERE p.user_id = ? AND q.world_id = 1 AND p.completed_at IS NOT NULL", String.class, userId));
    }
    private boolean unlocked(long userId, long worldId) {
        return db.queryForObject("SELECT COUNT(*) FROM user_world_unlocks WHERE user_id = ? AND world_id = ?", Integer.class, userId, worldId) > 0;
    }
    private String status(QuestRepository.Definition q, Set<String> done) {
        if (done.contains(q.id())) return "COMPLETED";
        boolean priorDone = quests.beachQuests().stream().filter(p -> p.order() < q.order()).allMatch(p -> done.contains(p.id()));
        return priorDone ? "AVAILABLE" : "LOCKED";
    }
    private void requireAccessible(long userId, QuestRepository.Definition q, Set<String> done) {
        if (!unlocked(userId, q.worldId()) || status(q, done).equals("LOCKED"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete the earlier beach quests to unlock this quest.");
    }
    private QuestSummary summary(QuestRepository.Definition q, Set<String> done) {
        return new QuestSummary(q.id(), q.title(), q.topic(), q.difficulty(), q.reward(), status(q, done));
    }
    private Progress snapshot(long userId) {
        var xpRows = db.queryForList("SELECT total_xp FROM player_stats WHERE user_id = ?", Integer.class, userId);
        if (xpRows.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        int xp = xpRows.getFirst();
        var done = completed(userId);
        var worldViews = db.query("SELECT w.slug, CASE WHEN u.user_id IS NULL THEN FALSE ELSE TRUE END AS unlocked FROM worlds w LEFT JOIN user_world_unlocks u ON w.id = u.world_id AND u.user_id = ? ORDER BY w.sort_order",
            (rs, row) -> new WorldView(rs.getString("slug"), rs.getBoolean("unlocked")), userId);
        var badges = db.query("SELECT b.id, b.name FROM badges b JOIN user_badges u ON b.id = u.badge_id WHERE u.user_id = ? ORDER BY b.id",
            (rs, row) -> new BadgeView(rs.getString("id"), rs.getString("name")), userId);
        return new Progress(xp, 1 + xp / 300, done.size(), quests.beachQuests().stream().map(q -> summary(q, done)).toList(), worldViews, badges);
    }
}
