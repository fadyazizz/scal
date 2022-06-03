CREATE or replace  PROCEDURE  deleteAccount(
    param_ID INT
)
AS $$
BEGIN 
    DELETE FROM users
    WHERE id=param_ID ;
END; $$ LANGUAGE plpgsql;