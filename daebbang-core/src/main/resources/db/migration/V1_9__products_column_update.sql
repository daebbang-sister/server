ALTER TABLE products
    ADD COLUMN hover_image_url VARCHAR(255) NULL;

ALTER TABLE products
    ADD COLUMN discount_type TINYINT UNSIGNED NOT NULL DEFAULT 1;

ALTER TABLE products
    ADD COLUMN discount_rate TINYINT UNSIGNED NULL;

ALTER TABLE products
    ADD COLUMN discount_start_date DATE NULL;

ALTER TABLE products
    ADD COLUMN discount_end_date DATE NULL;

ALTER TABLE products
    ADD COLUMN original_price INT NOT NULL;

ALTER TABLE products
    DROP COLUMN product_price;