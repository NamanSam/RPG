package com.codequest.quest;

import com.codequest.auth.*;
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
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class JavaCampaignIntegrationTest {
    @Autowired MockMvc mvc; @Autowired QuestService service; @Autowired QuestRepository repo;
    @Autowired JdbcTemplate db; @Autowired PlayerRepository players; @Autowired LearningService learning;
    record Account(long id,String email,Cookie cookie) {}
    Account user() throws Exception {
        String email="java-"+UUID.randomUUID()+"@codequest.test";
        var r=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","java-test-password","displayName","Java Journey")))).andExpect(status().isCreated()).andExpect(jsonPath("$.totalXp").value(0)).andReturn();
        return new Account(players.findByEmail(email).orElseThrow().getId(),email,r.getResponse().getCookie(JwtCookieFilter.COOKIE));
    }
    @Test void normalJourneyLocksRewardsBadgesAndDsaPersist() throws Exception {
        var u=user();var all=repo.allQuests();assertThat(all).hasSize(16);int xp=0;
        for(var q:all) {
            for(var locked:all.stream().filter(p->p.worldId()>q.worldId()||(p.worldId()==q.worldId()&&p.order()>q.order())).toList()) {
                mvc.perform(get("/api/quests/"+locked.id()).cookie(u.cookie())).andExpect(status().isForbidden());
                mvc.perform(post("/api/quests/"+locked.id()+"/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer",locked.expectedAnswer())))).andExpect(status().isForbidden());
            }
            mvc.perform(get("/api/questions/arrays-01").cookie(u.cookie())).andExpect(status().isForbidden());
            mvc.perform(get("/api/quests/"+q.id()).cookie(u.cookie())).andExpect(status().isOk()).andExpect(jsonPath("$.expectedAnswer").doesNotExist());
            var bad=service.submit(u.id(),q.id(),"wrong");assertThat(bad.awardedXp()).isZero();assertThat(bad.progress().totalXp()).isEqualTo(xp);
            xp+=q.reward();
            mvc.perform(post("/api/quests/"+q.id()+"/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer",q.expectedAnswer())))).andExpect(status().isOk()).andExpect(jsonPath("$.awardedXp").value(q.reward())).andExpect(jsonPath("$.progress.totalXp").value(xp));
            assertThat(service.submit(u.id(),q.id(),q.expectedAnswer()).awardedXp()).isZero();
            if(q.id().equals("hidden-maximum"))assertThat(xp).isEqualTo(850);
            if(q.id().equals("many-forms"))assertThat(xp).isEqualTo(1500);
        }
        var p=service.progress(u.id());assertThat(p.totalXp()).isEqualTo(2025);assertThat(p.javaCompleted()).isTrue();assertThat(p.badges()).hasSize(5);
        mvc.perform(get("/api/campaigns/dsa/map").cookie(u.cookie())).andExpect(jsonPath("$.unlocked").value(true));
        mvc.perform(get("/api/topics/arrays/progress").cookie(u.cookie())).andExpect(jsonPath("$.unlockedTrail").value(1)).andExpect(jsonPath("$.completed").value(0));
        mvc.perform(get("/api/questions/arrays-01").cookie(u.cookie())).andExpect(status().isOk());
        var first=learning.submit(u.id(),"arrays-01","b");assertThat(first.progress().totalXp()).isEqualTo(2035);
        mvc.perform(post("/api/auth/logout").cookie(u.cookie()).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("email",u.email(),"password","java-test-password")))).andExpect(status().isOk()).andReturn();
        var cookie=login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/me/progress").cookie(cookie)).andExpect(jsonPath("$.javaCompleted").value(true)).andExpect(jsonPath("$.badges.length()").value(5)).andExpect(jsonPath("$.totalXp").value(2035));
        mvc.perform(get("/api/topics/arrays/progress").cookie(cookie)).andExpect(jsonPath("$.completed").value(1));
    }
    @Test void concurrentRegionFinalsAwardOnceAndForgedUnlockDoesNotGrantAccess() throws Exception {
        var u=user();db.update("INSERT INTO user_world_unlocks VALUES (?,5,CURRENT_TIMESTAMP)",u.id());
        mvc.perform(post("/api/quests/hall-of-uniques/submit").cookie(u.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"3\"}")).andExpect(status().isForbidden());
        db.update("DELETE FROM user_world_unlocks WHERE user_id=? AND world_id=5",u.id());
        for(var q:repo.allQuests()) {
            boolean finalQuest=q.worldId()>2 && q.order()==(q.worldId()==4?4:3);
            if(!finalQuest){service.submit(u.id(),q.id(),q.expectedAnswer());continue;}
            var start=new CountDownLatch(1);
            try(var pool=Executors.newFixedThreadPool(3)) {
                List<Future<QuestService.Submission>> futures=new ArrayList<>();for(int i=0;i<3;i++)futures.add(pool.submit(()->{start.await();return service.submit(u.id(),q.id(),q.expectedAnswer());}));
                start.countDown();int award=0,events=0;for(var f:futures){var r=f.get(10,TimeUnit.SECONDS);award+=r.awardedXp();events+=r.completedWorld()!=null?1:0;}
                assertThat(award).isEqualTo(q.reward());assertThat(events).isEqualTo(1);
            }
        }
        assertThat(service.progress(u.id()).totalXp()).isEqualTo(2025);assertThat(service.progress(u.id()).badges()).hasSize(5);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=?",Integer.class,u.id())).isEqualTo(2);
    }
}
