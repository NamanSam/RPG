INSERT INTO quests (id, world_id, sort_order, title, topic, difficulty, reward, story, lesson, task_text, starter_code, hint, answer_kind, expected_answer, success_feedback, incorrect_feedback) VALUES
('the-gatekeeper', 2, 1, 'The Gatekeeper', 'if / else conditions', 'Easy', 75,
 'The village gate opens for travelers carrying at least ten coins. The Gatekeeper needs a condition that welcomes everyone who can pay, including travelers with exactly ten.',
 'An if statement runs its block when a condition is true; else runs when it is false. Java comparison operators include > (greater than), >= (greater than or equal to), and == (equal to). At least includes the boundary value.',
 'Choose the condition that opens the gate for any traveler with at least 10 coins.',
 'if (____) {
    System.out.println("Gate open");
} else {
    System.out.println("Please wait");
}',
 'The condition must accept both 10 and 11 coins, but reject 9.', 'CODE_CHOICE', 'coins >= 10',
 'Correct! coins >= 10 includes exactly ten and every larger value. The else block handles fewer than ten coins.',
 'Not quite. At least ten includes ten and all larger values. Check the boundary as well as travelers with extra coins.'),
('the-looping-mill', 2, 2, 'The Looping Mill', 'for loop', 'Easy', 100,
 'The miller has three sacks of grain. Set the mill to turn exactly three times, starting the counter at zero and increasing it by one each turn.',
 'A for loop has initialization, a condition, and an update. Initialization runs once. The condition is checked before each iteration. The update runs after the body. Starting at zero with a strict upper bound of three visits counter values 0, 1, and 2.',
 'Choose the for-loop header that runs millTurn() exactly three times, starting i at 0 and increasing i by 1.',
 '____ {
    millTurn();
}',
 'Count the values of i that pass the condition. Using <= 3 would include a fourth turn at i = 3.', 'CODE_CHOICE', 'for (int i = 0; i < 3; i++)',
 'Exactly! The body runs for i = 0, 1, and 2. After the update to 3, i < 3 is false and the loop stops.',
 'Try again. The mill needs the counter values 0, 1, and 2 only. Check the starting value, condition, and update.'),
('the-endless-well', 2, 3, 'The Endless Well', 'while loop', 'Medium', 125,
 'The Well Keeper raises water two liters at a time. The bucket starts empty, and the keeper continues while it holds fewer than seven liters. Work out when the keeper stops.',
 'A while loop checks its condition before each iteration and may run zero times. Its body must make progress toward stopping. Here water += 2 increases water by two. A loop can cross a threshold rather than finish exactly on it.',
 'What is the final value of water after this loop finishes? Enter only the whole-number result.',
 'int water = 0;
while (water < 7) {
    water += 2;
}',
 'After three iterations water is 6. The condition is still true, so one more iteration runs.', 'INTEGER_TOKEN', '8',
 'Well done! water changes from 0 to 2, 4, 6, and 8. At 8, water < 7 is false, so the loop stops after four iterations.',
 'Not quite. Track the value after each addition of two. The condition is checked before the next iteration, not halfway through an addition.');

CREATE TABLE quest_options (
  quest_id VARCHAR(60) NOT NULL,
  sort_order INT NOT NULL,
  answer_value VARCHAR(80) NOT NULL,
  PRIMARY KEY (quest_id, sort_order),
  FOREIGN KEY (quest_id) REFERENCES quests(id)
);
INSERT INTO quest_options VALUES
('the-gatekeeper', 1, 'coins > 10'),
('the-gatekeeper', 2, 'coins == 10'),
('the-gatekeeper', 3, 'coins >= 10'),
('the-gatekeeper', 4, 'coins < 10'),
('the-looping-mill', 1, 'for (int i = 0; i <= 3; i++)'),
('the-looping-mill', 2, 'for (int i = 0; i < 3; i++)'),
('the-looping-mill', 3, 'for (int i = 0; i < 3; i--)'),
('the-looping-mill', 4, 'for (int i = 1; i < 3; i++)');

INSERT INTO badges (id, name, world_id) VALUES ('loop-village', 'Loop Village Pathfinder', 2);
