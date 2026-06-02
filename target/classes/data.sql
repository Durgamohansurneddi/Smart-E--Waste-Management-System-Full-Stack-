-- ─────────────────────────────────────────────────
--  SEED DATA  –  runs once on startup (INSERT IGNORE)
-- ─────────────────────────────────────────────────

-- Time Slots
INSERT IGNORE INTO time_slot_master (id, slot_label, start_time, end_time) VALUES
  (1, 'Morning   (9:00 AM – 12:00 PM)',  '09:00:00', '12:00:00'),
  (2, 'Afternoon (12:00 PM – 3:00 PM)',  '12:00:00', '15:00:00'),
  (3, 'Evening   (3:00 PM – 6:00 PM)',   '15:00:00', '18:00:00');

-- E-waste Item Master
INSERT IGNORE INTO ewaste_item_master (id, name) VALUES
  (1,  'Mobile Phone'),
  (2,  'Laptop / Notebook'),
  (3,  'Desktop Computer'),
  (4,  'Monitor / Display'),
  (5,  'Keyboard / Mouse'),
  (6,  'Printer / Scanner'),
  (7,  'Television'),
  (8,  'Refrigerator'),
  (9,  'Washing Machine'),
  (10, 'Air Conditioner'),
  (11, 'Battery / Power Bank'),
  (12, 'Cables & Chargers'),
  (13, 'Hard Disk / Pen Drive'),
  (14, 'Camera'),
  (15, 'Other Electronics');

-- Default Admin user  (password: Admin@123)
-- BCrypt hash of "Admin@123"
INSERT IGNORE INTO users (name, username, phone, email, password, role, status)
VALUES ('System Admin', 'admin', '9999999999', 'admin@ewaste.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lheO',
        'ADMIN', 'APPROVED');
