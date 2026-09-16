package com.codequest.learning;

import com.codequest.auth.*;
import com.codequest.content.*;
import com.codequest.quest.QuestService;
import com.codequest.user.PlayerRepository;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ArrayPackIntegrationTest {
    @Autowired MockMvc mvc; @Autowired LearningService learning; @Autowired PackService packs;
    @Autowired JdbcTemplate db; @Autowired PlayerRepository players; @Autowired QuestService quests;
    record Account(long id,String email,Cookie cookie) {}
    String source() throws Exception { try(var s=new ClassPathResource("content/dsa/arrays/v1.json").getInputStream()){return new String(s.readAllBytes(),StandardCharsets.UTF_8);} }
    Account user() throws Exception {
        String email="array-"+UUID.randomUUID()+"@codequest.test";
        var r=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","arrays-test-password","displayName","Island Tester")))).andExpect(status().isCreated()).andReturn();
        return new Account(players.findByEmail(email).orElseThrow().getId(),email,r.getResponse().getCookie(JwtCookieFilter.COOKIE));
    }
    void grantFixture(Account u) { db.update("INSERT INTO user_campaign_progress VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",u.id()); }
    @Test void campaignAndTrailLocksAreServerEnforced() throws Exception {
        var u=user();
        mvc.perform(get("/api/questions/arrays-01")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/campaigns/dsa/map").cookie(u.cookie())).andExpect(jsonPath("$.unlocked").value(false));
        mvc.perform(get("/api/questions/arrays-01").cookie(u.cookie())).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/arrays-01/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"b\"}")).andExpect(status().isForbidden());
        quests.submit(u.id(),"first-variable","5");quests.submit(u.id(),"choose-the-type","boolean");quests.submit(u.id(),"operator-training","2");
        quests.submit(u.id(),"the-gatekeeper","coins >= 10");quests.submit(u.id(),"the-looping-mill","for (int i = 0; i < 3; i++)");quests.submit(u.id(),"the-endless-well","8");
        mvc.perform(get("/api/questions/arrays-01").cookie(u.cookie())).andExpect(status().isForbidden());
        assertThat(quests.progress(u.id()).totalXp()).isEqualTo(475);
        grantFixture(u);
        mvc.perform(get("/api/questions/arrays-01").cookie(u.cookie())).andExpect(status().isOk()).andExpect(jsonPath("$.expectedAnswer").doesNotExist()).andExpect(jsonPath("$.validator").doesNotExist()).andExpect(jsonPath("$.explanation").doesNotExist());
        for(String id:List.of("arrays-11","arrays-21","arrays-31","arrays-41")) {
            mvc.perform(get("/api/questions/"+id).cookie(u.cookie())).andExpect(status().isForbidden());
            mvc.perform(post("/api/questions/"+id+"/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"8\"}")).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/questions/arrays-01/submit").cookie(u.cookie()).contentType("application/json").content("{\"answer\":\"b\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/arrays-01/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"\"}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/topics/arrays/trails/2").cookie(u.cookie())).andExpect(status().isForbidden());
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_question_progress WHERE user_id=?",Integer.class,u.id())).isZero();
    }
    @Test void allFiftyAnswersProgressAndPersistWithoutReplayRewards() throws Exception {
        var u=user();grantFixture(u);var p=packs.validate(source());int total=0;
        for(var q:p.questions()) {
            var wrong=learning.submit(u.id(),q.id(),"not-an-answer");assertThat(wrong.correct()).isFalse();assertThat(wrong.awardedXp()).isZero();assertThat(wrong.explanation()).isEmpty();
            total+=q.xpReward();
            mvc.perform(post("/api/questions/"+q.id()+"/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer",q.expectedAnswer(),"xpReward",999999,"userId",-1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.awardedXp").value(q.xpReward())).andExpect(jsonPath("$.progress.totalXp").value(total))
                .andExpect(jsonPath("$.trailCompletedNow").value(q.order()%10==0)).andExpect(jsonPath("$.topicCompletedNow").value(q.order()==50));
            var replay=learning.submit(u.id(),q.id(),q.expectedAnswer());assertThat(replay.awardedXp()).isZero();assertThat(replay.topicCompletedNow()).isFalse();
        }
        var state=learning.progress(u.id(),"arrays");assertThat(state.completed()).isEqualTo(50);assertThat(state.totalXp()).isEqualTo(900);assertThat(state.badge()).isEqualTo("Array Isles Pathfinder");assertThat(state.successorEligible()).isTrue();
        mvc.perform(post("/api/auth/logout").cookie(u.cookie()).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("email",u.email(),"password","arrays-test-password")))).andExpect(status().isOk()).andReturn();
        mvc.perform(get("/api/topics/arrays/progress").cookie(login.getResponse().getCookie(JwtCookieFilter.COOKIE))).andExpect(jsonPath("$.completed").value(50)).andExpect(jsonPath("$.totalXp").value(900));
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=?",Integer.class,u.id())).isEqualTo(1);
    }
    @Test void concurrentFinalSubmissionsAwardOnce() throws Exception {
        var u=user();grantFixture(u);var p=packs.validate(source());
        for(var q:p.questions().subList(0,49))learning.submit(u.id(),q.id(),q.expectedAnswer());
        var q=p.questions().getLast();var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(3)){
            List<Future<LearningService.Result>> futures=new ArrayList<>();for(int i=0;i<3;i++)futures.add(pool.submit(()->{start.await();return learning.submit(u.id(),q.id(),q.expectedAnswer());}));
            start.countDown();int xp=0,events=0;for(var f:futures){var r=f.get(10,TimeUnit.SECONDS);xp+=r.awardedXp();events+=r.topicCompletedNow()?1:0;}
            assertThat(xp).isEqualTo(30);assertThat(events).isEqualTo(1);
        }
        assertThat(learning.progress(u.id(),"arrays").totalXp()).isEqualTo(900);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=?",Integer.class,u.id())).isEqualTo(1);
    }
    @Test void importsAreIdempotentImmutableAndPinned() throws Exception {
        String text=source(); assertThat(packs.publish(text)).isEqualTo("arrays-v1");assertThat(packs.publish(text)).isEqualTo("arrays-v1");
        var u=user();grantFixture(u);assertThat(learning.progress(u.id(),"arrays").pack()).isEqualTo("arrays-v1");
        assertThatThrownBy(()->packs.publish(text.replace("Array length","Changed title"))).isInstanceOf(IllegalArgumentException.class);
        var revised=PackService.JSON.readTree(text);((tools.jackson.databind.node.ObjectNode)revised).put("version",2);
        try { packs.publish(PackService.JSON.writeValueAsString(revised));assertThat(learning.progress(u.id(),"arrays").pack()).isEqualTo("arrays-v1"); }
        finally { db.update("UPDATE topics SET active_pack='arrays-v1' WHERE slug='arrays'"); }
        var invalid=PackService.JSON.readTree(text);((tools.jackson.databind.node.ArrayNode)invalid.get("questions")).remove(0);
        assertThatThrownBy(()->packs.validate(PackService.JSON.writeValueAsString(invalid))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->packs.validate(text.replaceFirst("\"difficulty\": \"Easy\"","\"difficulty\": \"Hard\""))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->packs.validate(text.replaceFirst("\"validator\": \"OPTION\"","\"validator\": \"EXECUTE_JAVA\""))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void structuredValidationRejectsMalformedAndWrongShapes() {
        assertThat(AnswerValidator.matches("INTEGER","8"," 08 ")).isTrue();
        assertThat(AnswerValidator.matches("INTEGER","8","8.0")).isFalse();
        assertThat(AnswerValidator.matches("INT_LIST","[1,2]","[1, 2]")).isTrue();
        for(String input:List.of("[2,1]","[\"1\",2]","[1.0,2]","null","{\"0\":1}","System.exit(0)"))assertThat(AnswerValidator.matches("INT_LIST","[1,2]",input)).isFalse();
        assertThat(AnswerValidator.matches("INT_MATRIX","[[1],[2]]","[1,2]")).isFalse();
    }
    @Test void classicAnswersMatchIndependentReferenceCalculations() throws Exception {
        var qs=packs.validate(source()).questions();
        int[] values={3,-1,1,2,-2,3};int count=0;for(int i=0;i<values.length;i++){int sum=0;for(int j=i;j<values.length;j++){sum+=values[j];if(sum==3)count++;}}
        assertThat(qs.get(39).expectedAnswer()).isEqualTo(""+count);
        int[] h={1,8,6,2,5,4,8,3,7};int area=0;for(int i=0;i<h.length;i++)for(int j=i+1;j<h.length;j++)area=Math.max(area,(j-i)*Math.min(h[i],h[j]));
        assertThat(qs.get(28).expectedAnswer()).isEqualTo(""+area);
        int[] a={-2,1,-3,4,-1,2,1,-5,4};int best=Integer.MIN_VALUE;for(int i=0;i<a.length;i++){int sum=0;for(int j=i;j<a.length;j++){sum+=a[j];best=Math.max(best,sum);}}
        assertThat(qs.get(38).expectedAnswer()).isEqualTo(""+best);
        int[] heights={3,0,2,0,4};int water=0;for(int i=0;i<heights.length;i++){int left=0,right=0;for(int j=0;j<=i;j++)left=Math.max(left,heights[j]);for(int j=i;j<heights.length;j++)right=Math.max(right,heights[j]);water+=Math.min(left,right)-heights[i];}
        assertThat(qs.get(49).expectedAnswer()).isEqualTo(""+water);
    }
}
