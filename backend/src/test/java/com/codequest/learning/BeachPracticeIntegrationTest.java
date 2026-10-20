package com.codequest.learning;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.content.*;
import com.codequest.quest.*;
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
class BeachPracticeIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired LearningService learning;
    @Autowired PackService packs;
    @Autowired QuestService story;
    @Autowired QuestRepository quests;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;
    record Account(long id,String email,Cookie cookie) {}
    String source() throws Exception {try(var s=new ClassPathResource("content/java/beginner-beach/v1.json").getInputStream()){return new String(s.readAllBytes(),StandardCharsets.UTF_8);}}
    Account user() throws Exception {
        String email="practice-"+UUID.randomUUID()+"@codequest.test";
        var r=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","practice-test-password","displayName","Beach Learner"))))
            .andExpect(status().isCreated()).andReturn();
        return new Account(players.findByEmail(email).orElseThrow().getId(),email,r.getResponse().getCookie(JwtCookieFilter.COOKIE));
    }
    void beach(long user){for(var q:quests.allQuests().stream().filter(q->q.worldId()==1).toList())story.submit(user,q.id(),q.expectedAnswer());}
    void locked(Account u,String id) throws Exception {
        mvc.perform(get("/api/questions/"+id).cookie(u.cookie())).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/"+id+"/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"11\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void contentHasExactBalancedConceptsAndReusableTypes() throws Exception {
        var p=packs.validate(source());assertThat(p.questions()).hasSize(50);assertThat(p.trails()).hasSize(5);
        assertThat(p.questions()).extracting(QuestionPack.Question::title).doesNotHaveDuplicates();
        assertThat(p.questions()).extracting(QuestionPack.Question::description).doesNotHaveDuplicates();
        assertThat(p.questions().stream().filter(q->q.difficulty().equals("Easy")).count()).isEqualTo(20);
        assertThat(p.questions().stream().filter(q->q.difficulty().equals("Medium")).count()).isEqualTo(20);
        assertThat(p.questions().stream().filter(q->q.difficulty().equals("Hard")).count()).isEqualTo(10);
        assertThat(p.questions().stream().mapToInt(QuestionPack.Question::xpReward).sum()).isEqualTo(900);
        var concepts=List.of("variables","data-types","operators","input-output","mixed-beginner");
        for(int i=0;i<5;i++) {
            final int t=i+1;var qs=p.questions().stream().filter(q->q.trail()==t).toList();
            assertThat(qs).hasSize(10).allMatch(q->q.subtopic().equals(concepts.get(t-1)));
            assertThat(qs.stream().filter(q->q.difficulty().equals("Easy")).count()).isEqualTo(4);
            assertThat(qs.stream().filter(q->q.difficulty().equals("Medium")).count()).isEqualTo(4);
            assertThat(qs.stream().filter(q->q.difficulty().equals("Hard")).count()).isEqualTo(2);
        }
        assertThat(p.questions()).extracting(QuestionPack.Question::questionType).contains("MULTIPLE_CHOICE","PREDICT_OUTPUT","CODE_BLANK","LOGIC_ANSWER");
        assertThat(p.questions()).anyMatch(QuestionPack.Question::isClassic);
        assertThat(packs.publish(source())).isEqualTo("beach-practice-v1");
        assertThat(packs.publish(source())).isEqualTo("beach-practice-v1");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM questions WHERE topic_slug='beach-practice'",Integer.class)).isEqualTo(50);
        assertThatThrownBy(()->packs.publish(source().replace("Name a supply counter","Changed after publication"))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void storyGatesAndSequentialQuestionLocksAreEnforcedAtApi() throws Exception {
        var u=user();var initial=learning.progress(u.id(),"beach-practice");
        assertThat(initial.trails()).noneMatch(LearningService.Trail::unlocked);
        for(String id:List.of("beach-practice-01","beach-practice-11","beach-practice-21","beach-practice-31","beach-practice-41"))locked(u,id);
        story.submit(u.id(),"first-variable","5");
        var opened=learning.progress(u.id(),"beach-practice");
        assertThat(opened.trails().getFirst().questions()).extracting(LearningService.Marker::status)
            .containsExactly("AVAILABLE","LOCKED","LOCKED","LOCKED","LOCKED","LOCKED","LOCKED","LOCKED","LOCKED","LOCKED");
        locked(u,"beach-practice-02");locked(u,"beach-practice-11");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_question_progress WHERE user_id=?",Integer.class,u.id())).isZero();
        mvc.perform(get("/api/questions/beach-practice-01").cookie(u.cookie())).andExpect(status().isOk())
            .andExpect(jsonPath("$.regionName").value("Beginner Beach")).andExpect(jsonPath("$.practice").value(true))
            .andExpect(jsonPath("$.expectedAnswer").doesNotExist()).andExpect(jsonPath("$.validator").doesNotExist()).andExpect(jsonPath("$.explanation").doesNotExist());
        var wrong=learning.submit(u.id(),"beach-practice-01","a");assertThat(wrong.correct()).isFalse();assertThat(wrong.awardedXp()).isZero();
        var first=learning.submit(u.id(),"beach-practice-01","b");assertThat(first.awardedXp()).isEqualTo(10);assertThat(first.progress().practiceXp()).isEqualTo(10);
        assertThat(first.progress().totalXp()).isEqualTo(50);assertThat(first.progress().trails().getFirst().questions().get(1).status()).isEqualTo("AVAILABLE");
        assertThat(learning.submit(u.id(),"beach-practice-01","b").awardedXp()).isZero();
        var other=user();assertThat(learning.progress(other.id(),"beach-practice").practiceXp()).isZero();
    }
    @Test void trainingLocationsAreIndependentButFinalTrialRequiresAllFour() throws Exception {
        var u=user();beach(u.id());var p=learning.progress(u.id(),"beach-practice");
        assertThat(p.trails()).extracting(LearningService.Trail::unlocked).containsExactly(true,true,true,true,false);
        locked(u,"beach-practice-41");
        learning.submit(u.id(),"beach-practice-31","println");
        assertThat(learning.progress(u.id(),"beach-practice").trails().get(3).questions().get(1).status()).isEqualTo("AVAILABLE");
        locked(u,"beach-practice-33");
        assertThat(learning.progress(u.id(),"beach-practice").trails().getFirst().completed()).isZero();
    }
    @Test void masteryAwardsOnceConcurrentlyAndPersistsWithoutStoryXp() throws Exception {
        var u=user();beach(u.id());var pack=packs.validate(source());int total=0;
        for(var q:pack.questions().subList(0,49)) {
            if(q.order()%10!=0)locked(u,pack.questions().get(q.order()).id());
            var wrong=learning.submit(u.id(),q.id(),"not-an-answer");assertThat(wrong.awardedXp()).isZero();
            var right=learning.submit(u.id(),q.id(),q.expectedAnswer()); total+=q.xpReward();
            assertThat(right.awardedXp()).isEqualTo(q.xpReward());assertThat(right.progress().practiceXp()).isEqualTo(total);
            assertThat(right.progress().totalXp()).isEqualTo(175);
            assertThat(right.trailCompletedNow()).isEqualTo(q.order()%10==0);
            assertThat(right.topicCompletedNow()).isFalse();
            assertThat(learning.submit(u.id(),q.id(),q.expectedAnswer()).awardedXp()).isZero();
        }
        var finalQuestion=pack.questions().getLast();var ready=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(3)) {
            var futures=new ArrayList<Future<LearningService.Result>>();
            for(int i=0;i<3;i++)futures.add(pool.submit(()->{ready.await();return learning.submit(u.id(),finalQuestion.id(),finalQuestion.expectedAnswer());}));
            ready.countDown();int awarded=0,events=0;
            for(var f:futures){var r=f.get(15,TimeUnit.SECONDS);awarded+=r.awardedXp();if(r.topicCompletedNow())events++;}
            assertThat(awarded).isEqualTo(30);assertThat(events).isEqualTo(1);
        }
        var done=learning.progress(u.id(),"beach-practice");assertThat(done.completed()).isEqualTo(50);assertThat(done.complete()).isTrue();
        assertThat(done.practiceXp()).isEqualTo(900);assertThat(done.topicXp()).isEqualTo(900);assertThat(done.totalXp()).isEqualTo(175);
        assertThat(done.badge()).isEqualTo("Beginner Beach Mastery");assertThat(done.successorEligible()).isFalse();
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='beginner-beach-mastery'",Integer.class,u.id())).isEqualTo(1);
        assertThat(story.progress(u.id()).javaCampaignComplete()).isFalse();assertThat(story.progress(u.id()).javaXp()).isEqualTo(175);
        mvc.perform(post("/api/auth/logout").cookie(u.cookie()).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",u.email(),"password","practice-test-password")))).andExpect(status().isOk()).andReturn();
        var cookie=login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/topics/beach-practice/progress").cookie(cookie)).andExpect(jsonPath("$.completed").value(50)).andExpect(jsonPath("$.practiceXp").value(900))
            .andExpect(jsonPath("$.totalXp").value(175)).andExpect(jsonPath("$.badge").value("Beginner Beach Mastery"));
        mvc.perform(get("/api/me/progress").cookie(cookie)).andExpect(jsonPath("$.totalXp").value(175)).andExpect(jsonPath("$.beachCompleted").value(3));
    }
    @Test void allJavaStoryRegionsAndDsaRemainOpenWithIncompletePractice() throws Exception {
        var u=user();story.submit(u.id(),"first-variable","5");learning.submit(u.id(),"beach-practice-01","b");
        for(var q:quests.allQuests())story.submit(u.id(),q.id(),q.expectedAnswer());
        var completed=story.progress(u.id());assertThat(completed.totalXp()).isEqualTo(2025);assertThat(completed.javaXp()).isEqualTo(2025);assertThat(completed.javaCampaignComplete()).isTrue();
        var practice=learning.progress(u.id(),"beach-practice");assertThat(practice.completed()).isEqualTo(1);assertThat(practice.practiceXp()).isEqualTo(10);assertThat(practice.badge()).isEmpty();
        assertThat(learning.progress(u.id(),"arrays").trails().getFirst().questions()).allMatch(q->q.status().equals("AVAILABLE"));
        assertThat(completed.badges()).hasSize(5);
    }
    @Test void trickyContentAnswersMatchIndependentJavaReferences() throws Exception {
        var qs=packs.validate(source()).questions();
        assertThat(qs.get(18).expectedAnswer()).isEqualTo(String.valueOf((byte)128));
        assertThat(qs.get(19).expectedAnswer()).isEqualTo(String.valueOf((long)(1000000*3000)));
        int a=2,b=a++ + ++a;assertThat(qs.get(26).expectedAnswer()).isEqualTo(a+" "+b);
        assertThat(qs.get(28).expectedAnswer()).isEqualTo("["+Integer.parseInt("47".substring(0,1))+","+Integer.parseInt("47".substring(1))+"]");
        assertThat(qs.get(48).expectedAnswer()).isEqualTo(new StringBuilder("406").reverse().toString());
        assertThat(qs.get(49).expectedAnswer()).isEqualTo(String.valueOf((int)Math.ceil(23.0/6)));
    }
}

