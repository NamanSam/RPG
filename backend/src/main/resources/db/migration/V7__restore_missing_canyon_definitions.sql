INSERT INTO quests SELECT 'blueprint-maker',4,1,'Blueprint Maker','Classes and Objects','Easy',125,'The Architect needs a new Bridge object from an existing class.','A class defines a blueprint. The new keyword creates an instance by invoking its constructor.','Choose the statement that creates a Bridge object named bridge.','class Bridge { }
// Create an instance below.','Use new followed by the constructor.','CODE_CHOICE','Bridge bridge = new Bridge();','Bridge bridge = new Bridge(); declares a reference and creates an instance.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='blueprint-maker');
INSERT INTO quest_options SELECT 'blueprint-maker',1,'Bridge bridge = new Bridge();' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='blueprint-maker' AND sort_order=1);
INSERT INTO quest_options SELECT 'blueprint-maker',2,'Bridge bridge = Bridge;' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='blueprint-maker' AND sort_order=2);
INSERT INTO quest_options SELECT 'blueprint-maker',3,'new class Bridge;' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='blueprint-maker' AND sort_order=3);
INSERT INTO quests SELECT 'the-hidden-vault',4,2,'The Hidden Vault','Encapsulation','Medium',150,'Protect the vault''s coin balance so callers must use its controlled deposit method.','Encapsulation hides internal state. A private field is accessed through behavior provided by the class.','Which access modifier keeps coins accessible only inside Vault?','class Vault {
    ____ int coins;
    public void deposit(int amount) {
        if (amount > 0) coins += amount;
    }
}','Hide the field from callers.','CODE_CHOICE','private','private restricts the field to the declaring class, preserving the deposit rule.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='the-hidden-vault');
INSERT INTO quest_options SELECT 'the-hidden-vault',1,'public' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='the-hidden-vault' AND sort_order=1);
INSERT INTO quest_options SELECT 'the-hidden-vault',2,'private' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='the-hidden-vault' AND sort_order=2);
INSERT INTO quest_options SELECT 'the-hidden-vault',3,'protected' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='the-hidden-vault' AND sort_order=3);
INSERT INTO quests SELECT 'bloodline-of-classes',4,3,'Bloodline of Classes','Inheritance','Medium',175,'An apprentice blueprint should inherit the tools of the Architect.','A Java class uses extends to inherit from one superclass. Inherited accessible methods can be reused.','Fill the missing inheritance keyword.','class Architect { public void build() { } }
class Apprentice ____ Architect { }','The parent is a class, not an interface.','CODE_CHOICE','extends','extends establishes the superclass relationship.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='bloodline-of-classes');
INSERT INTO quest_options SELECT 'bloodline-of-classes',1,'implements' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='bloodline-of-classes' AND sort_order=1);
INSERT INTO quest_options SELECT 'bloodline-of-classes',2,'extends' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='bloodline-of-classes' AND sort_order=2);
INSERT INTO quest_options SELECT 'bloodline-of-classes',3,'inherits' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='bloodline-of-classes' AND sort_order=3);
INSERT INTO quests SELECT 'many-forms',4,4,'Many Forms','Polymorphism','Hard',200,'Two bridge designs share a base type, but the Architect must predict which design speaks.','Overridden instance methods dispatch according to the runtime object, even when the reference uses the superclass type.','Choose the printed output.','class Bridge { String style() { return "Stone"; } }
class RopeBridge extends Bridge {
    @Override String style() { return "Rope"; }
}
Bridge bridge = new RopeBridge();
System.out.println(bridge.style());','The created object is a RopeBridge.','CODE_CHOICE','Rope','Runtime dispatch invokes RopeBridge.style(), so Rope is printed.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='many-forms');
INSERT INTO quest_options SELECT 'many-forms',1,'Stone' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='many-forms' AND sort_order=1);
INSERT INTO quest_options SELECT 'many-forms',2,'Rope' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='many-forms' AND sort_order=2);
INSERT INTO quest_options SELECT 'many-forms',3,'Compilation fails' WHERE NOT EXISTS (SELECT 1 FROM quest_options WHERE quest_id='many-forms' AND sort_order=3);
UPDATE quests SET lesson='A class is a blueprint that defines fields (state) and methods (behavior). Each object has its own fields. The new keyword calls a constructor to create an object; the dot operator accesses its fields and methods.', starter_code='class Bridge {
    int length = 12;
    int span() { return length; }
}
// After creating bridge, bridge.length is 12
// and bridge.span() returns 12.
// Choose the object creation statement.' WHERE id='blueprint-maker';
UPDATE quests SET lesson='Encapsulation hides data behind controlled access. A private field cannot be read or changed directly by callers. A getter reads it; a setter validates a proposed value before updating it. This keeps the object valid.', starter_code='class Vault {
    ____ int coins;
    public int getCoins() { return coins; }
    public void setCoins(int amount) {
        if (amount >= 0) coins = amount;
    }
}
// vault.setCoins(10); changes the balance safely.
// vault.getCoins(); reads it without exposing the field.', success_feedback='private hides coins. Callers use getCoins() to read and setCoins() to change it; the setter rejects negative balances.' WHERE id='the-hidden-vault';
UPDATE quests SET lesson='A parent class provides shared state and behavior. A child class uses extends to inherit accessible fields and methods. It can reuse them without copying their implementation. Private parent fields require controlled access through parent methods.', starter_code='class Architect {
    public int tools = 3;
    public void build() { System.out.println("Built"); }
}
class Apprentice ____ Architect { }
// Apprentice a = new Apprentice();
// a.tools is inherited; a.build() reuses the parent method.', success_feedback='extends makes Apprentice a child of Architect. It inherits tools and reuses build() without copying either definition.' WHERE id='bloodline-of-classes';
