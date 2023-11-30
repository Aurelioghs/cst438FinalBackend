INSERT INTO user_table
(name, email, password, role, city, state_code, country_code) VALUES
('user','user@csumb.edu','$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue','USER','Martinez','CA','US'),
('admin','admin@csumb.edu','$2a$10$8cjz47bjbR4Mn8GMg9IZx.vyjhLXR/SKKMSZ9.mP9vpMu0ssKi8GW','ADMIN','Monterey','CA','US');

INSERT INTO coords
(user_id, lat, lon) VALUES
(1, 36.600258, -121.89464),
(2, 38.013893, -122.133865);

INSERT INTO default_cities (city, country_code, latitude, longitude) VALUES 
('New York', 'US', 40.712728, -74.006015),
('Rome', 'IT', 41.893320, 12.482932),
('Tokyo', 'JP', 35.682839, 139.759455),
('Cairo', 'EG', 30.044388, 31.235726),
('London', 'GB', 51.507322, -0.127647),
('Moscow', 'RU', 55.750446, 37.617494),
('Paris', 'FR', 48.858890, 2.320041),
('Sydney', 'AU', -33.869844, 151.208285);