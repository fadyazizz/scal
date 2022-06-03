CREATE or replace PROCEDURE  edit_Account(
UserId INTEGER,
newPassword VARCHAR,
newPhoto VARCHAR)
language plpgsql
as $$
BEGIN
UPDATE users SET

password= CASE WHEN newPassword IS NOT NULL THEN newPassword ELSE password END,

profile_photo=CASE WHEN newPhoto IS NOT NULL THEN newPhoto ELSE profile_photo END
WHERE id=UserId;
END; $$