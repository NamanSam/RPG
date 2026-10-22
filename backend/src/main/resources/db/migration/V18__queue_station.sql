INSERT INTO campaign_regions VALUES ('queue-station','dsa','Queue Station',NULL,5);
INSERT INTO topics(slug,region_slug,name) VALUES ('queues','queue-station','Queues');
INSERT INTO topic_prerequisites VALUES ('queues','stacks');
INSERT INTO topic_learning_rules VALUES ('queues','ACCOUNT',TRUE,FALSE);
INSERT INTO topic_badges VALUES ('queue-station-conductor','queues','Queue Station Conductor');
