-- 1. Create Products Table
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          unit_price NUMERIC(19, 2) NOT NULL,
                          stock_quantity INT NOT NULL
);

-- 2. Create Orders Table
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(19, 2) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create Order Items Table
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             unit_price NUMERIC(19, 2) NOT NULL,
                             subtotal NUMERIC(19, 2) NOT NULL,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 4. Create Order Status History Table (Audit Log)
CREATE TABLE order_status_history (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_id BIGINT NOT NULL,
                                      previous_status VARCHAR(50),
                                      new_status VARCHAR(50) NOT NULL,
                                      reason VARCHAR(255),
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 5. Seed Initial Sample Data for Products
INSERT INTO products (name, unit_price, stock_quantity)
VALUES
    ('Mechanical Keyboard', 99.99, 50),
    ('Wireless Mouse', 49.99, 100),
    ('4K Monitor', 399.99, 20);