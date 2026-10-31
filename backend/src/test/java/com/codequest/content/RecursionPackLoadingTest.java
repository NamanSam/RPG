package com.codequest.content;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class RecursionPackLoadingTest {
    @Test
    void recursionRuinsResourceIsRegisteredValidAndHasExpectedRegionPrerequisite() throws Exception {
        String resourcePath = "content/dsa/recursion/v1.json";
        assertThat(ContentLoader.PACK_RESOURCES).contains(resourcePath);

        String source;
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        QuestionPack pack = new PackService(null).validate(source);
        assertThat(pack.topic()).isEqualTo("recursion");
        assertThat(pack.trails()).hasSize(5);
        assertThat(pack.questions()).hasSize(50);

        for (int trail = 1; trail <= 5; trail++) {
            final int number = trail;
            var questions = pack.questions().stream().filter(q -> q.trail() == number).toList();
            assertThat(questions).hasSize(10);
            assertThat(questions.stream().filter(q -> q.difficulty().equals("Easy")).count()).isEqualTo(4);
            assertThat(questions.stream().filter(q -> q.difficulty().equals("Medium")).count()).isEqualTo(4);
            assertThat(questions.stream().filter(q -> q.difficulty().equals("Hard")).count()).isEqualTo(2);
        }
        assertThat(pack.questions().stream().filter(q -> q.difficulty().equals("Easy")).count()).isEqualTo(20);
        assertThat(pack.questions().stream().filter(q -> q.difficulty().equals("Medium")).count()).isEqualTo(20);
        assertThat(pack.questions().stream().filter(q -> q.difficulty().equals("Hard")).count()).isEqualTo(10);

        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:recursion-loader;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        var db = new JdbcTemplate(dataSource);
        var packId = new PackService(db).publish(source);
        assertThat(packId).isEqualTo("recursion-v1");
        assertThat(db.queryForObject(
                "SELECT r.slug FROM topics t JOIN campaign_regions r ON r.slug=t.region_slug WHERE t.slug='recursion'", String.class))
                .isEqualTo("recursion-ruins");
        assertThat(db.queryForObject(
                "SELECT prerequisite_topic_slug FROM topic_prerequisites WHERE topic_slug='recursion'", String.class))
                .isEqualTo("heaps");
        assertThat(db.queryForObject(
                "SELECT active_pack FROM topics WHERE slug='recursion'", String.class))
                .isEqualTo("recursion-v1");
    }
}
