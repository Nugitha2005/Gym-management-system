-- Student ID: <your ID> | Table: role | Member A - User & Account Management
-- Seed data for the four system roles.
-- INSERT IGNORE prevents duplicate-key errors on every application restart.

INSERT IGNORE INTO role (role_name, description, access_start_time, access_end_time) VALUES
('STUDENT', 'Gym member with booking and equipment borrowing access', '08:00:00', '20:00:00'),
('STAFF',   'Front desk staff who issue and receive equipment',       '06:00:00', '22:00:00'),
('DOCTOR',  'University doctor who approves medical clearances',      '08:00:00', '17:00:00'),
('ADMIN',   'System administrator with full access',                  '00:00:00', '23:59:59');