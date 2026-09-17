package com.codequest.quest;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Database-only challenge definition; expected answers never leave the service. */
@Repository
public class QuestRepository {
    public record Definition(String id, long worldId, int order, String title, String topic,
        String difficulty, int reward, String story, String lesson, String task, String starterCode,
        String hint, String kind, String expectedAnswer, String successFeedback, String incorrectFeedback,
        String worldName, String worldSlug) {}
    private final JdbcTemplate db;
    public QuestRepository(JdbcTemplate db) { this.db = db; }
    public List<Definition> allQuests() {
        return db.query("SELECT q.*, w.name AS world_name, w.slug AS world_slug FROM quests q JOIN worlds w ON q.world_id = w.id ORDER BY q.world_id, q.sort_order", (rs, row) ->
            new Definition(rs.getString("id"), rs.getLong("world_id"), rs.getInt("sort_order"),
                rs.getString("title"), rs.getString("topic"), rs.getString("difficulty"), rs.getInt("reward"),
                rs.getString("story"), rs.getString("lesson"), rs.getString("task_text"), rs.getString("starter_code"),
                rs.getString("hint"), rs.getString("answer_kind"), rs.getString("expected_answer"),
                rs.getString("success_feedback"), rs.getString("incorrect_feedback"), rs.getString("world_name"), rs.getString("world_slug")));
    }
    public List<String> options(Definition quest) {
        if (quest.kind().equals("TYPE_CHOICE")) return List.of("int", "double", "boolean", "char");
        return db.queryForList("SELECT answer_value FROM quest_options WHERE quest_id = ? ORDER BY sort_order", String.class, quest.id());
    }
}
