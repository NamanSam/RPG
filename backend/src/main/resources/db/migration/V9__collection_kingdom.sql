INSERT INTO quests SELECT 'the-dynamic-scroll',5,1,'The Dynamic Scroll','ArrayList','Easy',150,'The royal librarian adds pages to a scroll that grows as needed.','ArrayList is a resizable ordered collection. add appends an entry; size returns its entry count.','What size is printed?','ArrayList<String> pages = new ArrayList<>();
pages.add("North");
pages.add("South");
pages.add("East");
System.out.println(pages.size());','Each add appends one entry.','INTEGER_TOKEN','3','Three appended entries give size 3.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='the-dynamic-scroll');
INSERT INTO quests SELECT 'the-key-keeper',5,2,'The Key Keeper','HashMap','Medium',175,'The Key Keeper updates the castle''s room ledger.','HashMap associates each unique key with one value. Putting an existing key replaces its value without adding another key.','What value is printed for the key tower?','HashMap<String, Integer> rooms = new HashMap<>();
rooms.put("tower", 4);
rooms.put("hall", 2);
rooms.put("tower", 7);
System.out.println(rooms.get("tower"));','The later put uses the same key.','INTEGER_TOKEN','7','The tower mapping is replaced with 7; there are still two keys.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='the-key-keeper');
INSERT INTO quests SELECT 'hall-of-uniques',5,3,'Hall of Uniques','Set','Medium',200,'Seal the royal archive by counting its distinct emblems.','A Set retains unique values. Adding the same value again does not increase size. HashSet does not promise iteration order.','What size is printed?','Set<String> emblems = new HashSet<>();
emblems.add("sun");
emblems.add("moon");
emblems.add("sun");
emblems.add("star");
System.out.println(emblems.size());','Count distinct emblems.','INTEGER_TOKEN','3','sun, moon and star are the three unique values.','Not quite. Revisit the lesson and trace the scenario carefully.' WHERE NOT EXISTS (SELECT 1 FROM quests WHERE id='hall-of-uniques');
INSERT INTO badges(id,name,world_id) SELECT 'collection-kingdom','Collection Kingdom Keeper',5 WHERE NOT EXISTS (SELECT 1 FROM badges WHERE world_id=5);
UPDATE worlds SET planned_quest_count=3 WHERE id=5;
UPDATE quests SET lesson='ArrayList is a resizable ordered list. Create one with new ArrayList<String>(). add(value) appends; get(index) reads a zero-based position; remove(index) removes that entry and shifts later entries left; size() counts entries. Import java.util.ArrayList to use it.', starter_code='ArrayList<String> pages = new ArrayList<>();
pages.add("North");
pages.add("South");
pages.add("East");
pages.add("West");
String first = pages.get(0); // North
pages.remove(1); // removes South; East moves to index 1
System.out.println(pages.size());', success_feedback='Three pages remain: North, East, West. get(0) reads North; remove(1) removes South and shifts the later entries. size() returns 3.' WHERE id='the-dynamic-scroll';
UPDATE quests SET lesson='HashMap stores key/value pairs with unique keys. Create one with new HashMap<String, Integer>(). put(key, value) adds or replaces a mapping. get(key) reads its value (or null if absent). containsKey(key) checks whether a key exists. Import java.util.HashMap.', starter_code='HashMap<String, Integer> rooms = new HashMap<>();
rooms.put("tower", 4);
rooms.put("hall", 2);
rooms.put("tower", 7); // replace the old value
boolean known = rooms.containsKey("tower"); // true
System.out.println(rooms.get("tower"));', success_feedback='put replaces the tower value with 7. containsKey("tower") is true, and get("tower") reads 7; the map still has two unique keys.' WHERE id='the-key-keeper';
UPDATE quests SET lesson='A Set stores unique values. HashSet is a Set implementation; create one with new HashSet<String>(). add(value) inserts a value only if absent, returning false for a duplicate. contains(value) checks membership. size() counts unique entries. HashSet has no guaranteed iteration order. Import java.util.Set and java.util.HashSet.', starter_code='Set<String> emblems = new HashSet<>();
emblems.add("sun");
emblems.add("moon");
boolean added = emblems.add("sun"); // false: duplicate
emblems.add("star");
boolean found = emblems.contains("moon"); // true
System.out.println(emblems.size());', success_feedback='The set contains sun, moon and star: 3 unique values. Adding sun twice does not create a second entry. contains("moon") is true.' WHERE id='hall-of-uniques';
