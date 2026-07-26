ALTER TABLE orders
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE orders
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE orders
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE orders
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;