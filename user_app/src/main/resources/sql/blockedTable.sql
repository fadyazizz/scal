CREATE TABLE IF NOT EXISTS blocked (
  blocking_id INT NOT NULL,
  blocked_id INT NOT NULL,
  FOREIGN KEY (blocking_id) REFERENCES users(id),
  FOREIGN KEY (blocked_id) REFERENCES users(id),
  CONSTRAINT PK_blocked PRIMARY KEY (blocking_id,blocked_id)
);