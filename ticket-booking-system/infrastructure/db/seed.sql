-- ============================================
-- AY Ticket Booking System - Complete Seed Data
-- ============================================

-- 1. Users
INSERT INTO users (id, name, email, phone) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Ava Sharma',   'ava@ticketing.com',   '9876543210'),
    ('a0000000-0000-0000-0000-000000000002', 'Rahul Verma',  'rahul@ticketing.com', '9876543211'),
    ('a0000000-0000-0000-0000-000000000003', 'Priya Singh',  'priya@ticketing.com', '9876543212')
ON CONFLICT DO NOTHING;

-- 2. Events (3 events with different dates, venues, seat counts)
INSERT INTO events (id, name, venue, event_date, total_seats) VALUES
    ('e0000000-0000-0000-0000-000000000001',
     'Arijit Singh Live Concert',
     'MMRDA Grounds, Mumbai',
     NOW() + INTERVAL '15 days',
     100),
    ('e0000000-0000-0000-0000-000000000002',
     'Ed Sheeran Mathematics Tour',
     'Jawaharlal Nehru Stadium, Delhi',
     NOW() + INTERVAL '45 days',
     150),
    ('e0000000-0000-0000-0000-000000000003',
     'IPL 2026 Final - CSK vs MI',
     'MA Chidambaram Stadium, Chennai',
     NOW() + INTERVAL '60 days',
     200)
ON CONFLICT DO NOTHING;

-- 3. Seats for Event 1: Arijit Singh (100 seats, 10 rows x 10 cols)
-- VIP: Rows A-B (20 seats) @ ₹5,000
-- Premium: Rows C-E (30 seats) @ ₹3,000
-- General: Rows F-J (50 seats) @ ₹1,500
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000001',
    row_label || col_num,
    row_label,
    section,
    price,
    'AVAILABLE',
    0
FROM (
    SELECT 
        CASE 
            WHEN row_num <= 2 THEN 'VIP'
            WHEN row_num <= 5 THEN 'PREMIUM'
            ELSE 'GENERAL'
        END as section,
        CASE 
            WHEN row_num <= 2 THEN 5000.00
            WHEN row_num <= 5 THEN 3000.00
            ELSE 1500.00
        END as price,
        chr(64 + row_num) as row_label,
        col_num
    FROM generate_series(1, 10) as row_num,
         generate_series(1, 10) as col_num
) AS seat_data
ON CONFLICT DO NOTHING;

-- 4. Seats for Event 2: Ed Sheeran (150 seats, 15 rows x 10 cols)
-- VIP: Rows A-C (30 seats) @ ₹8,000
-- Premium: Rows D-G (40 seats) @ ₹5,000
-- General: Rows H-O (80 seats) @ ₹2,500
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000002',
    row_label || col_num,
    row_label,
    section,
    price,
    'AVAILABLE',
    0
FROM (
    SELECT 
        CASE 
            WHEN row_num <= 3 THEN 'VIP'
            WHEN row_num <= 7 THEN 'PREMIUM'
            ELSE 'GENERAL'
        END as section,
        CASE 
            WHEN row_num <= 3 THEN 8000.00
            WHEN row_num <= 7 THEN 5000.00
            ELSE 2500.00
        END as price,
        chr(64 + row_num) as row_label,
        col_num
    FROM generate_series(1, 15) as row_num,
         generate_series(1, 10) as col_num
) AS seat_data
ON CONFLICT DO NOTHING;

-- 5. Seats for Event 3: IPL Final (200 seats, 20 rows x 10 cols)
-- VIP: Rows A-D (40 seats) @ ₹10,000
-- Premium: Rows E-I (50 seats) @ ₹6,000
-- General: Rows J-T (110 seats) @ ₹3,000
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000003',
    row_label || col_num,
    row_label,
    section,
    price,
    'AVAILABLE',
    0
FROM (
    SELECT 
        CASE 
            WHEN row_num <= 4 THEN 'VIP'
            WHEN row_num <= 9 THEN 'PREMIUM'
            ELSE 'GENERAL'
        END as section,
        CASE 
            WHEN row_num <= 4 THEN 10000.00
            WHEN row_num <= 9 THEN 6000.00
            ELSE 3000.00
        END as price,
        chr(64 + row_num) as row_label,
        col_num
    FROM generate_series(1, 20) as row_num,
         generate_series(1, 10) as col_num
) AS seat_data
ON CONFLICT DO NOTHING;