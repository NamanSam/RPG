package com.codequest.learning;

import com.codequest.content.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Service
public class LearningService {
    private final JdbcTemplate db;
    public LearningService(JdbcTemplate db) { this.db=db; }
    public record Marker(String id, String title, String difficulty, int xpReward, String status, int order) {}
    public record Trail(int number, String name, boolean unlocked, int completed, List<Marker> questions) {}
    public record TopicProgress(String topic, String pack, int totalXp, int level, int completed, int unlockedTrail,
        boolean complete, String badge, boolean successorEligible, List<Trail> trails) {}
    public record Result(boolean correct, int awardedXp, boolean firstCompletion, String feedback,
        String explanation, boolean trailCompletedNow, boolean topicCompletedNow, TopicProgress progress) {}
    private boolean campaignOpen(long user, String campaign) {
        var prerequisite=db.queryForList("SELECT prerequisite FROM campaigns WHERE slug=?",String.class,campaign);
        if(prerequisite.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Campaign not found.");
        return prerequisite.getFirst()==null || db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=? AND campaign_slug=? AND completed_at IS NOT NULL",Integer.class,user,prerequisite.getFirst())>0;
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> campaigns(long user) {
        return db.query("SELECT slug,name FROM campaigns ORDER BY slug DESC",(rs,n)->Map.<String,Object>of("slug",rs.getString(1),"name",rs.getString(2),"unlocked",campaignOpen(user,rs.getString(1))));
    }
    @Transactional(readOnly=true)
    public Map<String,Object> map(long user) {
        boolean open=campaignOpen(user,"dsa");
        return Map.of("unlocked",open,"message",open?"Follow the island trails.":"Complete the Java campaign to cross this sea. OOP Canyon and Collection Kingdom are not playable yet.","regions",db.queryForList("SELECT r.slug,r.name,t.slug AS topic FROM campaign_regions r JOIN topics t ON t.region_slug=r.slug WHERE r.campaign_slug='dsa' ORDER BY r.sort_order"));
    }
    private void playerLock(long user) {
        if(db.queryForList("SELECT total_xp FROM player_stats WHERE user_id=? FOR UPDATE",user).isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again.");
    }
    private String enroll(long user,String topic) {
        var rows=db.queryForList("SELECT t.active_pack,r.campaign_slug FROM topics t JOIN campaign_regions r ON r.slug=t.region_slug WHERE t.slug=?",topic);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic not found.");
        String campaign=(String)rows.getFirst().get("campaign_slug");
        if(!campaignOpen(user,campaign)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Complete the prerequisite campaign first.");
        if(db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=? AND campaign_slug=?",Integer.class,user,campaign)==0)
            db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at) VALUES (?,?,CURRENT_TIMESTAMP)",user,campaign);
        var assigned=db.queryForList("SELECT pack_id FROM user_topic_progress WHERE user_id=? AND topic_slug=?",String.class,user,topic);
        if(!assigned.isEmpty()) return assigned.getFirst();
        String pack=(String)rows.getFirst().get("active_pack");
        if(pack==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic is not published.");
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail) VALUES (?,?,?,1)",user,topic,pack);
        return pack;
    }
    private String topicFor(String id) {
        var rows=db.queryForList("SELECT topic_slug FROM questions WHERE id=?",String.class,id);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found."); return rows.getFirst();
    }
    private int unlocked(long user,String topic) { return db.queryForObject("SELECT unlocked_trail FROM user_topic_progress WHERE user_id=? AND topic_slug=?",Integer.class,user,topic); }
    private Map<String,Object> questionRow(long user,String id,String topic,String pack) {
        var rows=db.queryForList("SELECT * FROM question_versions WHERE pack_id=? AND question_id=?",pack,id);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not in assigned pack.");
        var q=rows.getFirst();
        if(((Number)q.get("trail_number")).intValue()>unlocked(user,topic)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Complete the previous trail first."); return q;
    }
    @Transactional
    public TopicProgress progress(long user,String topic) { playerLock(user); String pack=enroll(user,topic); return snapshot(user,topic,pack); }
    @Transactional
    public Trail trail(long user,String topic,int number) {
        var p=progress(user,topic);
        if(number<1 || number>5) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Trail not found.");
        if(number>p.unlockedTrail()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Complete the previous trail first.");
        return p.trails().get(number-1);
    }
    @Transactional
    public JsonNode detail(long user,String id) {
        playerLock(user); String topic=topicFor(id),pack=enroll(user,topic);
        return PackService.JSON.readTree((String)questionRow(user,id,topic,pack).get("public_content"));
    }
    @Transactional
    public Result submit(long user,String id,String answer) {
        playerLock(user); String topic=topicFor(id),pack=enroll(user,topic);
        var q=questionRow(user,id,topic,pack);
        var validation=db.queryForMap("SELECT * FROM question_validations WHERE pack_id=? AND question_id=?",pack,id);
        boolean correct=AnswerValidator.matches((String)validation.get("validator"),(String)validation.get("expected_answer"),answer);
        boolean done=db.queryForObject("SELECT COUNT(*) FROM user_question_progress WHERE user_id=? AND question_id=? AND completed_at IS NOT NULL",Integer.class,user,id)>0;
        boolean first=correct&&!done;
        if(db.update("UPDATE user_question_progress SET attempt_count=attempt_count+1 WHERE user_id=? AND question_id=?",user,id)==0)
            db.update("INSERT INTO user_question_progress(user_id,question_id,attempt_count) VALUES (?,?,1)",user,id);
        int reward=first?((Number)q.get("xp_reward")).intValue():0;
        if(first) {
            db.update("UPDATE user_question_progress SET completed_at=CURRENT_TIMESTAMP,awarded_xp=? WHERE user_id=? AND question_id=?",reward,user,id);
            db.update("UPDATE player_stats SET total_xp=total_xp+? WHERE user_id=?",reward,user);
        }
        int trail=((Number)q.get("trail_number")).intValue();
        int count=db.queryForObject("SELECT COUNT(*) FROM question_versions v JOIN user_question_progress p ON p.question_id=v.question_id WHERE v.pack_id=? AND v.trail_number=? AND p.user_id=? AND p.completed_at IS NOT NULL",Integer.class,pack,trail,user);
        boolean trailNow=first&&count==10, topicNow=trailNow&&trail==5;
        if(trailNow&&trail<5) db.update("UPDATE user_topic_progress SET unlocked_trail=? WHERE user_id=? AND topic_slug=?",trail+1,user,topic);
        if(topicNow) {
            db.update("UPDATE user_topic_progress SET completed_at=CURRENT_TIMESTAMP WHERE user_id=? AND topic_slug=?",user,topic);
            db.update("INSERT INTO user_topic_badges(user_id,badge_id,earned_at) SELECT ?,id,CURRENT_TIMESTAMP FROM topic_badges WHERE topic_slug=?",user,topic);
        }
        return new Result(correct,reward,first,correct?(first?"Correct! Your island grows brighter.":"Correct! Already earned; replay awards 0 XP."):"Not quite. Check the scenario and your answer format, then try again.",
            correct?(String)validation.get("explanation"):"",trailNow,topicNow,snapshot(user,topic,pack));
    }
    private TopicProgress snapshot(long user,String topic,String pack) {
        int open=unlocked(user,topic),xp=db.queryForObject("SELECT total_xp FROM player_stats WHERE user_id=?",Integer.class,user);
        Set<String> done=new HashSet<>(db.queryForList("SELECT question_id FROM user_question_progress WHERE user_id=? AND completed_at IS NOT NULL",String.class,user));
        var trails=db.query("SELECT trail_number,name FROM question_trails WHERE pack_id=? ORDER BY trail_number",(rs,n)->{
            int number=rs.getInt(1);
            var markers=db.query("SELECT question_id,public_content,difficulty,xp_reward,sort_order FROM question_versions WHERE pack_id=? AND trail_number=? ORDER BY sort_order",(qr,i)->new Marker(qr.getString(1),PackService.JSON.readTree(qr.getString(2)).get("title").asText(),qr.getString(3),qr.getInt(4),number>open?"LOCKED":done.contains(qr.getString(1))?"COMPLETED":"AVAILABLE",qr.getInt(5)),pack,number);
            return new Trail(number,rs.getString(2),number<=open,(int)markers.stream().filter(m->m.status().equals("COMPLETED")).count(),markers);
        },pack);
        int completed=trails.stream().mapToInt(Trail::completed).sum();
        var badges=db.queryForList("SELECT b.name FROM topic_badges b JOIN user_topic_badges u ON b.id=u.badge_id WHERE u.user_id=? AND b.topic_slug=?",String.class,user,topic);
        return new TopicProgress(topic,pack,xp,1+xp/300,completed,open,completed==50,badges.isEmpty()?"":badges.getFirst(),completed==50,trails);
    }
}
