INSERT INTO topics(slug,region_slug,name) VALUES ('oop-canyon-practice','oop-canyon','OOP Canyon Practice');
INSERT INTO topic_learning_rules VALUES ('oop-canyon-practice','PRACTICE',TRUE,TRUE);
INSERT INTO topic_trail_requirements VALUES
 ('oop-canyon-practice',1,'blueprint-maker',FALSE),
 ('oop-canyon-practice',2,'the-hidden-vault',FALSE),
 ('oop-canyon-practice',3,'bloodline-of-classes',FALSE),
 ('oop-canyon-practice',4,'many-forms',FALSE),
 ('oop-canyon-practice',5,'many-forms',TRUE);
INSERT INTO topic_badges VALUES ('oop-canyon-mastery','oop-canyon-practice','OOP Canyon Mastery');
