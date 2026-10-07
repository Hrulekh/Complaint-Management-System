-- Pre-seed staff users so admin can assign complaints
INSERT IGNORE INTO users (full_name, email, password_hash, phone, role, active, created_at) VALUES
('Staff Member 1', 'staff1@cms.local', 'staff123', '9876543211', 'STAFF', true, NOW()),
('Staff Member 2', 'staff2@cms.local', 'staff123', '9876543212', 'STAFF', true, NOW()),
('Staff Member 3', 'staff3@cms.local', 'staff123', '9876543213', 'STAFF', true, NOW());

-- Pre-seed a complainant user for testing
INSERT IGNORE INTO users (full_name, email, password_hash, phone, role, active, created_at) VALUES
('Test Complainant', 'user@cms.local', 'user123', '9876543220', 'COMPLAINANT', true, NOW());
