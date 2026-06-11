
-- Sample users
INSERT INTO users (id, name, email, phone) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Ava Sharma',   'ava@ticketing.com',   '9876543210'),
    ('a0000000-0000-0000-0000-000000000002', 'Rahul Verma',  'rahul@ticketing.com', '9876543211'),
    ('a0000000-0000-0000-0000-000000000003', 'Priya Singh',  'priya@ticketing.com', '9876543212')
ON CONFLICT DO NOTHING;

-- Sample event (100 seat concert)
INSERT INTO events (id, name, venue, event_date, total_seats) VALUES
    ('e0000000-0000-0000-0000-000000000001',
     'Arijit Singh Live Concert',
     'MMRDA Grounds, Mumbai',
     NOW() + INTERVAL '30 days',
     100)
ON CONFLICT DO NOTHING;

-- Generate 100 seats for the event
INSERT INTO seats (id, event_id, seat_number, row_label, section, price, status)
SELECT
    gen_random_uuid(),
    'e0000000-0000-0000-0000-000000000001',
    'S' || gs,
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
    'AVAILABLE'
FROM generate_series(1, 100) AS gs
ON CONFLICT DO NOTHING;