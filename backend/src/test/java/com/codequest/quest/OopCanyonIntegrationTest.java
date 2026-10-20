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
class OopCanyonIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired QuestService service;
    @Autowired QuestRepository repository;
    @Autowired PlayerRepository players;

    record Account(long id, String email, Cookie cookie) {}

    Account user() throws Exception {
        String email="canyon-"+UUID.randomUUID()+"@codequest.test";
        var result=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",email,"password","canyon-test-password","displayName","Forest Explorer"))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.totalXp").value(0)).andReturn();
        return new Account(players.findByEmail(email).orElseThrow().getId(),email,result.getResponse().getCookie(JwtCookieFilter.COOKIE));
    }

    void completeWorld(long userId, long worldId) {
        repository.allQuests().stream().filter(q->q.worldId()==worldId)
            .forEach(q->assertThat(service.submit(userId,q.id(),q.expectedAnswer()).correct()).isTrue());
    }

    @Test void canyonLocksRewardsBadgeUnlockAndPersistence() throws Exception {
        var user=user();
        mvc.perform(get("/api/quests/blueprint-maker").cookie(user.cookie())).andExpect(status().isForbidden());
        mvc.perform(post("/api/quests/blueprint-maker/submit").cookie(user.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"Bridge bridge = new Bridge();\"}"))
            .andExpect(status().isForbidden());
        completeWorld(user.id(),1);
        mvc.perform(get("/api/quests/blueprint-maker").cookie(user.cookie())).andExpect(status().isForbidden());
        completeWorld(user.id(),2);
        mvc.perform(get("/api/quests/blueprint-maker").cookie(user.cookie())).andExpect(status().isForbidden());
        completeWorld(user.id(),3);

        var start=service.progress(user.id());
        assertThat(start.totalXp()).isEqualTo(850);
        assertThat(start.canyonCompleted()).isZero();
        assertThat(start.canyonQuests()).extracting(QuestService.QuestSummary::status)
            .containsExactly("AVAILABLE","LOCKED","LOCKED","LOCKED");
        mvc.perform(post("/api/quests/the-hidden-vault/submit").cookie(user.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"10\"}"))
            .andExpect(status().isForbidden());

        for (String id : java.util.List.of("bloodline-of-classes", "many-forms")) {
            mvc.perform(get("/api/quests/"+id).cookie(user.cookie())).andExpect(status().isForbidden());
            mvc.perform(post("/api/quests/"+id+"/submit").cookie(user.cookie()).with(csrf()).contentType("application/json").content("{\"answer\":\"Rope\"}")).andExpect(status().isForbidden());
        }
        var wrong=service.submit(user.id(),"blueprint-maker","8");
        assertThat(wrong.awardedXp()).isZero();
        assertThat(wrong.progress().totalXp()).isEqualTo(850);
        var first=service.submit(user.id(),"blueprint-maker","Bridge bridge = new Bridge();");
        assertThat(first.awardedXp()).isEqualTo(125);
        assertThat(first.progress().canyonQuests().get(1).status()).isEqualTo("AVAILABLE");
        assertThat(service.submit(user.id(),"blueprint-maker","Bridge bridge = new Bridge();").awardedXp()).isZero();
        assertThat(service.submit(user.id(),"the-hidden-vault","private").awardedXp()).isEqualTo(150);
        assertThat(service.progress(user.id()).canyonQuests().get(2).status()).isEqualTo("AVAILABLE");

        assertThat(service.submit(user.id(),"bloodline-of-classes","extends").awardedXp()).isEqualTo(175);
        var ready=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var one=pool.submit(()->{ ready.await(); return service.submit(user.id(),"many-forms","Rope"); });
            var two=pool.submit(()->{ ready.await(); return service.submit(user.id(),"many-forms","Rope"); });
            ready.countDown();
            var a=one.get(10,TimeUnit.SECONDS); var b=two.get(10,TimeUnit.SECONDS);
            assertThat(a.awardedXp()+b.awardedXp()).isEqualTo(200);
            assertThat((a.completedWorld()!=null?1:0)+(b.completedWorld()!=null?1:0)).isEqualTo(1);
        }
        var complete=service.progress(user.id());
        assertThat(complete.totalXp()).isEqualTo(1500);
        assertThat(complete.canyonCompleted()).isEqualTo(4);
        assertThat(complete.badges()).extracting(QuestService.BadgeView::id)
            .containsExactlyInAnyOrder("beginner-beach","loop-village","array-forest","oop-canyon");
        assertThat(complete.worlds().stream().filter(w->w.slug().equals("collection-kingdom")).findFirst().orElseThrow().unlocked()).isTrue();
        assertThat(service.submit(user.id(),"many-forms","Rope").awardedXp()).isZero();

        mvc.perform(post("/api/auth/logout").cookie(user.cookie()).with(csrf())).andExpect(status().isNoContent());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content(PackService.JSON.writeValueAsString(Map.of("email",user.email(),"password","canyon-test-password"))))
            .andExpect(status().isOk()).andReturn();
        mvc.perform(get("/api/me/progress").cookie(login.getResponse().getCookie(JwtCookieFilter.COOKIE)))
            .andExpect(jsonPath("$.totalXp").value(1500)).andExpect(jsonPath("$.canyonCompleted").value(4))
            .andExpect(jsonPath("$.badges[?(@.id == 'oop-canyon')]").exists());
    }
}


