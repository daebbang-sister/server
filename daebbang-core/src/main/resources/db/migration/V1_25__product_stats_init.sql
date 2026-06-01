CREATE TABLE product_stats (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    wish_count INT NOT NULL DEFAULT 0,
    sales_count INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_product_stats_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_product_stats_product UNIQUE KEY (product_id)
)
