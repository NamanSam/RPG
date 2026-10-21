INSERT INTO campaign_regions VALUES ('string-harbor','dsa','String Harbor',NULL,2);
INSERT INTO topics(slug,region_slug,name) VALUES ('strings','string-harbor','Strings');
CREATE TABLE topic_prerequisites (
 topic_slug VARCHAR(60) PRIMARY KEY,
 prerequisite_topic_slug VARCHAR(60) NOT NULL,
 FOREIGN KEY(topic_slug) REFERENCES topics(slug),
 FOREIGN KEY(prerequisite_topic_slug) REFERENCES topics(slug)
);
INSERT INTO topic_prerequisites VALUES ('strings','arrays');
INSERT INTO topic_learning_rules VALUES ('strings','ACCOUNT',TRUE,FALSE);
INSERT INTO topic_badges VALUES ('string-harbor-navigator','strings','String Harbor Navigator');
