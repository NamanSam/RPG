package com.codequest.learning;

import com.codequest.auth.AuthService;
import com.codequest.user.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

/** Explicit local test fixture only: excluded from application artifacts and ordinary test runs. */
@SpringBootTest @ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named="CODEQUEST_ARRAYS_FIXTURE", matches="true")
class ArrayDemoFixtureTest {
    @Autowired AuthService auth; @Autowired PlayerRepository players; @Autowired JdbcTemplate db;
    @Test void createDedicatedLocalDemoAccount() {
        assertThat(System.getenv("SPRING_DATASOURCE_URL")).startsWith("jdbc:mysql://localhost:3307/codequest");
        String email=System.getenv("CODEQUEST_ARRAYS_EMAIL"), password=System.getenv("CODEQUEST_ARRAYS_PASSWORD");
        assertThat(email).matches("arrays-(demo|smoke-[a-f0-9]+)@codequest\\.test");assertThat(password).hasSizeBetween(8,64);
        long id=players.findByEmail(email).map(p->{auth.login(email,password);return p.getId();}).orElseGet(()->auth.register(email,password,"Array Isles Demo").id());
        if(db.queryForObject("SELECT COUNT(*) FROM user_campaign_progress WHERE user_id=? AND campaign_slug='java'",Integer.class,id)==0)
            db.update("INSERT INTO user_campaign_progress VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id);
        // Do not touch legacy quests, XP, badges or real users. This account alone is a fixture.
    }
}
