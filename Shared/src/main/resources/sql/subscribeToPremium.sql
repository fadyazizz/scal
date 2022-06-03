CREATE  or replace PROCEDURE subscribeToPremium(
    param_ID INT
)
AS $$
BEGIN 
    UPDATE users
    SET isPremium=true
    WHERE id=param_ID ;
END;  $$ LANGUAGE plpgsql;