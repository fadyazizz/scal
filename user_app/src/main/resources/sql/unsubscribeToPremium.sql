CREATE or replace PROCEDURE  unsubscribeToPremium(
    param_ID INT
)
AS $$
BEGIN 
    UPDATE users
    SET isPremium=false
    WHERE id=param_ID ;
END;
$$ LANGUAGE plpgsql;