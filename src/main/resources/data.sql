-- Student ID: IT25100964 | Table: role | Member A - User & Account Management
-- Seed data for the five system roles.
-- INSERT IGNORE prevents duplicate-key errors on every application restart.
-- NOTE: INSERT IGNORE never changes rows that already exist. Changing a name or time here
--       does NOT update an existing database; run an UPDATE on the database for that.
--
-- Access hours: students may use the gym until 17:30; after 17:30 it is university staff only.

INSERT IGNORE INTO role (role_name, description, access_start_time, access_end_time) VALUES
('STUDENT',          'Student gym member with booking and equipment borrowing access',     '08:00:00', '17:30:00'),
('UNIVERSITY_STAFF', 'University staff gym member; can also use the gym after 17:30',      '08:00:00', '22:00:00'),
('TRAINER',          'Gym trainer who runs the front desk and issues equipment',           '06:00:00', '22:00:00'),
('DOCTOR',           'University doctor who approves medical clearances',                  '08:00:00', '17:00:00'),
('ADMIN',            'System administrator with full access',                              '00:00:00', '23:59:59');
