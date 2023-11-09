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

create table coords(
id identity primary key,
lat FLOAT,
lon FLOAT
);



