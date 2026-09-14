package com.codequest.quest;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.user.PlayerRepository;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class VillageProgressIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired QuestService quests;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;
    record Account(String email, Cookie cookie, long id) {}
    static final String GATE = "coins >= 10";
    static final String MILL = "for (int i = 0; i < 3; i++)";

    Account register() throws Exception {
        String email = "village-" + UUID.randomUUID() + "@codequest.test";
        var response = mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"" + email + "\",\"password\":\"village-test-password\",\"displayName\":\"Village Explorer\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.totalXp").value(0)).andReturn();
        return new Account(email, response.getResponse().getCookie(JwtCookieFilter.COOKIE), players.findByEmail(email).orElseThrow().getId());
    }
    void completeBeach(Account user) {
        quests.submit(user.id(), "first-variable", "5");
        quests.submit(user.id(), "choose-the-type", "boolean");
        quests.submit(user.id(), "operator-training", "2");
    }
    ResultActions submit(Account user, String id, String answer) throws Exception {
        return mvc.perform(post("/api/quests/" + id + "/submit").cookie(user.cookie()).with(csrf())
            .contentType("application/json").content("{\"answer\":\"" + answer + "\"}"));
    }
    ResultActions progress(Account user) throws Exception {
        return mvc.perform(get("/api/me/progress").cookie(user.cookie())).andExpect(status().isOk());
    }

    @Test void villageRequiresBeachAndCannotBypassEitherRegionOrQuestLocks() throws Exception {
        var user = register();
        progress(user).andExpect(jsonPath("$.worlds[1].unlocked").value(false))
            .andExpect(jsonPath("$.villageQuests[0].status").value("LOCKED"))
            .andExpect(jsonPath("$.villageQuests[1].status").value("LOCKED"))
            .andExpect(jsonPath("$.villageQuests[2].status").value("LOCKED"));
        String[] ids = {"the-gatekeeper", "the-looping-mill", "the-endless-well"};
        String[] answers = {GATE, MILL, "8"};
        for (int i = 0; i < ids.length; i++) {
            mvc.perform(get("/api/quests/" + ids[i]).cookie(user.cookie())).andExpect(status().isForbidden());
            submit(user, ids[i], answers[i]).andExpect(status().isForbidden());
        }
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_quest_progress WHERE user_id = ?", Integer.class, user.id())).isZero();
        quests.submit(user.id(), "first-variable", "5");
        submit(user, "the-gatekeeper", GATE).andExpect(status().isForbidden());
        quests.submit(user.id(), "choose-the-type", "boolean");
        submit(user, "the-gatekeeper", GATE).andExpect(status().isForbidden());
        quests.submit(user.id(), "operator-training", "2");
        submit(user, "the-looping-mill", MILL).andExpect(status().isForbidden());
        submit(user, "the-endless-well", "8").andExpect(status().isForbidden());
        mvc.perform(get("/api/quests/the-gatekeeper").cookie(user.cookie())).andExpect(status().isOk())
            .andExpect(jsonPath("$.worldName").value("Loop Village"))
            .andExpect(jsonPath("$.options.length()").value(4))
            .andExpect(jsonPath("$.expectedAnswer").doesNotExist())
            .andExpect(jsonPath("$.quest.expectedAnswer").doesNotExist());
        mvc.perform(get("/api/quests/the-gatekeeper")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/quests/the-gatekeeper/submit").with(csrf()).contentType("application/json").content("{\"answer\":\"coins >= 10\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/quests/the-gatekeeper/submit").cookie(user.cookie()).contentType("application/json").content("{\"answer\":\"coins >= 10\"}"))
            .andExpect(status().isForbidden());
        submit(user, "the-gatekeeper", "").andExpect(status().isBadRequest());
        submit(user, "the-gatekeeper", "x".repeat(81)).andExpect(status().isBadRequest());
        // A different account cannot borrow the unlocked player's ID or reward values.
        var other = register();
        mvc.perform(post("/api/quests/the-gatekeeper/submit").cookie(other.cookie()).with(csrf()).contentType("application/json")
            .content("{\"answer\":\"coins >= 10\",\"userId\":" + user.id() + ",\"reward\":9999}"))
            .andExpect(status().isForbidden());
        progress(other).andExpect(jsonPath("$.totalXp").value(0));
        progress(user).andExpect(jsonPath("$.totalXp").value(175));
    }

    @Test void fullProgressionRewardsBadgesUnlocksAndLoginPersistence() throws Exception {
        var user = register(); completeBeach(user);
        progress(user).andExpect(jsonPath("$.totalXp").value(175))
            .andExpect(jsonPath("$.worlds[1].unlocked").value(true))
            .andExpect(jsonPath("$.worlds[2].unlocked").value(false))
            .andExpect(jsonPath("$.villageQuests[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$.villageQuests[1].status").value("LOCKED"))
            .andExpect(jsonPath("$.villageQuests[2].status").value("LOCKED"));
        submit(user, "the-gatekeeper", "coins > 10").andExpect(status().isOk())
            .andExpect(jsonPath("$.correct").value(false)).andExpect(jsonPath("$.awardedXp").value(0))
            .andExpect(jsonPath("$.progress.totalXp").value(175));
        submit(user, "the-gatekeeper", GATE).andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(75)).andExpect(jsonPath("$.progress.totalXp").value(250))
            .andExpect(jsonPath("$.progress.villageQuests[1].status").value("AVAILABLE"));
        submit(user, "the-gatekeeper", GATE).andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.progress.totalXp").value(250));
        submit(user, "the-endless-well", "8").andExpect(status().isForbidden());
        submit(user, "the-looping-mill", "for (int i = 0; i <= 3; i++)").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.progress.totalXp").value(250));
        submit(user, "the-looping-mill", MILL).andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(100)).andExpect(jsonPath("$.progress.totalXp").value(350))
            .andExpect(jsonPath("$.progress.level").value(2))
            .andExpect(jsonPath("$.progress.villageQuests[2].status").value("AVAILABLE"));
        submit(user, "the-endless-well", "7").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.progress.worlds[2].unlocked").value(false));
        submit(user, "the-endless-well", "8").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(125)).andExpect(jsonPath("$.completedWorld").value("loop-village"))
            .andExpect(jsonPath("$.beachCompletedNow").value(false))
            .andExpect(jsonPath("$.progress.totalXp").value(475)).andExpect(jsonPath("$.progress.villageCompleted").value(3))
            .andExpect(jsonPath("$.progress.beachCompleted").value(3))
            .andExpect(jsonPath("$.progress.badges[1].id").value("loop-village"))
            .andExpect(jsonPath("$.progress.worlds[2].unlocked").value(true))
            .andExpect(jsonPath("$.progress.worlds[3].unlocked").value(false))
            .andExpect(jsonPath("$.progress.worlds[4].unlocked").value(false));
        // Replays of every quest, including the old region, preserve the aggregate.
        String[] ids={"first-variable","choose-the-type","operator-training","the-gatekeeper","the-looping-mill","the-endless-well"};
        String[] answers={"5","boolean","2",GATE,MILL,"8"};
        for(int i=0;i<ids.length;i++) submit(user,ids[i],answers[i]).andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.progress.totalXp").value(475))
            .andExpect(jsonPath("$.completedWorld").isEmpty());
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(user.cookie())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content("{\"email\":\""+user.email()+"\",\"password\":\"village-test-password\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalXp").value(475)).andReturn();
        progress(new Account(user.email(),login.getResponse().getCookie(JwtCookieFilter.COOKIE),user.id()))
            .andExpect(jsonPath("$.villageCompleted").value(3)).andExpect(jsonPath("$.beachCompleted").value(3))
            .andExpect(jsonPath("$.badges.length()").value(2)).andExpect(jsonPath("$.worlds[2].unlocked").value(true));
    }

    @Test void concurrentVillageSubmissionsNeverDuplicateRewardsBadgesOrUnlocks() throws Exception {
        var user=register(); completeBeach(user);
        concurrent(user.id(),"the-gatekeeper",GATE,75);
        concurrent(user.id(),"the-looping-mill",MILL,100);
        concurrent(user.id(),"the-endless-well","8",125);
        var state=quests.progress(user.id());
        assertThat(state.totalXp()).isEqualTo(475);
        assertThat(state.villageCompleted()).isEqualTo(3);
        assertThat(state.badges()).hasSize(2);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_badges WHERE user_id = ? AND badge_id = 'loop-village'",Integer.class,user.id())).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_world_unlocks WHERE user_id = ? AND world_id = 3",Integer.class,user.id())).isEqualTo(1);
    }
    void concurrent(long userId,String id,String answer,int reward) throws Exception {
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<QuestService.Submission> action=()->{start.await();return quests.submit(userId,id,answer);};
            var first=pool.submit(action);var second=pool.submit(action);start.countDown();
            var a=first.get(15,TimeUnit.SECONDS);var b=second.get(15,TimeUnit.SECONDS);
            assertThat(a.awardedXp()+b.awardedXp()).isEqualTo(reward);
            assertThat(a.firstCompletion()).isNotEqualTo(b.firstCompletion());
        }
    }
}
