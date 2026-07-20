-- ============================================
-- AY Ticket Booking System - Complete Seed Data
-- ============================================

-- Sample users
INSERT INTO users (id, name, email, phone) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Ava Sharma',   'ava@ticketing.com',   '9876543210'),
    ('a0000000-0000-0000-0000-000000000002', 'Rahul Verma',  'rahul@ticketing.com', '9876543211'),
    ('a0000000-0000-0000-0000-000000000003', 'Priya Singh',  'priya@ticketing.com', '9876543212')
ON CONFLICT DO NOTHING;

-- ============================================
-- EVENTS (3 events with different capacities)
-- ============================================

-- Event 1: Arijit Singh Live Concert (100 seats)
INSERT INTO events (id, name, venue, event_date, total_seats, image_url) VALUES
    ('e0000000-0000-0000-0000-000000000001',
     'Arijit Singh Live Concert',
     'MMRDA Grounds, Mumbai',
     NOW() + INTERVAL '30 days',
     100,
     'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800')
ON CONFLICT DO NOTHING;

-- Event 2: Ed Sheeran Mathematics Tour (150 seats)
INSERT INTO events (id, name, venue, event_date, total_seats, image_url) VALUES
    ('e0000000-0000-0000-0000-000000000002',
     'Ed Sheeran Mathematics Tour',
     'Jawaharlal Nehru Stadium, Delhi',
     NOW() + INTERVAL '45 days',
     150,
     'https://images.unsplash.com/photo-1459749411177-0473ef716175?w=800')
ON CONFLICT DO NOTHING;

-- Event 3: IPL Final 2026 (200 seats)
INSERT INTO events (id, name, venue, event_date, total_seats, image_url) VALUES
    ('e0000000-0000-0000-0000-000000000003',
     'IPL Final 2026',
     'MA Chidambaram Stadium, Chennai',
     NOW() + INTERVAL '15 days',
     200,
     'https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=800')
ON CONFLICT DO NOTHING;

-- ============================================
-- SEATS - Event 1: Arijit Singh (100 seats)
-- Row A: VIP (seats 1-20, ₹2,000)
-- Row B: PREMIUM (seats 21-50, ₹1,000)
-- Row C: GENERAL (seats 51-100, ₹500)
-- ============================================
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000001',
    'A' || gs,
    CASE
        WHEN gs <= 20 THEN 'A'
        WHEN gs <= 50 THEN 'B'
        ELSE 'C'
    END,
    CASE
        WHEN gs <= 20 THEN 'VIP'
        WHEN gs <= 50 THEN 'PREMIUM'
        ELSE 'GENERAL'
    END,
    CASE
        WHEN gs <= 20 THEN 2000.00
        WHEN gs <= 50 THEN 1000.00
        ELSE 500.00
    END,
    'AVAILABLE',
    0
FROM generate_series(1, 100) AS gs
ON CONFLICT DO NOTHING;

-- ============================================
-- SEATS - Event 2: Ed Sheeran (150 seats)
-- Row A: VIP (seats 1-30, ₹3,000)
-- Row B: PREMIUM (seats 31-80, ₹1,500)
-- Row C: GENERAL (seats 81-150, ₹750)
-- ============================================
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000002',
    'B' || gs,
    CASE
        WHEN gs <= 30 THEN 'A'
        WHEN gs <= 80 THEN 'B'
        ELSE 'C'
    END,
    CASE
        WHEN gs <= 30 THEN 'VIP'
        WHEN gs <= 80 THEN 'PREMIUM'
        ELSE 'GENERAL'
    END,
    CASE
        WHEN gs <= 30 THEN 3000.00
        WHEN gs <= 80 THEN 1500.00
        ELSE 750.00
    END,
    'AVAILABLE',
    0
FROM generate_series(1, 150) AS gs
ON CONFLICT DO NOTHING;

-- ============================================
-- SEATS - Event 3: IPL Final (200 seats)
-- Row A: VIP (seats 1-50, ₹5,000)
-- Row B: PREMIUM (seats 51-120, ₹2,500)
-- Row C: GENERAL (seats 121-200, ₹1,000)
-- ============================================
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status, version)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000003',
    'C' || gs,
    CASE
        WHEN gs <= 50 THEN 'A'
        WHEN gs <= 120 THEN 'B'
        ELSE 'C'
    END,
    CASE
        WHEN gs <= 50 THEN 'VIP'
        WHEN gs <= 120 THEN 'PREMIUM'
        ELSE 'GENERAL'
    END,
    CASE
        WHEN gs <= 50 THEN 5000.00
        WHEN gs <= 120 THEN 2500.00
        ELSE 1000.00
    END,
    'AVAILABLE',
    0
FROM generate_series(1, 200) AS gs
ON CONFLICT DO NOTHING;