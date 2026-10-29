CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE muscle_groups (
  id BIGINT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  optimal_sets_per_week INT NOT NULL CHECK (optimal_sets_per_week > 0)
);
CREATE TABLE exercises (
  id BIGINT PRIMARY KEY,
  base_name VARCHAR(120) NOT NULL,
  muscle_group_id BIGINT NOT NULL REFERENCES muscle_groups(id)
);
CREATE TABLE exercise_variants (
  id BIGINT PRIMARY KEY,
  exercise_id BIGINT NOT NULL REFERENCES exercises(id),
  variant_name VARCHAR(160) NOT NULL,
  equipment_type VARCHAR(20) NOT NULL CHECK (equipment_type IN ('BARBELL','DUMBBELL','CABLE','MACHINE','BODYWEIGHT'))
);
CREATE TABLE exercise_muscles (
  variant_id BIGINT NOT NULL REFERENCES exercise_variants(id),
  muscle_id BIGINT NOT NULL REFERENCES muscle_groups(id),
  weight_percentage NUMERIC(4,2) NOT NULL CHECK (weight_percentage > 0 AND weight_percentage <= 1),
  PRIMARY KEY (variant_id, muscle_id)
);
CREATE TABLE workout_sessions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  session_date DATE NOT NULL,
  split_type VARCHAR(10) NOT NULL CHECK (split_type IN ('UPPER','LOWER','PUSH','PULL','LEGS')),
  notes VARCHAR(1000),
  deleted_at TIMESTAMP
);
CREATE TABLE workout_sets (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES workout_sessions(id),
  variant_id BIGINT NOT NULL REFERENCES exercise_variants(id),
  set_number INT NOT NULL CHECK (set_number > 0),
  weight NUMERIC(7,2) NOT NULL CHECK (weight >= 0),
  reps INT NOT NULL CHECK (reps >= 0),
  set_type VARCHAR(10) NOT NULL CHECK (set_type IN ('WARMUP','WORKING','DROPSET','FAILURE')),
  is_bodyweight BOOLEAN NOT NULL DEFAULT FALSE,
  added_weight NUMERIC(7,2) NOT NULL DEFAULT 0
);
CREATE TABLE session_feedback (
  session_id UUID PRIMARY KEY REFERENCES workout_sessions(id),
  regression_reason VARCHAR(30) NOT NULL CHECK (regression_reason IN ('PURPOSEFUL_DELOAD','FATIGUE_POOR_SLEEP','INJURY_DISCOMFORT','EQUIPMENT_CHANGE'))
);
CREATE INDEX idx_sessions_user_date ON workout_sessions(user_id, session_date);
CREATE INDEX idx_sets_session ON workout_sets(session_id);
CREATE INDEX idx_sets_variant ON workout_sets(variant_id);
