UPDATE worlds SET planned_quest_count=3 WHERE id IN (3,5);
UPDATE worlds SET planned_quest_count=4 WHERE id=4;
INSERT INTO badges VALUES ('array-forest','Array Forest Ranger',3),('oop-canyon','OOP Canyon Architect',4),('collection-kingdom','Collection Kingdom Keeper',5);
INSERT INTO quests VALUES ('the-lost-index',3,1,'The Lost Index','Array basics and indexing','Easy',100,'Ranger Index has hidden a trail marker in the third slot of a supply array.','Array indices begin at zero. Index 2 refers to the third entry.','What value does markers[2] contain?','int[] markers = {4, 7, 9, 2};','Count positions starting at zero.','INTEGER_TOKEN','9','Index 2 holds 9.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quests VALUES ('forest-traversal',3,2,'Forest Traversal','Traversing arrays with loops','Medium',125,'Count the lanterns along the winding trail to illuminate the ruins.','An enhanced for loop visits every array entry once. Accumulate each value into a running total.','What final value is printed?','int[] lanterns = {2, 4, 3, 1};
int total = 0;
for (int count : lanterns) total += count;
System.out.println(total);','Add all four entries.','INTEGER_TOKEN','10','The running totals are 2, 6, 9 and 10.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quests VALUES ('hidden-maximum',3,3,'Hidden Maximum','Finding the maximum','Medium',150,'The ancient ruin opens when you identify the highest signal among its stones.','Initialize the maximum from the first element, then replace it whenever a larger value appears. This also handles negative values.','What is the maximum value of these signals?','int[] signals = {-8, -3, -11, -5};','The largest negative number is closest to zero.','INTEGER_TOKEN','-3','-3 exceeds every other signal. Starting the maximum at zero would be incorrect.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quests VALUES ('blueprint-maker',4,1,'Blueprint Maker','Classes and Objects','Easy',125,'The Architect needs a new Bridge object from an existing class.','A class defines a blueprint. The new keyword creates an instance by invoking its constructor.','Choose the statement that creates a Bridge object named bridge.','class Bridge { }
// Create an instance below.','Use new followed by the constructor.','CODE_CHOICE','Bridge bridge = new Bridge();','Bridge bridge = new Bridge(); declares a reference and creates an instance.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quest_options VALUES ('blueprint-maker',1,'Bridge bridge = new Bridge();');
INSERT INTO quest_options VALUES ('blueprint-maker',2,'Bridge bridge = Bridge;');
INSERT INTO quest_options VALUES ('blueprint-maker',3,'new class Bridge;');
INSERT INTO quests VALUES ('the-hidden-vault',4,2,'The Hidden Vault','Encapsulation','Medium',150,'Protect the vault''s coin balance so callers must use its controlled deposit method.','Encapsulation hides internal state. A private field is accessed through behavior provided by the class.','Which access modifier keeps coins accessible only inside Vault?','class Vault {
    ____ int coins;
    public void deposit(int amount) {
        if (amount > 0) coins += amount;
    }
}','Hide the field from callers.','CODE_CHOICE','private','private restricts the field to the declaring class, preserving the deposit rule.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quest_options VALUES ('the-hidden-vault',1,'public');
INSERT INTO quest_options VALUES ('the-hidden-vault',2,'private');
INSERT INTO quest_options VALUES ('the-hidden-vault',3,'protected');
INSERT INTO quests VALUES ('bloodline-of-classes',4,3,'Bloodline of Classes','Inheritance','Medium',175,'An apprentice blueprint should inherit the tools of the Architect.','A Java class uses extends to inherit from one superclass. Inherited accessible methods can be reused.','Fill the missing inheritance keyword.','class Architect { public void build() { } }
class Apprentice ____ Architect { }','The parent is a class, not an interface.','CODE_CHOICE','extends','extends establishes the superclass relationship.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quest_options VALUES ('bloodline-of-classes',1,'implements');
INSERT INTO quest_options VALUES ('bloodline-of-classes',2,'extends');
INSERT INTO quest_options VALUES ('bloodline-of-classes',3,'inherits');
INSERT INTO quests VALUES ('many-forms',4,4,'Many Forms','Polymorphism','Hard',200,'Two bridge designs share a base type, but the Architect must predict which design speaks.','Overridden instance methods dispatch according to the runtime object, even when the reference uses the superclass type.','Choose the printed output.','class Bridge { String style() { return "Stone"; } }
class RopeBridge extends Bridge {
    @Override String style() { return "Rope"; }
}
Bridge bridge = new RopeBridge();
System.out.println(bridge.style());','The created object is a RopeBridge.','CODE_CHOICE','Rope','Runtime dispatch invokes RopeBridge.style(), so Rope is printed.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quest_options VALUES ('many-forms',1,'Stone');
INSERT INTO quest_options VALUES ('many-forms',2,'Rope');
INSERT INTO quest_options VALUES ('many-forms',3,'Compilation fails');
INSERT INTO quests VALUES ('the-dynamic-scroll',5,1,'The Dynamic Scroll','ArrayList','Easy',150,'The royal librarian adds pages to a scroll that grows as needed.','ArrayList is a resizable ordered collection. add appends an entry; size returns its entry count.','What size is printed?','ArrayList<String> pages = new ArrayList<>();
pages.add("North");
pages.add("South");
pages.add("East");
System.out.println(pages.size());','Each add appends one entry.','INTEGER_TOKEN','3','Three appended entries give size 3.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quests VALUES ('the-key-keeper',5,2,'The Key Keeper','HashMap','Medium',175,'The Key Keeper updates the castle''s room ledger.','HashMap associates each unique key with one value. Putting an existing key replaces its value without adding another key.','What value is printed for the key tower?','HashMap<String, Integer> rooms = new HashMap<>();
rooms.put("tower", 4);
rooms.put("hall", 2);
rooms.put("tower", 7);
System.out.println(rooms.get("tower"));','The later put uses the same key.','INTEGER_TOKEN','7','The tower mapping is replaced with 7; there are still two keys.','Not quite. Revisit the lesson and trace the scenario carefully.');
INSERT INTO quests VALUES ('hall-of-uniques',5,3,'Hall of Uniques','Set','Medium',200,'Seal the royal archive by counting its distinct emblems.','A Set retains unique values. Adding the same value again does not increase size. HashSet does not promise iteration order.','What size is printed?','Set<String> emblems = new HashSet<>();
emblems.add("sun");
emblems.add("moon");
emblems.add("sun");
emblems.add("star");
System.out.println(emblems.size());','Count distinct emblems.','INTEGER_TOKEN','3','sun, moon and star are the three unique values.','Not quite. Revisit the lesson and trace the scenario carefully.');
