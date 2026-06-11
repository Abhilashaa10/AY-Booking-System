-- USERS
CREATE TABLE IF NOT EXISTS users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    phone      VARCHAR(15),
    created_at TIMESTAMP DEFAULT NOW()
);

-- EVENTS (concerts, matches, trains etc.)
CREATE TABLE IF NOT EXISTS events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    venue       VARCHAR(200),
    event_date  TIMESTAMP NOT NULL,
    total_seats INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- SEATS
CREATE TABLE IF NOT EXISTS seats (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID NOT NULL REFERENCES events(id),
    seat_number VARCHAR(10) NOT NULL,
    row_label   VARCHAR(5),
    section     VARCHAR(20),
    price       DECIMAL(10,2) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version     BIGINT DEFAULT 0,
    CONSTRAINT chk_seat_status CHECK (status IN ('AVAILABLE','HELD','BOOKED'))
);

CREATE INDEX IF NOT EXISTS idx_seat_event_id ON seats(event_id);
CREATE INDEX IF NOT EXISTS idx_seat_status   ON seats(status);
CREATE UNIQUE INDEX IF NOT EXISTS idx_seat_event_number ON seats(event_id, seat_number);

-- BOOKINGS
CREATE TABLE IF NOT EXISTS bookings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL,
    seat_id          UUID NOT NULL REFERENCES seats(id),
    event_id         UUID NOT NULL REFERENCES events(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key  VARCHAR(64) NOT NULL UNIQUE,
    amount           DECIMAL(10,2) NOT NULL,
    expires_at       TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_booking_status CHECK (
        status IN ('PENDING','CONFIRMED','FAILED','CANCELLED','EXPIRED')
    )
);

CREATE INDEX IF NOT EXISTS idx_booking_user_id  ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_seat_id  ON bookings(seat_id);
CREATE INDEX IF NOT EXISTS idx_booking_status   ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_expires  ON bookings(expires_at) WHERE status = 'PENDING';

-- PAYMENTS
CREATE TABLE IF NOT EXISTS payments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id       UUID NOT NULL UNIQUE REFERENCES bookings(id),
    user_id          UUID NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway          VARCHAR(30) DEFAULT 'SIMULATED',
    failure_reason   VARCHAR(255),
    idempotency_key  VARCHAR(64) NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    processed_at     TIMESTAMP,
    CONSTRAINT chk_payment_status CHECK (
        status IN ('PENDING','SUCCESS','FAILED','CANCELLED')
    )
);

CREATE INDEX IF NOT EXISTS idx_payment_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_status     ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_user_id    ON payments(user_id);

-- CANCELLATIONS
CREATE TABLE IF NOT EXISTS cancellations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id   UUID NOT NULL UNIQUE,
    user_id      UUID,
    seat_id      UUID,
    reason       VARCHAR(30) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at   TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    CONSTRAINT chk_cancellation_reason CHECK (
        reason IN ('USER_INITIATED','TTL_EXPIRED','PAYMENT_FAILED','PAYMENT_CANCELLED')
    )
);

CREATE INDEX IF NOT EXISTS idx_cancellation_booking_id ON cancellations(booking_id);
CREATE INDEX IF NOT EXISTS idx_cancellation_user_id    ON cancellations(user_id);

-- NOTIFICATION LOGS
CREATE TABLE IF NOT EXISTS notification_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id     UUID,
    user_id        UUID,
    type           VARCHAR(20) NOT NULL,
    event          VARCHAR(40) NOT NULL,
    recipient      VARCHAR(255),
    status         VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notif_booking_id ON notification_logs(booking_id);
CREATE INDEX IF NOT EXISTS idx_notif_user_id    ON notification_logs(user_id);