package com.codequest.learning;

import com.codequest.content.*;
import com.codequest.user.PlayerRepository;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class QueuePackIntegrationTest {
 @Autowired PackService packs; @Autowired LearningService learning; @Autowired PlayerRepository players; @Autowired JdbcTemplate db;
 String source(String path) throws Exception {try(var s=new ClassPathResource(path).getInputStream()){return new String(s.readAllBytes(),StandardCharsets.UTF_8);}}
 long user(){var p=players.saveAndFlush(new com.codequest.user.Player("queue-"+UUID.randomUUID()+"@test.dev","x","Station Learner"));db.update("INSERT INTO player_stats(user_id,total_xp,practice_xp) VALUES (?,0,0)",p.getId());db.update("INSERT INTO user_world_unlocks VALUES (?,1,CURRENT_TIMESTAMP)",p.getId());db.update("INSERT INTO user_campaign_progress VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",p.getId());return p.getId();}
 @Test void exactContentDistributionIdentityAndTeachingData() throws Exception {
  var p=packs.validate(source("content/dsa/queues/v1.json"));assertThat(p.questions()).hasSize(50);assertThat(p.trails()).hasSize(5);
  assertThat(p.questions()).extracting(QuestionPack.Question::id).doesNotHaveDuplicates();assertThat(p.questions()).extracting(QuestionPack.Question::slug).doesNotHaveDuplicates();assertThat(p.questions()).extracting(QuestionPack.Question::title).doesNotHaveDuplicates();assertThat(p.questions()).extracting(QuestionPack.Question::description).doesNotHaveDuplicates();
  assertThat(p.questions().stream().filter(q->q.difficulty().equals("Easy")).count()).isEqualTo(20);assertThat(p.questions().stream().filter(q->q.difficulty().equals("Medium")).count()).isEqualTo(20);assertThat(p.questions().stream().filter(q->q.difficulty().equals("Hard")).count()).isEqualTo(10);assertThat(p.questions().stream().mapToInt(QuestionPack.Question::xpReward).sum()).isEqualTo(900);
  for(int n=1;n<=5;n++){final int t=n;var qs=p.questions().stream().filter(q->q.trail()==t).toList();assertThat(qs).hasSize(10);assertThat(qs.stream().filter(q->q.difficulty().equals("Easy")).count()).isEqualTo(4);assertThat(qs.stream().filter(q->q.difficulty().equals("Medium")).count()).isEqualTo(4);assertThat(qs.stream().filter(q->q.difficulty().equals("Hard")).count()).isEqualTo(2);}
  assertThat(p.questions()).allSatisfy(q->{assertThat(q.hints()).isNotEmpty();assertThat(q.explanation()).isNotBlank();});assertThat(packs.publish(source("content/dsa/queues/v1.json"))).isEqualTo("queues-v1");
 }
 @Test void stackGateSequentialProgressRewardsAndBadge() throws Exception {
  long u=user();assertThatThrownBy(()->learning.progress(u,"queues")).isInstanceOf(ResponseStatusException.class);
  var arrays=packs.validate(source("content/dsa/arrays/v1.json"));learning.progress(u,"arrays");for(var q:arrays.questions())learning.submit(u,q.id(),q.expectedAnswer());
  assertThatThrownBy(()->learning.progress(u,"queues")).isInstanceOf(ResponseStatusException.class);
  var prior=packs.validate(source("content/dsa/strings/v1.json"));learning.progress(u,"strings");for(var q:prior.questions())learning.submit(u,q.id(),q.expectedAnswer());
  assertThatThrownBy(()->learning.progress(u,"queues")).isInstanceOf(ResponseStatusException.class);
  var linked=packs.validate(source("content/dsa/linked-lists/v1.json"));learning.progress(u,"linked-lists");for(var q:linked.questions())learning.submit(u,q.id(),q.expectedAnswer());
  assertThatThrownBy(()->learning.progress(u,"queues")).isInstanceOf(ResponseStatusException.class);
  var stack=packs.validate(source("content/dsa/stacks/v1.json"));learning.progress(u,"stacks");for(var q:stack.questions())learning.submit(u,q.id(),q.expectedAnswer());
  var start=learning.progress(u,"queues");assertThat(start.trails()).extracting(LearningService.Trail::unlocked).containsExactly(true,false,false,false,false);assertThat(start.totalXp()).isEqualTo(3600);
  var strings=packs.validate(source("content/dsa/queues/v1.json"));int earned=3600;
  for(var q:strings.questions()){
   if(q.order()%10!=0)assertThatThrownBy(()->learning.detail(u,strings.questions().get(q.order()).id())).isInstanceOf(ResponseStatusException.class);
   var wrong=learning.submit(u,q.id(),"definitely-wrong");assertThat(wrong.awardedXp()).isZero();
   var right=learning.submit(u,q.id(),q.expectedAnswer());earned+=q.xpReward();assertThat(right.awardedXp()).isEqualTo(q.xpReward());assertThat(right.progress().totalXp()).isEqualTo(earned);assertThat(learning.submit(u,q.id(),q.expectedAnswer()).awardedXp()).isZero();
  }
  var done=learning.progress(u,"queues");assertThat(done.completed()).isEqualTo(50);assertThat(done.totalXp()).isEqualTo(4500);assertThat(done.badge()).isEqualTo("Queue Station Conductor");assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='queue-station-conductor'",Integer.class,u)).isEqualTo(1);
 }
}



