CREATE TABLE product_image (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_type TINYINT UNSIGNED NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    image_order TINYINT UNSIGNED NOT NULL,
    deleted_at DATETIME NULL,

    CONSTRAINT `fk_product_image_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `products` (`id`)
);

CREATE TABLE product_details (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    color VARCHAR(50) NOT NULL,
    size VARCHAR(50) NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,

    CONSTRAINT `fk_product_details_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `products` (`id`),

    UNIQUE KEY unique_product_details(product_id, color, size)
);