-- Tier 1: muscle groups
INSERT INTO muscle_groups (id, name, optimal_sets_per_week) VALUES
  (1,'Chest',12),(2,'Back',14),(3,'Legs',12),(4,'Shoulders',10),(5,'Arms',12),(6,'Core',10);

-- Tier 2: base movements
INSERT INTO exercises (id, base_name, muscle_group_id) VALUES
  --Chest=1
  (1,'Flat Press',1),(2, 'Chest Fly',1),(3,'Incline Press',1),(4,'Decline Press',1),
  --Back=2
  (5,'Upper Back Row',2),(6,'Lat Pulldown',2),(7,'Seated Row',2),(8,'Bent Over Row',2),
  -- Leg=3
  (9,'Squat',3),(10,'Lunge',3),(11,'Leg Curl',3),(12,'Leg Extension',3), (13,'Calf Raise',3), (14, 'Hip Abduction',3), (15, 'Hip Adduction',3),
  --Shoulder=4
  (16, 'Shoulder Press',4),(17,'Lateral Raise',4), (18,'Rear Delt Fly',4),
  --Arms=5
  (19,'Bicep Curl',5),(20,'Hammer Curl',5),(21, 'Reverse Curl',5),(22,'Tricep Extension',5),(23,'Tricep Pushdown',5),
  --Core=6
  (24, 'Ab Machine',6);

-- Tier 3: equipment-specific variants. IDs 1-5 remain stable for existing logs.
INSERT INTO exercise_variants (id, exercise_id, variant_name, equipment_type) VALUES
  (1,1,'Incline Dumbbell Press','DUMBBELL'),
  (2,1,'Incline Barbell Press','BARBELL'),
  (3,4,'Cable Lat Pulldown','CABLE'),
  (4,5,'Dumbbell Lateral Raise','DUMBBELL'),
  (5,3,'Hack Squat','MACHINE');
-- Fractional muscle attribution (primary 0.70, secondary 0.15 ...)
INSERT INTO exercise_muscles (variant_id, muscle_id, weight_percentage) VALUES
  (1,1,0.70),(1,4,0.15),(1,5,0.15),
  (2,1,0.70),(2,4,0.15),(2,5,0.15),
  (3,2,0.75),(3,5,0.25),
  (4,4,1.00),
  (5,3,1.00);
-- Single demo user (authentication is outside the scope of this spec)
INSERT INTO users (id, email) VALUES ('00000000-0000-0000-0000-000000000001','demo@overload.app');
