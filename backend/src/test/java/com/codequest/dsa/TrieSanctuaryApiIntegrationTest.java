package com.codequest.dsa;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.content.PackService;
import com.codequest.content.QuestionPack;
import com.codequest.user.PlayerRepository;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrieSanctuaryApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;

    private Cookie register(String email) throws Exception {
        var response = mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType("application/json")
                        .content(PackService.JSON.writeValueAsString(Map.of(
                                "email", email, "password", "trie-test-password", "displayName", "Trie Tester"))))
                .andExpect(status().isCreated())
                .andReturn();
        return response.getResponse().getCookie(JwtCookieFilter.COOKIE);
    }

    private void completeBitwisePrerequisite(long user) {
        int javaUpdated = db.update("UPDATE user_campaign_progress SET completed_at=CURRENT_TIMESTAMP WHERE user_id=? AND campaign_slug='java'", user);
        if (javaUpdated == 0) {
            db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at,completed_at) VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", user);
        }
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'heaps','heaps-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'recursion','recursion-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'backtracking','backtracking-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'bitwise','bitwise-v1',5,CURRENT_TIMESTAMP)", user);
    }

    @Test
    void authenticatedApiGatesBitwisePrerequisiteAndPersistsRewardsAfterLogin() throws Exception {
        mvc.perform(get("/api/topics/trie/progress")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/questions/trie-01")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/questions/trie-01/submit").with(csrf())
                        .contentType("application/json").content("{\"answer\":\"a\"}"))
                .andExpect(status().isUnauthorized());

        String email = "trie-api-" + UUID.randomUUID() + "@codequest.test";
        Cookie cookie = register(email);
        long user = players.findByEmail(email).orElseThrow().getId();
        mvc.perform(get("/api/topics/trie/progress").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(get("/api/questions/trie-01").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/trie-01/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content("{\"answer\":\"a\"}"))
                .andExpect(status().isForbidden());

        completeBitwisePrerequisite(user);
        mvc.perform(get("/api/topics/trie/progress").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("trie"))
                .andExpect(jsonPath("$.regionName").value("Trie Sanctuary"))
                .andExpect(jsonPath("$.trails.length()").value(5))
                .andExpect(jsonPath("$.trails[0].unlocked").value(true))
                .andExpect(jsonPath("$.trails[0].questions.length()").value(10))
                .andExpect(jsonPath("$.trails[0].questions[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.trails[0].questions[1].status").value("LOCKED"))
                .andExpect(jsonPath("$.trails[1].unlocked").value(false))
                .andExpect(jsonPath("$.trails[4].questions.length()").value(10));
        mvc.perform(get("/api/questions/trie-02").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/trie-02/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content("{\"answer\":\"d\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/trie/trails/2").cookie(cookie)).andExpect(status().isForbidden());

        var resource = new ClassPathResource("content/dsa/tries/v1.json");
        QuestionPack pack;
        try (var input = resource.getInputStream()) {
            pack = new PackService(null).validate(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
        var question = pack.questions().getFirst();
        mvc.perform(get("/api/questions/" + question.id()).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("trie"))
                .andExpect(jsonPath("$.trail").value(1))
                .andExpect(jsonPath("$.difficulty").value(question.difficulty()))
                .andExpect(jsonPath("$.xpReward").value(question.xpReward()));
        mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", "__wrong_answer__"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(false)).andExpect(jsonPath("$.awardedXp").value(0));
        mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", question.expectedAnswer()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.awardedXp").value(question.xpReward())).andExpect(jsonPath("$.progress.completed").value(1));
        mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", question.expectedAnswer()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.awardedXp").value(0));

        mvc.perform(get("/api/topics/trie/progress").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.totalXp").value(question.xpReward()));

        mvc.perform(post("/api/auth/logout").cookie(cookie).with(csrf())).andExpect(status().isNoContent());
        var login = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content(PackService.JSON.writeValueAsString(Map.of("email", email, "password", "trie-test-password"))))
                .andExpect(status().isOk()).andReturn();
        Cookie restored = login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/topics/trie/progress").cookie(restored))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.totalXp").value(question.xpReward()));
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_progress WHERE user_id=? AND topic_slug='trie'", Integer.class, user)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT total_xp FROM player_stats WHERE user_id=?", Integer.class, user)).isEqualTo(question.xpReward());
    }
}

