package com.codequest.auth;

import com.codequest.user.Player;

public record PlayerView(Long id, String email, String displayName, int totalXp, int level) {
    public static PlayerView from(Player player, int xp) {
        return new PlayerView(player.getId(), player.getEmail(), player.getDisplayName(), xp, 1 + xp / 300);
    }
}
