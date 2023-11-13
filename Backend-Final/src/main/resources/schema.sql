create table user_table (
	id identity primary key,  
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
    FOREIGN KEY (user_id) REFERENCES user_table (id) ON DELETE CASCADE
);

CREATE TABLE cities (
 	city_id INT PRIMARY KEY AUTO_INCREMENT,
    city_name VARCHAR(255),
    country_code CHAR(2),
    latitude FLOAT,
    longitude FLOAT
);