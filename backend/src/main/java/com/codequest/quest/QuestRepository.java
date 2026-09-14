package com.codequest.quest;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Database-only challenge definition; expected answers never leave the service. */
@Repository
public class QuestRepository {
    public record Definition(String id, long worldId, int order, String title, String topic,
        String difficulty, int reward, String story, String lesson, String task, String starterCode,
        String hint, String kind, String expectedAnswer, String successFeedback, String incorrectFeedback) {}
    private final JdbcTemplate db;
    public QuestRepository(JdbcTemplate db) { this.db = db; }
    public List<Definition> beachQuests() {
        return db.query("SELECT * FROM quests WHERE world_id = 1 ORDER BY sort_order", (rs, row) ->
            new Definition(rs.getString("id"), rs.getLong("world_id"), rs.getInt("sort_order"),
                rs.getString("title"), rs.getString("topic"), rs.getString("difficulty"), rs.getInt("reward"),
                rs.getString("story"), rs.getString("lesson"), rs.getString("task_text"), rs.getString("starter_code"),
                rs.getString("hint"), rs.getString("answer_kind"), rs.getString("expected_answer"),
                rs.getString("success_feedback"), rs.getString("incorrect_feedback")));
    }
    public Optional<Definition> find(String id) { return beachQuests().stream().filter(q -> q.id().equals(id)).findFirst(); }
}
