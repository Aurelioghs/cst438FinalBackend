CREATE TABLE user_table (
	user_id int NOT NULL AUTO_INCREMENT, 
	name varchar(255) NOT NULL,
  	email varchar(255) NOT NULL UNIQUE,
	password varchar(100) NOT NULL, 
  	role varchar(25) NOT NULL,
	city varchar(32) NOT NULL,
	state_code varchar(2) NOT NULL,
	country_code varchar(2) NOT NULL,
	PRIMARY KEY (user_id)
);

CREATE TABLE coords(
	coord_id int NOT NULL AUTO_INCREMENT,
	user_id int NOT NULL,
	lat FLOAT NOT NULL,
	lon FLOAT NOT NULL,
	PRIMARY KEY (coord_id),
  	FOREIGN KEY (user_id) REFERENCES user_table (user_id) on delete cascade
);

CREATE TABLE default_cities (
	city_id int NOT NULL AUTO_INCREMENT,
	city varchar(255) NOT NULL,
	country_code varchar(2) NOT NULL,
	latitude FLOAT NOT NULL,
	longitude FLOAT NOT NULL,
	PRIMARY KEY (city_id)
);

CREATE TABLE user_cities (
	city_id int NOT NULL AUTO_INCREMENT,
	user_id int NOT NULL,
	city varchar(255) NOT NULL,
	country_code varchar(2) NOT NULL,
	latitude FLOAT NOT NULL,
	longitude FLOAT NOT NULL,
	PRIMARY KEY (city_id),
	FOREIGN KEY (user_id) REFERENCES user_table (user_id) on delete cascade
);