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
class BeachProgressIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired QuestService quests;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;
    record Account(String email, Cookie cookie, long id) {}

    Account register() throws Exception {
        String email = "beach-" + UUID.randomUUID() + "@codequest.test";
        var response = mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"" + email + "\",\"password\":\"beach-test-password\",\"displayName\":\"Beach Explorer\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.totalXp").value(0)).andReturn();
        return new Account(email, response.getResponse().getCookie(JwtCookieFilter.COOKIE), players.findByEmail(email).orElseThrow().getId());
    }
    ResultActions submit(Account user, String id, String answer) throws Exception {
        return mvc.perform(post("/api/quests/" + id + "/submit").cookie(user.cookie()).with(csrf())
            .contentType("application/json").content("{\"answer\":\"" + answer + "\"}"));
    }
    ResultActions progress(Account user) throws Exception { return mvc.perform(get("/api/me/progress").cookie(user.cookie())).andExpect(status().isOk()); }

    @Test void completeBeachFlowPersistsAcrossLoginAndNeverDuplicatesRewards() throws Exception {
        var user = register();
        progress(user).andExpect(jsonPath("$.totalXp").value(0))
            .andExpect(jsonPath("$.quests[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$.quests[1].status").value("LOCKED"))
            .andExpect(jsonPath("$.quests[2].status").value("LOCKED"))
            .andExpect(jsonPath("$.worlds[0].unlocked").value(true))
            .andExpect(jsonPath("$.worlds[1].unlocked").value(false));
        submit(user,"choose-the-type","boolean").andExpect(status().isForbidden());
        submit(user,"operator-training","2").andExpect(status().isForbidden());
        submit(user,"first-variable","4").andExpect(status().isOk())
            .andExpect(jsonPath("$.correct").value(false)).andExpect(jsonPath("$.awardedXp").value(0))
            .andExpect(jsonPath("$.progress.quests[1].status").value("LOCKED"));
        submit(user,"first-variable"," 5 ").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(50)).andExpect(jsonPath("$.firstCompletion").value(true))
            .andExpect(jsonPath("$.progress.totalXp").value(50)).andExpect(jsonPath("$.progress.quests[1].status").value("AVAILABLE"));
        submit(user,"first-variable","5").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.firstCompletion").value(false));
        submit(user,"first-variable","4").andExpect(status().isOk()).andExpect(jsonPath("$.progress.quests[0].status").value("COMPLETED"));
        submit(user,"choose-the-type","int").andExpect(status().isOk()).andExpect(jsonPath("$.progress.totalXp").value(50));
        submit(user,"choose-the-type","boolean").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(50)).andExpect(jsonPath("$.progress.totalXp").value(100))
            .andExpect(jsonPath("$.progress.quests[2].status").value("AVAILABLE"));
        submit(user,"operator-training","2").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(75)).andExpect(jsonPath("$.beachCompletedNow").value(true))
            .andExpect(jsonPath("$.progress.totalXp").value(175)).andExpect(jsonPath("$.progress.beachCompleted").value(3))
            .andExpect(jsonPath("$.progress.badges[0].id").value("beginner-beach"))
            .andExpect(jsonPath("$.progress.worlds[1].unlocked").value(true))
            .andExpect(jsonPath("$.progress.worlds[2].unlocked").value(false));
        submit(user,"operator-training","2").andExpect(status().isOk())
            .andExpect(jsonPath("$.awardedXp").value(0)).andExpect(jsonPath("$.beachCompletedNow").value(false));
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_badges WHERE user_id = ?",Integer.class,user.id())).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_world_unlocks WHERE user_id = ?",Integer.class,user.id())).isEqualTo(2);
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(user.cookie())).andExpect(status().isNoContent());
        var login = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content("{\"email\":\""+user.email()+"\",\"password\":\"beach-test-password\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalXp").value(175)).andReturn();
        progress(new Account(user.email(),login.getResponse().getCookie(JwtCookieFilter.COOKIE),user.id()))
            .andExpect(jsonPath("$.beachCompleted").value(3)).andExpect(jsonPath("$.worlds[1].unlocked").value(true))
            .andExpect(jsonPath("$.badges.length()").value(1));
        progress(register()).andExpect(jsonPath("$.totalXp").value(0)).andExpect(jsonPath("$.badges.length()").value(0));
    }

    @Test void endpointsRequireAuthenticationAndDoNotExposeGradingSecrets() throws Exception {
        mvc.perform(get("/api/me/progress")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/quests/first-variable")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/quests/first-variable/submit").with(csrf()).contentType("application/json").content("{\"answer\":\"5\"}"))
            .andExpect(status().isUnauthorized());
        var user=register();
        mvc.perform(get("/api/quests/choose-the-type").cookie(user.cookie())).andExpect(status().isForbidden());
        mvc.perform(get("/api/quests/first-variable").cookie(user.cookie())).andExpect(status().isOk())
            .andExpect(jsonPath("$.expectedAnswer").doesNotExist()).andExpect(jsonPath("$.quest.expectedAnswer").doesNotExist())
            .andExpect(jsonPath("$.successFeedback").doesNotExist());
        mvc.perform(post("/api/quests/first-variable/submit").cookie(user.cookie()).contentType("application/json").content("{\"answer\":\"5\"}"))
            .andExpect(status().isForbidden());
        submit(user,"not-a-quest","5").andExpect(status().isNotFound());
        submit(user,"first-variable","").andExpect(status().isBadRequest());
        submit(user,"first-variable","x".repeat(81)).andExpect(status().isBadRequest());
        progress(user).andExpect(jsonPath("$.totalXp").value(0));
    }

    @Test void concurrentFirstCompletionsAwardOnceIncludingFinalBadgeAndUnlock() throws Exception {
        var user=register();
        assertConcurrentReward(user.id(),"first-variable","5",50);
        quests.submit(user.id(),"choose-the-type","boolean");
        assertConcurrentReward(user.id(),"operator-training","2",75);
        var state=quests.progress(user.id());
        assertThat(state.totalXp()).isEqualTo(175);
        assertThat(state.badges()).hasSize(1);
        assertThat(state.beachCompleted()).isEqualTo(3);
    }
    void assertConcurrentReward(long userId, String id, String answer, int expectedReward) throws Exception {
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<QuestService.Submission> action=()->{start.await();return quests.submit(userId,id,answer);};
            var first=pool.submit(action);var second=pool.submit(action);start.countDown();
            var a=first.get(15,TimeUnit.SECONDS);var b=second.get(15,TimeUnit.SECONDS);
            assertThat(a.awardedXp()+b.awardedXp()).isEqualTo(expectedReward);
            assertThat(a.firstCompletion()).isNotEqualTo(b.firstCompletion());
        }
    }
}
