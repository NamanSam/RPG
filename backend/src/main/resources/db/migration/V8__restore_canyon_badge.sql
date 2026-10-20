INSERT INTO badges (id,name,world_id)
SELECT 'oop-canyon','OOP Canyon Architect',4
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE world_id=4);
UPDATE worlds SET planned_quest_count=4 WHERE id=4;
