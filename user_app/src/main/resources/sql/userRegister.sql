CREATE or replace PROCEDURE userRegister(
	username VARCHAR,
	email VARCHAR,
	password VARCHAR,
	first_name VARCHAR(20),
	last_name VARCHAR(20),
	isArtist BOOLEAN
)
AS $$
BEGIN 
INSERT INTO users(username, email,password, first_name, last_name, isArtist)
    VALUES(username, email,password, first_name, last_name, isArtist);
END;
$$ LANGUAGE plpgsql;