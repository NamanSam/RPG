package com.codequest.auth;

import com.codequest.user.PlayerRepository;
import com.codequest.progress.PlayerStatsRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired PlayerRepository players;
    @Autowired PlayerStatsRepository stats;
    @Autowired PasswordEncoder encoder;

    @Test void registerLoginRestoreAndLogout() throws Exception {
        var registration = mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"Knight@Example.com\",\"password\":\"test-password-123\",\"displayName\":\"Byte Knight\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.level").value(1)).andExpect(jsonPath("$.totalXp").value(0))
            .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        Cookie cookie = registration.getResponse().getCookie(JwtCookieFilter.COOKIE);
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(7200);
        assertThat(registration.getResponse().getHeader("Set-Cookie")).contains("SameSite=Lax");
        var player = players.findByEmail("knight@example.com").orElseThrow();
        assertThat(encoder.matches("test-password-123", player.getPasswordHash())).isTrue();
        assertThat(stats.findById(player.getId()).orElseThrow().getTotalXp()).isZero();
        mvc.perform(get("/api/auth/me").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Byte Knight"));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content("{\"email\":\"KNIGHT@example.com\",\"password\":\"test-password-123\"}"))
            .andExpect(status().isOk()).andExpect(cookie().exists(JwtCookieFilter.COOKIE));
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(cookie)).andExpect(status().isNoContent()).andExpect(cookie().maxAge(JwtCookieFilter.COOKIE, 0));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"knight@example.com\",\"password\":\"test-password-123\",\"displayName\":\"Other Knight\"}"))
            .andExpect(status().isConflict());
    }

    @Test void rejectsInvalidCredentialsCsrfAndTamperedTokens() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"invalid\",\"password\":\"short\",\"displayName\":\"A\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(new Cookie(JwtCookieFilter.COOKIE, "tampered-token")))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test void rejectsPasswordsExceedingBcryptByteLimit() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"email\":\"unicode@example.com\",\"password\":\"" + "界".repeat(30) + "\",\"displayName\":\"Explorer\"}"))
            .andExpect(status().isBadRequest());
        assertThat(players.findByEmail("unicode@example.com")).isEmpty();
    }
}
