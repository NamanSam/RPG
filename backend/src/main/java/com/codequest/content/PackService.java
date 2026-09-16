package com.codequest.content;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PackService {
    public static final JsonMapper JSON = JsonMapper.builder().build();
    private final JdbcTemplate db;
    public PackService(JdbcTemplate db) { this.db = db; }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalArgumentException(message); }
    public QuestionPack validate(String source) {
        var p = JSON.readValue(source, QuestionPack.class);
        check(p.topic() != null && p.topic().matches("[a-z][a-z0-9-]{1,59}") && p.version() > 0, "Invalid topic/version");
        check(p.trails() != null && p.trails().size() == 5 && p.questions() != null && p.questions().size() == 50, "A published topic requires 5 trails and 50 questions");
        Set<String> ids = new HashSet<>(), slugs = new HashSet<>(); Set<Integer> orders = new HashSet<>(), trails = new HashSet<>();
        for (var t : p.trails()) check(t.number() >= 1 && t.number() <= 5 && trails.add(t.number()) && t.name() != null && !t.name().isBlank(), "Invalid/duplicate trail");
        for (var q : p.questions()) {
            check(q.id() != null && q.id().matches("[a-z0-9-]{1,100}") && ids.add(q.id()) && q.slug() != null && q.slug().matches("[a-z0-9-]{1,100}") && slugs.add(q.slug()), "Invalid/duplicate question identity");
            check(p.topic().equals(q.topic()) && q.order() >= 1 && q.order() <= 50 && orders.add(q.order()) && q.trail() == (q.order()-1)/10+1, "Invalid question order/trail/topic");
            check(Set.of("Easy","Medium","Hard").contains(q.difficulty()), "Invalid difficulty");
            check(q.xpReward() == switch(q.difficulty()) { case "Easy" -> 10; case "Medium" -> 20; default -> 30; }, "Unexpected XP reward");
            check(q.title() != null && !q.title().isBlank() && q.description() != null && !q.description().isBlank() && q.subtopic() != null && !q.subtopic().isBlank(), "Missing question text");
            check(q.examples() != null && !q.examples().isEmpty() && q.constraints() != null && !q.constraints().isEmpty() && q.hints() != null && !q.hints().isEmpty() && q.explanation() != null && !q.explanation().isBlank() && q.tags() != null && !q.tags().isEmpty(), "Missing teaching content");
            var allowed = switch(q.questionType()) {
                case "MULTIPLE_CHOICE" -> Set.of("OPTION");
                case "PREDICT_OUTPUT" -> Set.of("OUTPUT","INTEGER");
                case "CODE_BLANK" -> Set.of("TOKEN");
                case "LOGIC_ANSWER", "CODING_CHALLENGE" -> Set.of("INTEGER","INT_LIST","INT_MATRIX","TOKEN");
                default -> Set.<String>of();
            };
            check(allowed.contains(q.validator()) && q.expectedAnswer() != null && AnswerValidator.matches(q.validator(), q.expectedAnswer(), q.expectedAnswer()), "Unsupported/invalid answer validation");
            check(q.options() != null, "Options must be an array");
            if (q.questionType().equals("MULTIPLE_CHOICE")) {
                var optionIds = new HashSet<String>();
                check(q.options().size() >= 2, "Choices required");
                for (var o : q.options()) check(o.id() != null && optionIds.add(o.id()) && o.text() != null && !o.text().isBlank(), "Invalid choice");
                check(optionIds.contains(q.expectedAnswer()), "Answer is not an option ID");
            }
        }
        for (int t=1;t<=5;t++) { final int n=t; var qs=p.questions().stream().filter(q->q.trail()==n).toList();
            check(qs.size()==10 && qs.stream().filter(q->q.difficulty().equals("Easy")).count()==4 && qs.stream().filter(q->q.difficulty().equals("Medium")).count()==4 && qs.stream().filter(q->q.difficulty().equals("Hard")).count()==2, "Each trail requires 4 Easy, 4 Medium, 2 Hard"); }
        return p;
    }
    @Transactional
    public String publish(String source) {
        var p=validate(source); String id=p.topic()+"-v"+p.version();
        var topic=db.queryForList("SELECT slug FROM topics WHERE slug=? FOR UPDATE",p.topic());
        check(!topic.isEmpty(), "Unknown topic");
        String checksum;
        try { checksum=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(JSON.writeValueAsString(p).getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalArgumentException("Cannot hash pack",e); }
        var existing=db.queryForList("SELECT checksum FROM question_packs WHERE id=?",String.class,id);
        if (!existing.isEmpty()) { check(existing.getFirst().equals(checksum),"Published versions are immutable; increase version"); return id; }
        var prior=db.queryForList("SELECT question_id FROM question_versions WHERE pack_id=(SELECT active_pack FROM topics WHERE slug=?)",String.class,p.topic());
        // Keep the same 50 identities in revisions so completion/XP cannot reset.
        if(!prior.isEmpty()) check(new HashSet<>(prior).equals(new HashSet<>(p.questions().stream().map(QuestionPack.Question::id).toList())),"Revisions must preserve question identities");
        db.update("INSERT INTO question_packs VALUES (?,?,?,?,?)",id,p.topic(),p.version(),checksum,"PUBLISHED");
        for(var t:p.trails()) db.update("INSERT INTO question_trails VALUES (?,?,?)",id,t.number(),t.name());
        for(var q:p.questions()) {
            var identity=db.queryForList("SELECT topic_slug,slug FROM questions WHERE id=?",q.id());
            if(identity.isEmpty()) db.update("INSERT INTO questions VALUES (?,?,?)",q.id(),p.topic(),q.slug());
            else check(identity.getFirst().get("topic_slug").equals(p.topic()) && identity.getFirst().get("slug").equals(q.slug()),"Question identity cannot change");
            var publicData=JSON.valueToTree(q).deepCopy();
            ((tools.jackson.databind.node.ObjectNode)publicData).remove(List.of("expectedAnswer","validator","explanation"));
            db.update("INSERT INTO question_versions VALUES (?,?,?,?,?,?,?)",id,q.id(),q.trail(),q.order(),q.difficulty(),q.xpReward(),JSON.writeValueAsString(publicData));
            db.update("INSERT INTO question_validations VALUES (?,?,?,?,?)",id,q.id(),q.validator(),q.expectedAnswer(),q.explanation());
        }
        db.update("UPDATE topics SET active_pack=? WHERE slug=?",id,p.topic()); return id;
    }
}
