insert into user_table
(name, email,password, role,city,statecode,countrycode) values 
('user','user@csumb.edu','$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue','USER','San Francisco','CA','US'),
('user2','user2@csumb.edu','$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue','USER','Los Angeles','CA','US'),
('admin','admin@csumb.edu','$2a$10$8cjz47bjbR4Mn8GMg9IZx.vyjhLXR/SKKMSZ9.mP9vpMu0ssKi8GW','ADMIN','Monterey','CA','US');

INSERT INTO user_cities (user_id,city_name, country_code, latitude, longitude)
VALUES 
    (1,'Miami', 'US', 25.7742, -80.1936),
    (1,'San Diego', 'US',32.7174,-117.1628),
    (3,'Seattle', 'US', 47.6038, -122.3301),
    (3,'Toronto', 'CAN', 43.6535, -79.3839);
    
INSERT INTO default_cities (city_name, country_code, latitude, longitude)
VALUES 
    ('New York', 'US', 40.712728, -74.006015),
    ('Rome', 'IT', 41.893320, 12.482932),
    ('Tokyo', 'JP', 35.682839, 139.759455),
    ('Cairo', 'EG', 30.044388, 31.235726),
    ('London', 'GB', 51.507322, -0.127647),
    ('Moscow', 'RU', 55.750446, 37.617494),
    ('Paris', 'FR', 48.858890, 2.320041),
    ('Sydney', 'AU', -33.869844, 151.208285);
    
   
INSERT INTO coords (user_id, lat, lon) VALUES
  (1, 38.0139, -122.1339), 
  (2, 34.0537,-118.2428),
  (3, 36.6003, -121.8946); 
