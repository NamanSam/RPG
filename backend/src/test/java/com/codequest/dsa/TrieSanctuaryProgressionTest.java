package com.codequest.dsa;

import com.codequest.content.PackService;
import com.codequest.content.QuestionPack;
import com.codequest.learning.LearningService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrieSanctuaryProgressionTest {
    private JdbcTemplate db;
    private PackService packs;
    private LearningService learning;
    private TransactionTemplate transactions;
    private QuestionPack trie;

    private String source(String path) throws Exception {
        try (var input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trie-progress-" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        dataSource.setUser("sa");
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        db = new JdbcTemplate(dataSource);
        packs = new PackService(db);
        packs.publish(source("content/dsa/heaps/v1.json"));
        packs.publish(source("content/dsa/recursion/v1.json"));
        packs.publish(source("content/dsa/backtracking/v1.json"));
        packs.publish(source("content/dsa/bitwise/v1.json"));
        trie = packs.validate(source("content/dsa/tries/v1.json"));
        assertThat(packs.publish(source("content/dsa/tries/v1.json"))).isEqualTo("trie-v1");
        learning = new LearningService(db);
        transactions = new TransactionTemplate(new JdbcTransactionManager(dataSource));
    }

    private long account(boolean bitwiseComplete) {
        String email = "trie-" + UUID.randomUUID() + "@codequest.test";
        db.update("INSERT INTO players(email,password_hash,display_name,created_at) VALUES (?,?,?,CURRENT_TIMESTAMP)",
                email, "test-only-hash", "Trie Tester");
        long user = db.queryForObject("SELECT id FROM players WHERE email=?", Long.class, email);
        db.update("INSERT INTO player_stats(user_id,total_xp,practice_xp) VALUES (?,0,0)", user);
        db.update("INSERT INTO user_world_unlocks(user_id,world_id,unlocked_at) VALUES (?,1,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_campaign_progress(user_id,campaign_slug,unlocked_at,completed_at) VALUES (?,'java',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'heaps','heaps-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'recursion','recursion-v1',5,CURRENT_TIMESTAMP)", user);
        db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'backtracking','backtracking-v1',5,CURRENT_TIMESTAMP)", user);
        if (bitwiseComplete) {
            db.update("INSERT INTO user_topic_progress(user_id,topic_slug,pack_id,unlocked_trail,completed_at) VALUES (?,'bitwise','bitwise-v1',5,CURRENT_TIMESTAMP)", user);
        }
        return user;
    }

    private <T> T inTransaction(java.util.function.Supplier<T> work) {
        return transactions.execute(status -> work.get());
    }

    @Test
    void prerequisiteSequentialProgressRewardsReplayConcurrencyAndBadgeAreEnforced() throws Exception {
        long blocked = account(false);
        assertThatThrownBy(() -> inTransaction(() -> learning.progress(blocked, "trie")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Complete the previous DSA region first");
        var firstQuestion = trie.questions().getFirst();
        assertThatThrownBy(() -> inTransaction(() -> learning.detail(blocked, firstQuestion.id())))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inTransaction(() -> learning.submit(blocked, firstQuestion.id(), firstQuestion.expectedAnswer())))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_progress WHERE user_id=? AND topic_slug='trie'", Integer.class, blocked)).isZero();

        long user = account(true);
        var start = inTransaction(() -> learning.progress(user, "trie"));
        assertThat(start.totalXp()).isZero();
        assertThat(start.trails()).hasSize(5);
        assertThat(start.trails()).extracting(LearningService.Trail::unlocked).containsExactly(true, false, false, false, false);
        assertThat(start.trails().getFirst().questions()).hasSize(10);
        assertThat(start.trails().getFirst().questions().getFirst().status()).isEqualTo("AVAILABLE");
        assertThat(start.trails().getFirst().questions().get(1).status()).isEqualTo("LOCKED");
        assertThatThrownBy(() -> inTransaction(() -> learning.trail(user, "trie", 2)))
                .isInstanceOf(ResponseStatusException.class);

        var wrong = inTransaction(() -> learning.submit(user, firstQuestion.id(), "__wrong_answer__"));
        assertThat(wrong.correct()).isFalse();
        assertThat(wrong.awardedXp()).isZero();
        int expectedTotal = trie.questions().stream().mapToInt(QuestionPack.Question::xpReward).sum();
        for (int index = 0; index < 49; index++) {
            var question = trie.questions().get(index);
            var nextQuestion = trie.questions().get(index + 1);
            assertThatThrownBy(() -> inTransaction(() -> learning.detail(user, nextQuestion.id())))
                    .as("question %s remains locked until its predecessor is complete", nextQuestion.id())
                    .isInstanceOf(ResponseStatusException.class);
            var accepted = inTransaction(() -> learning.submit(user, question.id(), question.expectedAnswer()));
            assertThat(accepted.correct()).isTrue();
            assertThat(accepted.awardedXp()).isEqualTo(question.xpReward());
            assertThat(accepted.trailCompletedNow()).isEqualTo(question.order() % 10 == 0);
            assertThat(accepted.topicCompletedNow()).isFalse();
            var replay = inTransaction(() -> learning.submit(user, question.id(), question.expectedAnswer()));
            assertThat(replay.awardedXp()).isZero();
        }
        assertThat(inTransaction(() -> learning.progress(user, "trie")).trails())
                .extracting(LearningService.Trail::unlocked).containsExactly(true, true, true, true, true);

        var finalQuestion = trie.questions().getLast();
        var startGate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Future<LearningService.Result>> futures = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return inTransaction(() -> learning.submit(user, finalQuestion.id(), finalQuestion.expectedAnswer()));
                }));
            }
            startGate.countDown();
            int awarded = 0;
            int completionEvents = 0;
            for (var future : futures) {
                var result = future.get(20, TimeUnit.SECONDS);
                awarded += result.awardedXp();
                if (result.topicCompletedNow()) completionEvents++;
            }
            assertThat(awarded).isEqualTo(finalQuestion.xpReward());
            assertThat(completionEvents).isEqualTo(1);
        }

        var done = inTransaction(() -> learning.progress(user, "trie"));
        assertThat(done.completed()).isEqualTo(50);
        assertThat(done.complete()).isTrue();
        assertThat(done.totalXp()).isEqualTo(expectedTotal);
        assertThat(done.badge()).isEqualTo("Trie Sanctuary Cartographer");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_badges WHERE user_id=? AND badge_id='trie-sanctuary-cartographer'", Integer.class, user)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_topic_progress WHERE user_id=? AND topic_slug='trie' AND completed_at IS NOT NULL", Integer.class, user)).isEqualTo(1);
        assertThat(expectedTotal).isEqualTo(900);

        LearningService reloaded = new LearningService(db);
        var reloadedProgress = inTransaction(() -> reloaded.progress(user, "trie"));
        assertThat(reloadedProgress.completed()).isEqualTo(50);
        assertThat(reloadedProgress.complete()).isTrue();
        assertThat(reloadedProgress.totalXp()).isEqualTo(900);
        assertThat(reloadedProgress.badge()).isEqualTo("Trie Sanctuary Cartographer");
        assertThat(db.queryForObject("SELECT total_xp FROM player_stats WHERE user_id=?", Integer.class, user)).isEqualTo(900);
    }
}

