package com.codequest.quest;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.content.PackService;
import com.codequest.learning.LearningService;
import com.codequest.user.PlayerRepository;
import jakarta.servlet.http.Cookie;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CollectionKingdomIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired QuestService quests;
    @Autowired QuestRepository definitions;
    @Autowired LearningService learning;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;

    void locked(Cookie cookie, String id) throws Exception {
        mvc.perform(get("/api/quests/"+id).cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/quests/"+id+"/submit").cookie(cookie).with(csrf()).contentType("application/json").content("{\"answer\":\"3\"}"))
            .andExpect(status().isForbidden());
    }
    void dsaLocked(Cookie cookie) throws Exception {
        mvc.perform(get("/api/campaigns/dsa/map").cookie(cookie)).andExpect(jsonPath("$.unlocked").value(false));
        mvc.perform(get("/api/questions/arrays-01").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/arrays-01/submit").cookie(cookie).with(csrf()).contentType("application/json").content("{\"answer\":\"b\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void normalJourneyCompletesJavaAndEntersArraysOnceWithPersistence() throws Exception {
        String email="kingdom-"+UUID.randomUUID()+"@codequest.test";
        var registration=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","kingdom-test-password","displayName","Royal Scholar"))))
            .andExpect(status().isCreated()).andReturn();
        Cookie cookie=registration.getResponse().getCookie(JwtCookieFilter.COOKIE);
        long user=players.findByEmail(email).orElseThrow().getId();
        locked(cookie,"the-dynamic-scroll"); dsaLocked(cookie);
        for (long world=1; world<=4; world++) {
            final long region=world;
            for(var q:definitions.allQuests().stream().filter(q->q.worldId()==region).toList()) {
                locked(cookie,"the-dynamic-scroll");
                assertThat(quests.submit(user,q.id(),q.expectedAnswer()).firstCompletion()).isTrue();
            }
            if(world<4) assertThat(quests.progress(user).kingdomQuests()).allMatch(q->q.status().equals("LOCKED"));
        }
        var start=quests.progress(user);
        assertThat(start.totalXp()).isEqualTo(1500);
        assertThat(start.javaCampaignComplete()).isFalse();
        assertThat(start.dsaUnlocked()).isFalse();
        assertThat(start.kingdomQuests()).extracting(QuestService.QuestSummary::status).containsExactly("AVAILABLE","LOCKED","LOCKED");
        locked(cookie,"the-key-keeper"); locked(cookie,"hall-of-uniques"); dsaLocked(cookie);
        var wrong=quests.submit(user,"the-dynamic-scroll","4");
        assertThat(wrong.correct()).isFalse(); assertThat(wrong.awardedXp()).isZero(); assertThat(wrong.progress().totalXp()).isEqualTo(1500);
        var scroll=quests.submit(user,"the-dynamic-scroll","3");
        assertThat(scroll.awardedXp()).isEqualTo(150);
        assertThat(scroll.progress().totalXp()).isEqualTo(1650);
        assertThat(scroll.progress().kingdomQuests().get(1).status()).isEqualTo("AVAILABLE");
        assertThat(quests.submit(user,"the-dynamic-scroll","3").awardedXp()).isZero();
        locked(cookie,"hall-of-uniques"); dsaLocked(cookie);
        var keys=quests.submit(user,"the-key-keeper","7");
        assertThat(keys.awardedXp()).isEqualTo(175); assertThat(keys.progress().totalXp()).isEqualTo(1825);
        assertThat(keys.progress().kingdomQuests().get(2).status()).isEqualTo("AVAILABLE");
        dsaLocked(cookie);
        var ready=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(3)) {
            var futures=new ArrayList<Future<QuestService.Submission>>();
            for(int i=0;i<3;i++) futures.add(pool.submit(()->{ready.await();return quests.submit(user,"hall-of-uniques","3");}));
            ready.countDown(); int xp=0,events=0;
            for(var f:futures) {var r=f.get(15,TimeUnit.SECONDS);xp+=r.awardedXp();if(r.completedWorld()!=null)events++;}
            assertThat(xp).isEqualTo(200); assertThat(events).isEqualTo(1);
        }
        var done=quests.progress(user);
        assertThat(done.totalXp()).isEqualTo(2025); assertThat(done.javaXp()).isEqualTo(2025);
        assertThat(done.kingdomCompleted()).isEqualTo(3); assertThat(done.javaCampaignComplete()).isTrue(); assertThat(done.dsaUnlocked()).isTrue();
        assertThat(done.badges()).extracting(QuestService.BadgeView::id).containsExactlyInAnyOrder("beginner-beach","loop-village","array-forest","oop-canyon","collection-kingdom");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=? AND campaign_slug='java' AND completed_at IS NOT NULL",Integer.class,user)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=? AND campaign_slug='dsa'",Integer.class,user)).isEqualTo(1);
        var completedAt=db.queryForObject("SELECT completed_at FROM user_campaign_progress WHERE user_id=? AND campaign_slug='java'",java.sql.Timestamp.class,user);
        for(var q:definitions.allQuests()) assertThat(quests.submit(user,q.id(),q.expectedAnswer()).awardedXp()).isZero();
        assertThat(db.queryForObject("SELECT completed_at FROM user_campaign_progress WHERE user_id=? AND campaign_slug='java'",java.sql.Timestamp.class,user)).isEqualTo(completedAt);
        mvc.perform(get("/api/campaigns/dsa/map").cookie(cookie)).andExpect(jsonPath("$.unlocked").value(true));
        mvc.perform(get("/api/questions/arrays-01").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.expectedAnswer").doesNotExist());
        var arrays=learning.progress(user,"arrays");
        assertThat(arrays.completed()).isZero(); assertThat(arrays.unlockedTrail()).isEqualTo(1);
        String answer=db.queryForObject("SELECT expected_answer FROM question_validations WHERE pack_id=? AND question_id='arrays-01'",String.class,arrays.pack());
        var firstArray=learning.submit(user,"arrays-01",answer);
        assertThat(firstArray.firstCompletion()).isTrue(); assertThat(firstArray.awardedXp()).isEqualTo(10);
        assertThat(learning.submit(user,"arrays-01",answer).awardedXp()).isZero();
        assertThat(quests.progress(user).javaXp()).isEqualTo(2025);
        mvc.perform(post("/api/auth/logout").cookie(cookie).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","kingdom-test-password"))))
            .andExpect(status().isOk()).andReturn();
        Cookie restored=login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/me/progress").cookie(restored)).andExpect(jsonPath("$.totalXp").value(2035)).andExpect(jsonPath("$.javaXp").value(2025))
            .andExpect(jsonPath("$.javaCampaignComplete").value(true)).andExpect(jsonPath("$.dsaUnlocked").value(true));
        mvc.perform(get("/api/topics/arrays/progress").cookie(restored)).andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(1));
    }
}
