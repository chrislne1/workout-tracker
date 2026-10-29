-- Ensure the core muscle group exists for upgrades from the original taxonomy.
INSERT INTO muscle_groups (id, name, optimal_sets_per_week)
SELECT 6, 'Core', 10
WHERE NOT EXISTS (SELECT 1 FROM muscle_groups WHERE id = 6);

-- Rename/reclassify the original seeded movements and add the expanded movement list.
UPDATE exercises SET base_name = CASE id
  WHEN 1 THEN 'Flat Press'
  WHEN 2 THEN 'Chest Fly'
  WHEN 3 THEN 'Incline Press'
  WHEN 4 THEN 'Decline Press'
  WHEN 5 THEN 'Upper Back Row'
  WHEN 6 THEN 'Lat Pulldown'
  WHEN 7 THEN 'Seated Row'
  WHEN 8 THEN 'Bent Over Row'
  WHEN 9 THEN 'Squat'
  WHEN 10 THEN 'Lunge'
  WHEN 11 THEN 'Leg Curl'
  WHEN 12 THEN 'Leg Extension'
  WHEN 13 THEN 'Calf Raise'
  WHEN 14 THEN 'Hip Abduction'
  WHEN 15 THEN 'Hip Adduction'
  WHEN 16 THEN 'Shoulder Press'
  WHEN 17 THEN 'Lateral Raise'
  WHEN 18 THEN 'Rear Delt Fly'
  WHEN 19 THEN 'Bicep Curl'
  WHEN 20 THEN 'Hammer Curl'
  WHEN 21 THEN 'Reverse Curl'
  WHEN 22 THEN 'Tricep Extension'
  WHEN 23 THEN 'Tricep Pushdown'
  WHEN 24 THEN 'Ab Machine'
END,
muscle_group_id = CASE
  WHEN id BETWEEN 1 AND 4 THEN 1
  WHEN id BETWEEN 5 AND 8 THEN 2
  WHEN id BETWEEN 9 AND 15 THEN 3
  WHEN id BETWEEN 16 AND 18 THEN 4
  WHEN id BETWEEN 19 AND 23 THEN 5
  WHEN id = 24 THEN 6
END
WHERE id BETWEEN 1 AND 24;

INSERT INTO exercises (id, base_name, muscle_group_id)
SELECT movement.id, movement.base_name, movement.muscle_group_id
FROM (VALUES
  (6,'Lat Pulldown',2),(7,'Seated Row',2),(8,'Bent Over Row',2),
  (9,'Squat',3),(10,'Lunge',3),(11,'Leg Curl',3),(12,'Leg Extension',3),
  (13,'Calf Raise',3),(14,'Hip Abduction',3),(15,'Hip Adduction',3),
  (16,'Shoulder Press',4),(17,'Lateral Raise',4),(18,'Rear Delt Fly',4),
  (19,'Bicep Curl',5),(20,'Hammer Curl',5),(21,'Reverse Curl',5),
  (22,'Tricep Extension',5),(23,'Tricep Pushdown',5),(24,'Ab Machine',6)
) AS movement(id, base_name, muscle_group_id)
WHERE NOT EXISTS (SELECT 1 FROM exercises e WHERE e.id = movement.id);

-- Keep the original variant IDs so historical sets continue to resolve.
UPDATE exercise_variants SET exercise_id = 3, variant_name = 'Dumbbell Incline Press', equipment_type = 'DUMBBELL' WHERE id = 1;
UPDATE exercise_variants SET exercise_id = 3, variant_name = 'Barbell Incline Press', equipment_type = 'BARBELL' WHERE id = 2;
UPDATE exercise_variants SET exercise_id = 6, variant_name = 'Machine Lat Pulldown', equipment_type = 'MACHINE' WHERE id = 3;
UPDATE exercise_variants SET exercise_id = 17, variant_name = 'Dumbbell Lateral Raise', equipment_type = 'DUMBBELL' WHERE id = 4;
UPDATE exercise_variants SET exercise_id = 9, variant_name = 'Machine Squat', equipment_type = 'MACHINE' WHERE id = 5;

INSERT INTO exercise_variants (id, exercise_id, variant_name, equipment_type)
SELECT variant.id, variant.exercise_id, variant.variant_name, variant.equipment_type
FROM (VALUES
  (6,1,'Barbell Flat Press','BARBELL'),
  (7,1,'Dumbbell Flat Press','DUMBBELL'),
  (8,1,'Machine Flat Press','MACHINE'),
  (9,2,'Machine Chest Fly','MACHINE'),
  (10,2,'Cable Chest Fly','CABLE'),
  (11,2,'Dumbbell Chest Fly','DUMBBELL'),
  (12,3,'Machine Incline Press','MACHINE'),
  (13,4,'Barbell Decline Press','BARBELL'),
  (14,4,'Machine Decline Press','MACHINE'),
  (15,4,'Dumbbell Decline Press','DUMBBELL'),
  (16,5,'Machine Upper Back Row','MACHINE'),
  (17,7,'Machine Seated Row','MACHINE'),
  (18,8,'Machine Bent Over Row','MACHINE'),
  (19,9,'Barbell Squat','BARBELL'),
  (20,10,'Barbell Lunge','BARBELL'),
  (21,10,'Dumbbell Lunge','DUMBBELL'),
  (22,10,'Machine Lunge','MACHINE'),
  (23,11,'Machine Leg Curl','MACHINE'),
  (24,12,'Machine Leg Extension','MACHINE'),
  (25,13,'Barbell Calf Raise','BARBELL'),
  (26,13,'Machine Calf Raise','MACHINE'),
  (27,14,'Machine Hip Abduction','MACHINE'),
  (28,15,'Machine Hip Adduction','MACHINE'),
  (29,16,'Dumbbell Shoulder Press','DUMBBELL'),
  (30,16,'Barbell Shoulder Press','BARBELL'),
  (31,16,'Machine Shoulder Press','MACHINE'),
  (32,17,'Machine Lateral Raise','MACHINE'),
  (33,17,'Cable Lateral Raise','CABLE'),
  (34,18,'Machine Rear Delt Fly','MACHINE'),
  (35,19,'Dumbbell Bicep Curl','DUMBBELL'),
  (36,19,'Cable Bicep Curl','CABLE'),
  (37,19,'Barbell Bicep Curl','BARBELL'),
  (38,19,'Machine Bicep Curl','MACHINE'),
  (39,20,'Cable Hammer Curl','CABLE'),
  (40,20,'Machine Hammer Curl','MACHINE'),
  (41,20,'Dumbbell Hammer Curl','DUMBBELL'),
  (42,21,'Cable Reverse Curl','CABLE'),
  (43,21,'Machine Reverse Curl','MACHINE'),
  (44,21,'Dumbbell Reverse Curl','DUMBBELL'),
  (45,22,'Dumbbell Tricep Extension','DUMBBELL'),
  (46,22,'Machine Tricep Extension','MACHINE'),
  (47,22,'Cable Tricep Extension','CABLE'),
  (48,23,'Machine Tricep Pushdown','MACHINE'),
  (49,23,'Cable Tricep Pushdown','CABLE'),
  (50,24,'Ab Machine','MACHINE')
) AS variant(id, exercise_id, variant_name, equipment_type)
WHERE NOT EXISTS (SELECT 1 FROM exercise_variants v WHERE v.id = variant.id);

INSERT INTO exercise_muscles (variant_id, muscle_id, weight_percentage)
SELECT v.id, e.muscle_group_id, 1.00
FROM exercise_variants v
JOIN exercises e ON e.id = v.exercise_id
WHERE NOT EXISTS (
  SELECT 1 FROM exercise_muscles em WHERE em.variant_id = v.id
);
