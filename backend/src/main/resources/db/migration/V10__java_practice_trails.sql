ALTER TABLE player_stats ADD COLUMN practice_xp INT NOT NULL DEFAULT 0 CHECK (practice_xp >= 0);
CREATE TABLE topic_learning_rules (
 topic_slug VARCHAR(60) PRIMARY KEY,
 xp_pool VARCHAR(20) NOT NULL,
 sequential_questions BOOLEAN NOT NULL,
 independent_trails BOOLEAN NOT NULL,
 FOREIGN KEY(topic_slug) REFERENCES topics(slug),
 CHECK (xp_pool IN ('ACCOUNT','PRACTICE'))
);
CREATE TABLE topic_trail_requirements (
 topic_slug VARCHAR(60) NOT NULL,
 trail_number INT NOT NULL,
 story_quest_id VARCHAR(60) NOT NULL,
 require_prior_trails BOOLEAN NOT NULL DEFAULT FALSE,
 PRIMARY KEY(topic_slug,trail_number),
 FOREIGN KEY(topic_slug) REFERENCES topics(slug),
 FOREIGN KEY(story_quest_id) REFERENCES quests(id)
);
INSERT INTO topics(slug,region_slug,name) VALUES ('beach-practice','beginner-beach','Beginner Beach Practice');
INSERT INTO topic_learning_rules VALUES ('beach-practice','PRACTICE',TRUE,TRUE);
INSERT INTO topic_trail_requirements VALUES
 ('beach-practice',1,'first-variable',FALSE),
 ('beach-practice',2,'choose-the-type',FALSE),
 ('beach-practice',3,'operator-training',FALSE),
 ('beach-practice',4,'operator-training',FALSE),
 ('beach-practice',5,'operator-training',TRUE);
INSERT INTO topic_badges VALUES ('beginner-beach-mastery','beach-practice','Beginner Beach Mastery');
