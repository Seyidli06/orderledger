ALTER TABLE products
    ADD CONSTRAINT chk_products_unit_price_positive
        CHECK (unit_price > 0),

    ADD CONSTRAINT chk_products_stock_non_negative
        CHECK (stock_quantity >= 0);


ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_non_negative
        CHECK (total_amount >= 0),

    ADD CONSTRAINT chk_orders_status_valid
        CHECK (
            status IN (
                'CREATED',
                'PAID',
                'CANCELLED',
                'SHIPPED',
                'COMPLETED'
            )
        );


ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_quantity_positive
        CHECK (quantity > 0),

    ADD CONSTRAINT chk_order_items_unit_price_positive
        CHECK (unit_price > 0),

    ADD CONSTRAINT chk_order_items_subtotal_non_negative
        CHECK (subtotal >= 0);


ALTER TABLE order_status_history
    ADD CONSTRAINT chk_history_new_status_valid
        CHECK (
            new_status IN (
                           'CREATED',
                           'PAID',
                           'CANCELLED',
                           'SHIPPED',
                           'COMPLETED'
                )
            ),

    ADD CONSTRAINT chk_history_previous_status_valid
        CHECK (
            previous_status IS NULL
            OR previous_status IN (
                'CREATED',
                'PAID',
                'CANCELLED',
                'SHIPPED',
                'COMPLETED'
            )
        );


ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_discount_percentage
        CHECK (discount_percentage BETWEEN 1 AND 100),

    ADD CONSTRAINT chk_coupons_max_usage_positive
        CHECK (max_usage_limit > 0),

    ADD CONSTRAINT chk_coupons_current_usage_non_negative
        CHECK (current_usage_count >= 0),

    ADD CONSTRAINT chk_coupons_usage_within_limit
        CHECK (current_usage_count <= max_usage_limit);