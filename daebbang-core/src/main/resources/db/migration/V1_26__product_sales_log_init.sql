CREATE TABLE product_sales_log (
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sale_date  DATE   NOT NULL,
    sales_count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_product_sales_log_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_product_sales_log UNIQUE KEY (product_id, sale_date)
);
