package com.codequest.learning;

import com.codequest.content.*;
import com.codequest.quest.*;
import com.codequest.user.PlayerRepository;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class OopPracticeIntegrationTest {
 @Autowired PackService packs; @Autowired LearningService learning; @Autowired QuestService story;
 @Autowired QuestRepository quests; @Autowired PlayerRepository players; @Autowired JdbcTemplate db;
 String source() throws Exception {try(var s=new ClassPathResource("content/java/oop-canyon/v1.json").getInputStream()){return new String(s.readAllBytes(),StandardCharsets.UTF_8);}}
 long user(){var p=players.saveAndFlush(new com.codequest.user.Player("oop-practice-"+UUID.randomUUID()+"@test.dev","x","OOP Learner"));db.update("INSERT INTO player_stats(user_id,total_xp,practice_xp) VALUES (?,0,0)",p.getId());db.update("INSERT INTO user_world_unlocks VALUES (?,1,CURRENT_TIMESTAMP)",p.getId());return p.getId();}
 void canyon(long u){for(var q:quests.allQuests().stream().filter(q->q.worldId()<=4).toList())story.submit(u,q.id(),q.expectedAnswer());}
 @Test void exactContentDistributionAndIdentity() throws Exception {
  var p=packs.validate(source());assertThat(p.questions()).hasSize(50);assertThat(p.trails()).hasSize(5);
  assertThat(p.questions()).extracting(QuestionPack.Question::id).doesNotHaveDuplicates();assertThat(p.questions()).extracting(QuestionPack.Question::slug).doesNotHaveDuplicates();
  assertThat(p.questions()).extracting(QuestionPack.Question::title).doesNotHaveDuplicates();assertThat(p.questions()).extracting(QuestionPack.Question::description).doesNotHaveDuplicates();
  assertThat(p.questions().stream().filter(q->q.difficulty().equals("Easy")).count()).isEqualTo(20);assertThat(p.questions().stream().filter(q->q.difficulty().equals("Medium")).count()).isEqualTo(20);assertThat(p.questions().stream().filter(q->q.difficulty().equals("Hard")).count()).isEqualTo(10);
  assertThat(p.questions().stream().mapToInt(QuestionPack.Question::xpReward).sum()).isEqualTo(900);
  for(int n=1;n<=5;n++){final int trail=n;var q=p.questions().stream().filter(x->x.trail()==trail).toList();assertThat(q).hasSize(10);assertThat(q.stream().filter(x->x.difficulty().equals("Easy")).count()).isEqualTo(4);assertThat(q.stream().filter(x->x.difficulty().equals("Medium")).count()).isEqualTo(4);assertThat(q.stream().filter(x->x.difficulty().equals("Hard")).count()).isEqualTo(2);}
  assertThat(p.questions()).extracting(QuestionPack.Question::questionType).contains("MULTIPLE_CHOICE","PREDICT_OUTPUT","CODE_BLANK","LOGIC_ANSWER");assertThat(packs.publish(source())).isEqualTo("oop-canyon-practice-v1");
 }
 @Test void sequentialRewardsMasteryAndStoryRemainIndependent() throws Exception {
  long u=user();assertThat(learning.progress(u,"oop-canyon-practice").trails()).noneMatch(LearningService.Trail::unlocked);canyon(u);
  var start=learning.progress(u,"oop-canyon-practice");assertThat(start.trails()).extracting(LearningService.Trail::unlocked).containsExactly(true,true,true,true,false);assertThat(story.progress(u).totalXp()).isEqualTo(1500);
  var pack=packs.validate(source());int earned=0;
  for(var q:pack.questions()){
   if(q.order()%10!=1){var marker=learning.progress(u,"oop-canyon-practice").trails().get(q.trail()-1).questions().get((q.order()-1)%10);assertThat(marker.status()).isEqualTo("AVAILABLE");}
   var wrong=learning.submit(u,q.id(),"definitely-wrong");assertThat(wrong.awardedXp()).isZero();
   var right=learning.submit(u,q.id(),q.expectedAnswer());earned+=q.xpReward();assertThat(right.awardedXp()).isEqualTo(q.xpReward());assertThat(right.progress().practiceXp()).isEqualTo(earned);assertThat(right.progress().totalXp()).isEqualTo(1500);assertThat(learning.submit(u,q.id(),q.expectedAnswer()).awardedXp()).isZero();
  }
  var done=learning.progress(u,"oop-canyon-practice");assertThat(done.completed()).isEqualTo(50);assertThat(done.topicXp()).isEqualTo(900);assertThat(done.practiceXp()).isEqualTo(900);assertThat(done.badge()).isEqualTo("OOP Canyon Mastery");assertThat(done.successorEligible()).isFalse();
  assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='oop-canyon-mastery'",Integer.class,u)).isEqualTo(1);assertThat(story.progress(u).totalXp()).isEqualTo(1500);assertThat(story.progress(u).worlds().stream().filter(w->w.slug().equals("collection-kingdom")).findFirst().orElseThrow().unlocked()).isTrue();
 }
}




