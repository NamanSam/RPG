package com.codequest.learning;

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
class RecursionRuinsApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;
    @Autowired PackService packs;

    private String source() throws Exception {
        try (var input = new ClassPathResource("content/dsa/recursion/v1.json").getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Cookie register(String email) throws Exception {
        var response = mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType("application/json")
                        .content(PackService.JSON.writeValueAsString(Map.of(
                                "email", email, "password", "recursion-test-password", "displayName", "Recursion Tester"))))
                .andExpect(status().isCreated())
                .andReturn();
        return response.getResponse().getCookie(JwtCookieFilter.COOKIE);
    }

    private void completeHeapPrerequisite(long user) {
        int updated = db.update("UPDATE user_campaign_progress SET completed_at=CURRENT_TIMESTAMP WHERE user_id=? AND campaign_slug='java'", user);
        if (updated == 0) {
            db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at,completed_at) VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", user);
        }
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'heaps','heaps-v1',5,CURRENT_TIMESTAMP)", user);
    }

    @Test
    void authenticatedApiGatesPrerequisiteCompletesAllQuestionsAndRestoresAfterLogin() throws Exception {
        mvc.perform(get("/api/topics/recursion/progress")).andExpect(status().isUnauthorized());

        String email = "recursion-api-" + UUID.randomUUID() + "@codequest.test";
        Cookie cookie = register(email);
        long user = players.findByEmail(email).orElseThrow().getId();
        mvc.perform(get("/api/topics/recursion/progress").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(get("/api/questions/recursion-01").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/questions/recursion-01/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content("{\"answer\":\"A\"}"))
                .andExpect(status().isForbidden());

        completeHeapPrerequisite(user);
        mvc.perform(get("/api/topics/recursion/progress").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("recursion"))
                .andExpect(jsonPath("$.trails.length()").value(5))
                .andExpect(jsonPath("$.trails[0].unlocked").value(true))
                .andExpect(jsonPath("$.trails[0].questions.length()").value(10))
                .andExpect(jsonPath("$.trails[0].questions[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.trails[0].questions[1].status").value("LOCKED"))
                .andExpect(jsonPath("$.trails[1].unlocked").value(false));
        mvc.perform(get("/api/questions/recursion-02").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/recursion/trails/2").cookie(cookie)).andExpect(status().isForbidden());

        QuestionPack pack = packs.validate(source());
        int earned = 0;
        for (var question : pack.questions()) {
            if (question.order() < 50) {
                String nextId = pack.questions().get(question.order()).id();
                mvc.perform(get("/api/questions/" + nextId).cookie(cookie)).andExpect(status().isForbidden());
            }
            mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                            .contentType("application/json")
                            .content(PackService.JSON.writeValueAsString(Map.of("answer", "__not_the_expected_answer__"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.correct").value(false))
                    .andExpect(jsonPath("$.awardedXp").value(0));

            earned += question.xpReward();
            mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                            .contentType("application/json")
                            .content(PackService.JSON.writeValueAsString(Map.of("answer", question.expectedAnswer()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.correct").value(true))
                    .andExpect(jsonPath("$.awardedXp").value(question.xpReward()))
                    .andExpect(jsonPath("$.progress.totalXp").value(earned))
                    .andExpect(jsonPath("$.trailCompletedNow").value(question.order() % 10 == 0))
                    .andExpect(jsonPath("$.topicCompletedNow").value(question.order() == 50));

            mvc.perform(post("/api/questions/" + question.id() + "/submit").cookie(cookie).with(csrf())
                            .contentType("application/json")
                            .content(PackService.JSON.writeValueAsString(Map.of("answer", question.expectedAnswer()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.awardedXp").value(0));
        }

        assertThat(earned).isEqualTo(900);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='recursion-ruins-archivist'", Integer.class, user)).isEqualTo(1);
        mvc.perform(post("/api/auth/logout").cookie(cookie).with(csrf())).andExpect(status().isNoContent());
        var login = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content(PackService.JSON.writeValueAsString(Map.of("email", email, "password", "recursion-test-password"))))
                .andExpect(status().isOk()).andReturn();
        Cookie restored = login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/topics/recursion/progress").cookie(restored))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(50))
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.totalXp").value(900))
                .andExpect(jsonPath("$.badge").value("Recursion Ruins Archivist"));
    }
}
