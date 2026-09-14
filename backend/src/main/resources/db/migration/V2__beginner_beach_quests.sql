CREATE TABLE quests (
  id VARCHAR(60) PRIMARY KEY,
  world_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  title VARCHAR(100) NOT NULL,
  topic VARCHAR(80) NOT NULL,
  difficulty VARCHAR(20) NOT NULL,
  reward INT NOT NULL,
  story VARCHAR(1000) NOT NULL,
  lesson VARCHAR(1500) NOT NULL,
  task_text VARCHAR(1000) NOT NULL,
  starter_code VARCHAR(1000) NOT NULL,
  hint VARCHAR(500) NOT NULL,
  answer_kind VARCHAR(30) NOT NULL,
  expected_answer VARCHAR(80) NOT NULL,
  success_feedback VARCHAR(500) NOT NULL,
  incorrect_feedback VARCHAR(500) NOT NULL,
  CONSTRAINT fk_quest_world FOREIGN KEY (world_id) REFERENCES worlds(id),
  CONSTRAINT uk_quest_order UNIQUE (world_id, sort_order)
);

INSERT INTO quests VALUES
('first-variable', 1, 1, 'First Variable', 'Variables', 'Easy', 50,
 'Captain Byte needs five torches before the lighthouse can guide ships home. Help him label the supplies.',
 'A variable gives a value a name. In Java, int stores whole numbers. For example, int shells = 3; creates a variable named shells with the value 3. The equals sign assigns the value, and the semicolon ends the statement.',
 'Complete the blank so the int variable torches starts with the value 5. Enter only the whole-number literal, not the full statement.',
 'int torches = ____;', 'The number belongs on the right side of the equals sign.', 'INTEGER_TOKEN', '5',
 'Exactly! int torches = 5; declares an integer variable and initializes it to five.',
 'Not quite. The keeper asked for five torches. Enter the whole-number literal that belongs in the blank.'),
('choose-the-type', 1, 2, 'Choose the Type', 'Java Data Types', 'Easy', 50,
 'The signal keeper needs to record whether the lighthouse is lit. It has just two states: true or false.',
 'Java types describe the values a variable can hold. int holds whole numbers, double holds decimal numbers, char holds a single character, and boolean holds true or false. Type keywords are lowercase and case-sensitive.',
 'Choose the Java type that makes this declaration valid.',
 '____ lighthouseLit = true;', 'Look for the type with exactly two possible values: true and false.', 'TYPE_CHOICE', 'boolean',
 'Correct! boolean lighthouseLit = true; stores a true-or-false value.',
 'Try again. A true-or-false value needs a boolean type, rather than a number or character type.'),
('operator-training', 1, 3, 'Operator Training', 'Operators', 'Easy', 75,
 'At the supply chest, Captain Byte divides seventeen shells into groups of five. Count the shells left over to finish your beach training.',
 'Java arithmetic operators include +, -, *, / and %. The remainder operator % gives what remains after division. For example, 10 % 4 is 2 because two groups of four use eight, leaving two.',
 'What value is assigned to leftover? Enter only the whole-number result.',
 'int leftover = 17 % 5;', 'Three groups of five use fifteen shells. How many remain?', 'INTEGER_TOKEN', '2',
 'Well done! 17 % 5 is 2. Three groups use fifteen shells, leaving two.',
 'Not quite. % means remainder, not division or percentage. Subtract three groups of five from seventeen.');

CREATE TABLE user_quest_progress (
  user_id BIGINT NOT NULL,
  quest_id VARCHAR(60) NOT NULL,
  attempt_count INT NOT NULL DEFAULT 0,
  completed_at TIMESTAMP(6) NULL,
  PRIMARY KEY (user_id, quest_id),
  FOREIGN KEY (user_id) REFERENCES players(id),
  FOREIGN KEY (quest_id) REFERENCES quests(id)
);
CREATE TABLE badges (
  id VARCHAR(60) PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  world_id BIGINT NOT NULL,
  FOREIGN KEY (world_id) REFERENCES worlds(id)
);
INSERT INTO badges VALUES ('beginner-beach', 'Beginner Beach Explorer', 1);
CREATE TABLE user_badges (
  user_id BIGINT NOT NULL,
  badge_id VARCHAR(60) NOT NULL,
  earned_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (user_id, badge_id),
  FOREIGN KEY (user_id) REFERENCES players(id),
  FOREIGN KEY (badge_id) REFERENCES badges(id)
);
CREATE TABLE user_world_unlocks (
  user_id BIGINT NOT NULL,
  world_id BIGINT NOT NULL,
  unlocked_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (user_id, world_id),
  FOREIGN KEY (user_id) REFERENCES players(id),
  FOREIGN KEY (world_id) REFERENCES worlds(id)
);
INSERT INTO user_world_unlocks (user_id, world_id, unlocked_at)
SELECT id, 1, CURRENT_TIMESTAMP FROM players;
