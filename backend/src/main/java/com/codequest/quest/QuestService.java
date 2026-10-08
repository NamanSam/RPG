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
        String hint, String kind, List<String> options, String worldName) {}
    public record WorldView(String slug, boolean unlocked) {}
    public record BadgeView(String id, String name) {}
    // Preserve the original beach fields and add separate village fields.
    public record Progress(int totalXp, int level, int beachCompleted, List<QuestSummary> quests,
        List<WorldView> worlds, List<BadgeView> badges, int villageCompleted, List<QuestSummary> villageQuests,
        int forestCompleted, List<QuestSummary> forestQuests) {}
    public record Submission(boolean correct, boolean firstCompletion, int awardedXp, String feedback,
        boolean beachCompletedNow, Progress progress, String completedWorld) {}

    private final QuestRepository quests;
    private final JdbcTemplate db;
    public QuestService(QuestRepository quests, JdbcTemplate db) { this.quests = quests; this.db = db; }

    @Transactional(readOnly = true)
    public Progress progress(long userId) { return snapshot(userId); }

    @Transactional(readOnly = true)
    public QuestView detail(long userId, String id) {
        var all = quests.allQuests();
        var definition = definition(id, all);
        var done = completed(userId);
        var unlocked = unlockedWorlds(userId);
        requireAccessible(definition, done, all, unlocked);
        return new QuestView(summary(definition, done, all, unlocked), definition.story(), definition.lesson(), definition.task(),
            definition.starterCode(), definition.hint(), definition.kind(), quests.options(definition), definition.worldName());
    }

    @Transactional
    public Submission submit(long userId, String id, String answer) {
        // All player mutations take this lock before reading completion state.
        // A waiting submission sees the prior transaction's committed progress.
        var player = db.queryForList("SELECT total_xp FROM player_stats WHERE user_id = ? FOR UPDATE", userId);
        if (player.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        var all = quests.allQuests();
        var quest = definition(id, all);
        var done = completed(userId);
        requireAccessible(quest, done, all, unlockedWorlds(userId));
        // Structured tokens/options only. Never parse or execute Java programs.
        boolean correct = answer.strip().equals(quest.expectedAnswer());
        boolean first = correct && !done.contains(id);
        int attemptsUpdated = db.update("UPDATE user_quest_progress SET attempt_count = attempt_count + 1 WHERE user_id = ? AND quest_id = ?", userId, id);
        if (attemptsUpdated == 0) db.update("INSERT INTO user_quest_progress (user_id, quest_id, attempt_count) VALUES (?, ?, 1)", userId, id);
        if (first) {
            db.update("UPDATE user_quest_progress SET completed_at = CURRENT_TIMESTAMP WHERE user_id = ? AND quest_id = ?", userId, id);
            db.update("UPDATE player_stats SET total_xp = total_xp + ? WHERE user_id = ?", quest.reward(), userId);
            done.add(id);
        }
        boolean areaCompletedNow = first && worldComplete(quest.worldId(), done, all);
        if (areaCompletedNow) {
            String badgeId = db.queryForObject("SELECT id FROM badges WHERE world_id = ?", String.class, quest.worldId());
            db.update("INSERT INTO user_badges (user_id, badge_id, earned_at) VALUES (?, ?, CURRENT_TIMESTAMP)", userId, badgeId);
            db.update("INSERT INTO user_world_unlocks (user_id, world_id, unlocked_at) VALUES (?, ?, CURRENT_TIMESTAMP)", userId, quest.worldId()+1);
        }
        String feedback = correct ? quest.successFeedback() : quest.incorrectFeedback();
        if (correct && !first) feedback += " You already earned this quest's XP; practice does not award it again.";
        return new Submission(correct, first, first ? quest.reward() : 0, feedback,
            areaCompletedNow && quest.worldId() == 1, snapshot(userId), areaCompletedNow ? quest.worldSlug() : null);
    }

    private QuestRepository.Definition definition(String id, List<QuestRepository.Definition> all) {
        return all.stream().filter(q -> q.id().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found."));
    }
    private Set<String> completed(long userId) {
        return new HashSet<>(db.queryForList("SELECT quest_id FROM user_quest_progress WHERE user_id = ? AND completed_at IS NOT NULL", String.class, userId));
    }
    private Set<Long> unlockedWorlds(long userId) {
        return new HashSet<>(db.queryForList("SELECT world_id FROM user_world_unlocks WHERE user_id = ?", Long.class, userId));
    }
    private boolean worldComplete(long worldId, Set<String> done, List<QuestRepository.Definition> all) {
        var region = all.stream().filter(q -> q.worldId() == worldId).toList();
        return !region.isEmpty() && region.stream().allMatch(q -> done.contains(q.id()));
    }
    private String status(QuestRepository.Definition q, Set<String> done, List<QuestRepository.Definition> all, Set<Long> unlocked) {
        if (!unlocked.contains(q.worldId()) || (q.worldId() > 1 && java.util.stream.LongStream.range(1,q.worldId()).anyMatch(w -> !worldComplete(w,done,all)))) return "LOCKED";
        if (done.contains(q.id())) return "COMPLETED";
        boolean priorDone = all.stream().filter(p -> p.worldId() == q.worldId() && p.order() < q.order()).allMatch(p -> done.contains(p.id()));
        return priorDone ? "AVAILABLE" : "LOCKED";
    }
    private void requireAccessible(QuestRepository.Definition q, Set<String> done, List<QuestRepository.Definition> all, Set<Long> unlocked) {
        if (status(q, done, all, unlocked).equals("LOCKED"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete the previous area and earlier quests to unlock this quest.");
    }
    private QuestSummary summary(QuestRepository.Definition q, Set<String> done, List<QuestRepository.Definition> all, Set<Long> unlocked) {
        return new QuestSummary(q.id(), q.title(), q.topic(), q.difficulty(), q.reward(), status(q, done, all, unlocked));
    }
    private Progress snapshot(long userId) {
        var xpRows = db.queryForList("SELECT total_xp FROM player_stats WHERE user_id = ?", Integer.class, userId);
        if (xpRows.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        int xp = xpRows.getFirst();
        var done = completed(userId);
        var all = quests.allQuests();
        var unlocked = unlockedWorlds(userId);
        var worldViews = db.query("SELECT id, slug FROM worlds ORDER BY sort_order",
            (rs, row) -> new WorldView(rs.getString("slug"), unlocked.contains(rs.getLong("id"))));
        var badges = db.query("SELECT b.id, b.name FROM badges b JOIN user_badges u ON b.id = u.badge_id WHERE u.user_id = ? ORDER BY b.id",
            (rs, row) -> new BadgeView(rs.getString("id"), rs.getString("name")), userId);
        var beach = all.stream().filter(q -> q.worldId() == 1).map(q -> summary(q, done, all, unlocked)).toList();
        var village = all.stream().filter(q -> q.worldId() == 2).map(q -> summary(q, done, all, unlocked)).toList();
        var forest = all.stream().filter(q -> q.worldId() == 3).map(q -> summary(q, done, all, unlocked)).toList();
        return new Progress(xp, 1 + xp / 300, (int) beach.stream().filter(q -> q.status().equals("COMPLETED")).count(), beach, worldViews, badges,
            (int) village.stream().filter(q -> q.status().equals("COMPLETED")).count(), village,
            (int) forest.stream().filter(q -> q.status().equals("COMPLETED")).count(), forest);
    }
}
