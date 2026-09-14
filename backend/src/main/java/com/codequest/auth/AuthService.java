package com.codequest.auth;

import com.codequest.user.*;
import com.codequest.progress.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final PlayerRepository players;
    private final PlayerStatsRepository stats;
    private final PasswordEncoder encoder;
    private final String dummyHash;
    private final JdbcTemplate db;
    public AuthService(PlayerRepository players, PlayerStatsRepository stats, PasswordEncoder encoder, JdbcTemplate db) {
        this.players = players; this.stats = stats; this.encoder = encoder;
        this.db = db;
        dummyHash = encoder.encode("unused-timing-equalizer");
    }
    @Transactional
    public PlayerView register(String email, String password, String displayName) {
        validatePasswordBytes(password);
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        String name = displayName.strip();
        if (name.length() < 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adventurer name must contain at least two characters.");
        if (players.existsByEmail(normalizedEmail)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with that email already exists.");
        Player player = players.saveAndFlush(new Player(normalizedEmail, encoder.encode(password), name));
        stats.save(new PlayerStats(player));
        db.update("INSERT INTO user_world_unlocks (user_id, world_id, unlocked_at) VALUES (?, 1, CURRENT_TIMESTAMP)", player.getId());
        return PlayerView.from(player, 0);
    }
    @Transactional(readOnly = true)
    public PlayerView login(String email, String password) {
        validatePasswordBytes(password);
        var player = players.findByEmail(email.strip().toLowerCase(Locale.ROOT));
        boolean matches = encoder.matches(password, player.map(Player::getPasswordHash).orElse(dummyHash));
        if (!matches || player.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        return view(player.get());
    }
    @Transactional(readOnly = true)
    public PlayerView me(long userId) {
        return view(players.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.")));
    }
    private PlayerView view(Player player) { return PlayerView.from(player, stats.findById(player.getId()).orElseThrow().getTotalXp()); }
    private void validatePasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes.");
    }
}
