package com.codequest.quest;

import com.codequest.auth.JwtCookieFilter;
import com.codequest.content.PackService;
import com.codequest.user.PlayerRepository;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ArrayForestIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired QuestService service;
    @Autowired QuestRepository repository;
    @Autowired PlayerRepository players;

    record Account(long id, String email, Cookie cookie) {}

    Account user() throws Exception {
        String email="forest-"+UUID.randomUUID()+"@codequest.test";
        var result=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","forest-test-password","displayName","Forest Explorer"))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.totalXp").value(0)).andReturn();
        return new Account(players.findByEmail(email).orElseThrow().getId(),email,result.getResponse().getCookie(JwtCookieFilter.COOKIE));
    }

    void completeWorld(long userId, long worldId) {
        repository.allQuests().stream().filter(q->q.worldId()==worldId)
            .forEach(q->assertThat(service.submit(userId,q.id(),q.expectedAnswer()).correct()).isTrue());
    }

    @Test void forestLocksRewardsBadgeUnlockAndPersistence() throws Exception {
        var user=user();
        mvc.perform(get("/api/quests/the-lost-index").cookie(user.cookie())).andExpect(status().isForbidden());
        mvc.perform(post("/api/quests/the-lost-index/submit").cookie(user.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"9\"}"))
            .andExpect(status().isForbidden());
        completeWorld(user.id(),1);
        mvc.perform(get("/api/quests/the-lost-index").cookie(user.cookie())).andExpect(status().isForbidden());
        completeWorld(user.id(),2);

        var start=service.progress(user.id());
        assertThat(start.totalXp()).isEqualTo(475);
        assertThat(start.forestCompleted()).isZero();
        assertThat(start.forestQuests()).extracting(QuestService.QuestSummary::status)
            .containsExactly("AVAILABLE","LOCKED","LOCKED");
        mvc.perform(post("/api/quests/forest-traversal/submit").cookie(user.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"10\"}"))
            .andExpect(status().isForbidden());

        var wrong=service.submit(user.id(),"the-lost-index","8");
        assertThat(wrong.awardedXp()).isZero();
        assertThat(wrong.progress().totalXp()).isEqualTo(475);
        var first=service.submit(user.id(),"the-lost-index","9");
        assertThat(first.awardedXp()).isEqualTo(100);
        assertThat(first.progress().forestQuests().get(1).status()).isEqualTo("AVAILABLE");
        assertThat(service.submit(user.id(),"the-lost-index","9").awardedXp()).isZero();
        assertThat(service.submit(user.id(),"forest-traversal","10").awardedXp()).isEqualTo(125);
        assertThat(service.progress(user.id()).forestQuests().get(2).status()).isEqualTo("AVAILABLE");

        var ready=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var one=pool.submit(()->{ ready.await(); return service.submit(user.id(),"hidden-maximum","-3"); });
            var two=pool.submit(()->{ ready.await(); return service.submit(user.id(),"hidden-maximum","-3"); });
            ready.countDown();
            var a=one.get(10,TimeUnit.SECONDS); var b=two.get(10,TimeUnit.SECONDS);
            assertThat(a.awardedXp()+b.awardedXp()).isEqualTo(150);
            assertThat((a.completedWorld()!=null?1:0)+(b.completedWorld()!=null?1:0)).isEqualTo(1);
        }
        var complete=service.progress(user.id());
        assertThat(complete.totalXp()).isEqualTo(850);
        assertThat(complete.forestCompleted()).isEqualTo(3);
        assertThat(complete.badges()).extracting(QuestService.BadgeView::id)
            .containsExactlyInAnyOrder("beginner-beach","loop-village","array-forest");
        assertThat(complete.worlds().stream().filter(w->w.slug().equals("oop-canyon")).findFirst().orElseThrow().unlocked()).isTrue();
        assertThat(service.submit(user.id(),"hidden-maximum","-3").awardedXp()).isZero();

        mvc.perform(post("/api/auth/logout").cookie(user.cookie()).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",user.email(),"password","forest-test-password"))))
            .andExpect(status().isOk()).andReturn();
        mvc.perform(get("/api/me/progress").cookie(login.getResponse().getCookie(JwtCookieFilter.COOKIE)))
            .andExpect(jsonPath("$.totalXp").value(850)).andExpect(jsonPath("$.forestCompleted").value(3))
            .andExpect(jsonPath("$.badges[?(@.id == 'array-forest')]").exists());
    }
}
