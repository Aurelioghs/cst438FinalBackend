create table user_table (
	user_id identity primary key,  
	name varchar(25) unique, 
	email varchar(25) unique,
	password varchar(100), 
	role varchar(25),
	city varchar(32),
	statecode varchar(2),
	countrycode varchar(2)
);

CREATE TABLE coords (
    coord_id int NOT NULL AUTO_INCREMENT,
    user_id int NOT NULL,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL,
    PRIMARY KEY (coord_id),
    FOREIGN KEY (user_id) REFERENCES user_table (user_id) ON DELETE CASCADE
);

CREATE TABLE default_cities (
 	city_id INT PRIMARY KEY AUTO_INCREMENT,
    city_name VARCHAR(255),
    country_code CHAR(2),
    latitude FLOAT,
    longitude FLOAT
);

CREATE TABLE user_cities (
	city_id int NOT NULL AUTO_INCREMENT,
	user_id int NOT NULL,
	city_name varchar(255) NOT NULL,
	country_code varchar(2) NOT NULL,
	latitude FLOAT NOT NULL,
	longitude FLOAT NOT NULL,
	PRIMARY KEY (city_id),
	  FOREIGN KEY (user_id) REFERENCES user_table (user_id) ON DELETE CASCADE
);