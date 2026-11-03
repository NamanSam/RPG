package com.codequest.content;

import java.nio.charset.StandardCharsets;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class BacktrackingPackLoadingTest {
    @Test
    void backtrackingMazePackLoadsAndRegistersAfterRecursionRuins() throws Exception {
        String resourcePath = "content/dsa/backtracking/v1.json";
        assertThat(ContentLoader.PACK_RESOURCES).contains(resourcePath);

        String source;
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        QuestionPack pack = new PackService(null).validate(source);
        assertThat(pack.topic()).isEqualTo("backtracking");
        assertThat(pack.trails()).extracting(QuestionPack.Trail::name).containsExactly(
                "Backtracking Fundamentals", "Subsets and Subsequences", "Permutations and Combinations",
                "Constraint Satisfaction Problems", "Advanced Backtracking Challenges");
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
        dataSource.setURL("jdbc:h2:mem:backtracking-loader;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        var db = new JdbcTemplate(dataSource);

        assertThat(db.queryForObject(
                "SELECT name FROM topics WHERE slug='backtracking'", String.class)).isEqualTo("Backtracking Maze");
        assertThat(db.queryForObject(
                "SELECT r.name FROM topics t JOIN campaign_regions r ON r.slug=t.region_slug WHERE t.slug='backtracking'",
                String.class)).isEqualTo("Backtracking Maze");
        assertThat(db.queryForObject(
                "SELECT prerequisite_topic_slug FROM topic_prerequisites WHERE topic_slug='backtracking'",
                String.class)).isEqualTo("recursion");

        var packId = new PackService(db).publish(source);
        assertThat(packId).isEqualTo("backtracking-v1");
        assertThat(db.queryForObject(
                "SELECT active_pack FROM topics WHERE slug='backtracking'", String.class)).isEqualTo("backtracking-v1");
        assertThat(db.queryForObject(
                "SELECT COUNT(*) FROM question_trails WHERE pack_id='backtracking-v1'", Integer.class)).isEqualTo(5);
        assertThat(db.queryForList(
                "SELECT name FROM question_trails WHERE pack_id='backtracking-v1' ORDER BY trail_number", String.class))
                .containsExactlyElementsOf(pack.trails().stream().map(QuestionPack.Trail::name).toList());
        assertThat(db.queryForObject(
                "SELECT COUNT(*) FROM question_versions WHERE pack_id='backtracking-v1'", Integer.class)).isEqualTo(50);
    }
}
