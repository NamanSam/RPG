CREATE TABLE players (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(30) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uk_players_email UNIQUE (email)
);

CREATE TABLE player_stats (
  user_id BIGINT NOT NULL PRIMARY KEY,
  total_xp INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_stats_player FOREIGN KEY (user_id) REFERENCES players(id),
  CONSTRAINT ck_nonnegative_xp CHECK (total_xp >= 0)
);

CREATE TABLE worlds (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  slug VARCHAR(60) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL,
  sort_order INT NOT NULL UNIQUE,
  theme_key VARCHAR(30) NOT NULL,
  planned_quest_count INT NOT NULL
);

INSERT INTO worlds (slug, name, sort_order, theme_key, planned_quest_count) VALUES
('beginner-beach', 'Beginner Beach', 1, 'beach', 3),
('loop-village', 'Loop Village', 2, 'village', 3),
('array-forest', 'Array Forest', 3, 'forest', 2),
('oop-canyon', 'OOP Canyon', 4, 'canyon', 2),
('collection-kingdom', 'Collection Kingdom', 5, 'kingdom', 2);
