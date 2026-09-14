package com.codequest.progress;

import com.codequest.user.Player;
import jakarta.persistence.*;

@Entity @Table(name = "player_stats")
public class PlayerStats {
    @Id @Column(name = "user_id") private Long userId;
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "user_id") private Player player;
    @Column(name = "total_xp", nullable = false) private int totalXp;
    protected PlayerStats() {}
    public PlayerStats(Player player) { this.player = player; }
    public int getTotalXp() { return totalXp; }
}
