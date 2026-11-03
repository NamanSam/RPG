package com.codequest.learning;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.auth.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BacktrackingMazeApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired PlayerRepository players;
    @Autowired JdbcTemplate db;
    @Autowired PackService packs;
    @Autowired JwtService jwt;
    @Autowired PasswordEncoder encoder;

    private String source() throws Exception {
        try (var input = new ClassPathResource("content/dsa/backtracking/v1.json").getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private long createAccount(String email, String password) {
        db.update("INSERT INTO players(email,password_hash,display_name,created_at) VALUES (?,?,?,CURRENT_TIMESTAMP)",
                email, encoder.encode(password), "Maze Tester");
        long user = players.findByEmail(email).orElseThrow().getId();
        db.update("INSERT INTO player_stats(user_id,total_xp,practice_xp) VALUES (?,0,0)", user);
        db.update("INSERT INTO user_world_unlocks(user_id,world_id,unlocked_at) VALUES (?,1,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at,completed_at) VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", user);
        return user;
    }

    private void completePrerequisite(long user) {
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'heaps','heaps-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'recursion','recursion-v1',5,CURRENT_TIMESTAMP)", user);
    }

    @Test
    void apiGatesPrerequisiteChecksTrailAnswerReplayAndPersistsAfterLogin() throws Exception {
        mvc.perform(get("/api/topics/backtracking/progress")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/questions/backtracking-01")).andExpect(status().isUnauthorized());

        String email = "backtracking-api-" + UUID.randomUUID() + "@codequest.test";
        String password = "backtracking-test-password";
        long user = createAccount(email, password);
        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE, jwt.issue(user));
        mvc.perform(get("/api/topics/backtracking/progress").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(get("/api/questions/backtracking-01").cookie(cookie)).andExpect(status().isForbidden());

        completePrerequisite(user);
        QuestionPack pack = packs.validate(source());
        mvc.perform(get("/api/topics/backtracking/progress").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("backtracking"))
                .andExpect(jsonPath("$.trails.length()").value(5))
                .andExpect(jsonPath("$.trails[0].unlocked").value(true))
                .andExpect(jsonPath("$.trails[0].questions.length()").value(10))
                .andExpect(jsonPath("$.trails[0].questions[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.trails[0].questions[1].status").value("LOCKED"))
                .andExpect(jsonPath("$.trails[1].unlocked").value(false))
                .andExpect(jsonPath("$.trails[4].questions.length()").value(10));
        mvc.perform(get("/api/questions/backtracking-02").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/backtracking/trails/2").cookie(cookie)).andExpect(status().isForbidden());

        var first = pack.questions().getFirst();
        mvc.perform(get("/api/questions/" + first.id()).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("backtracking"))
                .andExpect(jsonPath("$.trail").value(1))
                .andExpect(jsonPath("$.difficulty").value(first.difficulty()))
                .andExpect(jsonPath("$.xpReward").value(first.xpReward()));
        mvc.perform(post("/api/questions/" + first.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", "__wrong_answer__"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(false)).andExpect(jsonPath("$.awardedXp").value(0));
        mvc.perform(post("/api/questions/" + first.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", first.expectedAnswer()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.awardedXp").value(first.xpReward())).andExpect(jsonPath("$.progress.completed").value(1));
        mvc.perform(post("/api/questions/" + first.id() + "/submit").cookie(cookie).with(csrf())
                        .contentType("application/json").content(PackService.JSON.writeValueAsString(Map.of("answer", first.expectedAnswer()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.awardedXp").value(0));
        mvc.perform(post("/api/auth/logout").cookie(cookie).with(csrf())).andExpect(status().isNoContent());
        var login = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content(PackService.JSON.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        Cookie restored = login.getResponse().getCookie(JwtCookieFilter.COOKIE);
        mvc.perform(get("/api/topics/backtracking/progress").cookie(restored))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.totalXp").value(first.xpReward()));
    }
}
