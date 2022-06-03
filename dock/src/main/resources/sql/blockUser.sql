CREATE or replace PROCEDURE  blockUser(
      blocking_id INT,
      blocked_id INT
  )
AS $$
BEGIN
IF (blocking_id <> blocked_id) THEN
    INSERT INTO blocked(blocking_id, blocked_id)
    VALUES(blocking_id, blocked_id);
END IF;
END
$$ LANGUAGE plpgsql;