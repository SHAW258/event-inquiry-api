-- Schema DDL for Event Inquiry Management Application

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS event_inquiries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    event_date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    estimated_budget NUMERIC(12, 2) NOT NULL,
    guest_count INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    special_requests VARCHAR(1000),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Indexing for performance and lookup optimization
CREATE INDEX IF NOT EXISTS idx_inquiries_user_id ON event_inquiries(user_id);
CREATE INDEX IF NOT EXISTS idx_inquiries_status ON event_inquiries(status);
CREATE INDEX IF NOT EXISTS idx_inquiries_event_type ON event_inquiries(event_type);
