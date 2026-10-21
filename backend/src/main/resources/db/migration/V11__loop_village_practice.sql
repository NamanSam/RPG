INSERT INTO topics(slug,region_slug,name) VALUES ('loop-village-practice','loop-village','Loop Village Practice');
INSERT INTO topic_learning_rules VALUES ('loop-village-practice','PRACTICE',TRUE,TRUE);
INSERT INTO topic_trail_requirements VALUES
 ('loop-village-practice',1,'the-gatekeeper',FALSE),
 ('loop-village-practice',2,'the-looping-mill',FALSE),
 ('loop-village-practice',3,'the-endless-well',FALSE),
 ('loop-village-practice',4,'the-endless-well',FALSE),
 ('loop-village-practice',5,'the-endless-well',TRUE);
INSERT INTO topic_badges VALUES ('loop-village-mastery','loop-village-practice','Loop Village Mastery');
