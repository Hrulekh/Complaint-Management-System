INSERT IGNORE INTO categories (name, description, active) VALUES
('Billing', 'Issues related to billing and invoices', true),
('Service Quality', 'Complaints about service quality and standards', true),
('Facilities', 'Issues with facilities and infrastructure', true),
('Technical Issue', 'Technical problems and system errors', true),
('Staff Conduct', 'Concerns about staff behavior and professionalism', true),
('Other', 'General complaints not fitting other categories', true);

INSERT IGNORE INTO priorities (name, level, sla_hours, active) VALUES
('Low', 1, 120, true),
('Medium', 2, 72, true),
('High', 3, 24, true),
('Critical', 4, 4, true);

INSERT IGNORE INTO users (full_name, email, password_hash, phone, role, active) VALUES
('Admin User', 'admin@cms.local', 'admin123', '9876543210', 'ADMIN', true);
