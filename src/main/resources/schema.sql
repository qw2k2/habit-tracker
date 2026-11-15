CREATE TABLE IF NOT EXISTS habits (
  id UUID PRIMARY KEY,
  name VARCHAR(80) NOT NULL CHECK (length(trim(name)) > 0),
  category VARCHAR(40) NOT NULL CHECK (length(trim(category)) > 0),
  weekly_target INTEGER NOT NULL CHECK (weekly_target BETWEEN 1 AND 7),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS habits_name_category ON habits(lower(name), lower(category));
CREATE TABLE IF NOT EXISTS habit_tags (
  habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
  position INTEGER NOT NULL CHECK (position BETWEEN 0 AND 2),
  tag VARCHAR(20) NOT NULL CHECK (length(trim(tag)) > 0),
  PRIMARY KEY(habit_id, position),
  UNIQUE(habit_id, tag)
);
CREATE TABLE IF NOT EXISTS habit_completions (
  habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
  completed_on DATE NOT NULL,
  PRIMARY KEY(habit_id, completed_on)
);
