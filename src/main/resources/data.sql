INSERT INTO user_table
(name, email, password, role, city, state_code, country_code) VALUES
('user','user@csumb.edu','$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue','USER','Martinez','CA','US'),
('admin','admin@csumb.edu','$2a$10$8cjz47bjbR4Mn8GMg9IZx.vyjhLXR/SKKMSZ9.mP9vpMu0ssKi8GW','ADMIN','Monterey','CA','US');

INSERT INTO coords
(user_id, lat, lon) VALUES
(1, 36.600258, -121.89464),
(2, 38.013893, -122.133865);