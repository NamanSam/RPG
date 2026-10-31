package com.codequest.learning;

import com.codequest.content.PackService;
import com.codequest.content.QuestionPack;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.flywaydb.core.Flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecursionRuinsProgressionTest {
    private JdbcTemplate db;
    private PackService packs;
    private LearningService learning;
    private TransactionTemplate transactions;
    private QuestionPack recursion;

    private String source(String path) throws Exception {
        try (var input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:recursion-progress-" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        dataSource.setUser("sa");
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        db = new JdbcTemplate(dataSource);
        packs = new PackService(db);
        var heaps = packs.validate(source("content/dsa/heaps/v1.json"));
        assertThat(packs.publish(source("content/dsa/heaps/v1.json"))).isEqualTo("heaps-v1");
        recursion = packs.validate(source("content/dsa/recursion/v1.json"));
        assertThat(packs.publish(source("content/dsa/recursion/v1.json"))).isEqualTo("recursion-v1");
        assertThat(heaps.topic()).isEqualTo("heaps");
        learning = new LearningService(db);
        transactions = new TransactionTemplate(new JdbcTransactionManager(dataSource));
    }

    private long account(boolean heapComplete) {
        String email = "recursion-" + UUID.randomUUID() + "@codequest.test";
        db.update("INSERT INTO players(email,password_hash,display_name,created_at) VALUES (?,?,?,CURRENT_TIMESTAMP)",
                email, "test-only-hash", "Recursion Test");
        long user = db.queryForObject("SELECT id FROM players WHERE email=?", Long.class, email);
        db.update("INSERT INTO player_stats(user_id,total_xp,practice_xp) VALUES (?,0,0)", user);
        db.update("INSERT INTO user_world_unlocks(user_id,world_id,unlocked_at) VALUES (?,1,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at,completed_at) VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", user);
        if (heapComplete) {
            db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'heaps','heaps-v1',5,CURRENT_TIMESTAMP)", user);
            db.update("INSERT INTO user_topic_badges(user_id,badge_id,earned_at) VALUES (?,'heap-highlands-summit',CURRENT_TIMESTAMP)", user);
        }
        return user;
    }

    private <T> T inTransaction(java.util.function.Supplier<T> work) {
        return transactions.execute(status -> work.get());
    }

    @Test
    void prerequisiteSequentialProgressRewardsReplayConcurrencyAndBadgeAreEnforced() throws Exception {
        long blocked = account(false);
        assertThatThrownBy(() -> inTransaction(() -> learning.progress(blocked, "recursion")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Complete the previous DSA region first");
        assertThatThrownBy(() -> inTransaction(() -> learning.detail(blocked, recursion.questions().getFirst().id())))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inTransaction(() -> learning.submit(blocked, recursion.questions().getFirst().id(), recursion.questions().getFirst().expectedAnswer())))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_progress WHERE user_id=? AND topic_slug='recursion'", Integer.class, blocked)).isZero();

        long user = account(true);
        var start = inTransaction(() -> learning.progress(user, "recursion"));
        assertThat(start.totalXp()).isZero();
        assertThat(start.trails()).hasSize(5);
        assertThat(start.trails()).extracting(LearningService.Trail::unlocked)
                .containsExactly(true, false, false, false, false);
        assertThat(start.trails().getFirst().questions()).hasSize(10);
        assertThat(start.trails().getFirst().questions().getFirst().status()).isEqualTo("AVAILABLE");
        assertThat(start.trails().getFirst().questions().get(1).status()).isEqualTo("LOCKED");
        assertThatThrownBy(() -> inTransaction(() -> learning.trail(user, "recursion", 2)))
                .isInstanceOf(ResponseStatusException.class);

        int totalXp = 0;
        for (int index = 0; index < recursion.questions().size(); index++) {
            var question = recursion.questions().get(index);
            if (index + 1 < recursion.questions().size()) {
                var next = recursion.questions().get(index + 1);
                assertThatThrownBy(() -> inTransaction(() -> learning.detail(user, next.id())))
                        .as("question %s stays locked until its predecessor is completed", next.id())
                        .isInstanceOf(ResponseStatusException.class);
            }
            var wrong = inTransaction(() -> learning.submit(user, question.id(), "__definitely_not_the_answer__"));
            assertThat(wrong.correct()).isFalse();
            assertThat(wrong.awardedXp()).isZero();

            totalXp += question.xpReward();
            var right = inTransaction(() -> learning.submit(user, question.id(), question.expectedAnswer()));
            assertThat(right.correct()).isTrue();
            assertThat(right.awardedXp()).isEqualTo(question.xpReward());
            assertThat(right.progress().totalXp()).isEqualTo(totalXp);
            assertThat(right.trailCompletedNow()).isEqualTo((question.order() % 10) == 0);
            assertThat(right.topicCompletedNow()).isEqualTo(question.order() == 50);

            var replay = inTransaction(() -> learning.submit(user, question.id(), question.expectedAnswer()));
            assertThat(replay.awardedXp()).isZero();
            assertThat(replay.topicCompletedNow()).isFalse();
        }

        assertThat(totalXp).isEqualTo(900);
        var persisted = new LearningService(db).progress(user, "recursion");
        assertThat(persisted.completed()).isEqualTo(50);
        assertThat(persisted.complete()).isTrue();
        assertThat(persisted.totalXp()).isEqualTo(900);
        assertThat(persisted.badge()).isEqualTo("Recursion Ruins Archivist");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='recursion-ruins-archivist'", Integer.class, user)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_progress WHERE user_id=? AND topic_slug='recursion' AND completed_at IS NOT NULL", Integer.class, user)).isEqualTo(1);
    }

    @Test
    void concurrentQuestionAndFinalCompletionAwardXpAndBadgeOnce() throws Exception {
        long user = account(true);
        var question = recursion.questions().getFirst();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(3)) {
            List<java.util.concurrent.Future<LearningService.Result>> futures = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return inTransaction(() -> learning.submit(user, question.id(), question.expectedAnswer()));
                }));
            }
            start.countDown();
            int awarded = 0;
            for (var future : futures) awarded += future.get(15, TimeUnit.SECONDS).awardedXp();
            assertThat(awarded).isEqualTo(question.xpReward());
        }
        assertThat(inTransaction(() -> learning.progress(user, "recursion")).totalXp()).isEqualTo(question.xpReward());
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='recursion-ruins-archivist'", Integer.class, user)).isZero();

        for (var prior : recursion.questions().subList(1, 49)) {
            inTransaction(() -> learning.submit(user, prior.id(), prior.expectedAnswer()));
        }
        var finalQuestion = recursion.questions().getLast();
        var finalStart = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(3)) {
            List<java.util.concurrent.Future<LearningService.Result>> futures = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                futures.add(pool.submit(() -> {
                    finalStart.await();
                    return inTransaction(() -> learning.submit(user, finalQuestion.id(), finalQuestion.expectedAnswer()));
                }));
            }
            finalStart.countDown();
            int awarded = 0;
            int completionEvents = 0;
            for (var future : futures) {
                var result = future.get(15, TimeUnit.SECONDS);
                awarded += result.awardedXp();
                if (result.topicCompletedNow()) completionEvents++;
            }
            assertThat(awarded).isEqualTo(finalQuestion.xpReward());
            assertThat(completionEvents).isEqualTo(1);
        }
        var done = inTransaction(() -> learning.progress(user, "recursion"));
        assertThat(done.completed()).isEqualTo(50);
        assertThat(done.totalXp()).isEqualTo(900);
        assertThat(done.badge()).isEqualTo("Recursion Ruins Archivist");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='recursion-ruins-archivist'", Integer.class, user)).isEqualTo(1);
    }
}
