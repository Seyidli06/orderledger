-- 1. Add version column for Optimistic Locking on products table
ALTER TABLE products
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Create coupons table
CREATE TABLE coupons (
                         id BIGSERIAL PRIMARY KEY,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         discount_percentage INT NOT NULL,
                         expiration_date TIMESTAMP,
                         max_usage_limit INT NOT NULL,
                         current_usage_count INT NOT NULL DEFAULT 0,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Insert initial sample coupons for testing
INSERT INTO coupons (code, discount_percentage, expiration_date, max_usage_limit, current_usage_count, is_active)
VALUES
    ('WELCOME10', 10, '2026-12-31 23:59:59', 100, 0, TRUE),
    ('FLASH20', 20, '2026-08-01 00:00:00', 50, 0, TRUE);