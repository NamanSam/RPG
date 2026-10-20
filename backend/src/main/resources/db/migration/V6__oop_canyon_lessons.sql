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
