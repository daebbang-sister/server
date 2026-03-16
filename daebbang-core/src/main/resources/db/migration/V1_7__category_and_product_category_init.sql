CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    super_id BIGINT NULL,
    category_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,

    CONSTRAINT `fk_super_category`
    FOREIGN KEY (`super_id`)
    REFERENCES `categories`(`id`),

    UNIQUE KEY unique_category_unique(super_id, category_name)
);

CREATE TABLE product_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,

    CONSTRAINT `fk_product_category_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories`(`id`),

    CONSTRAINT `fk_product_category_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `products`(`id`),

    UNIQUE KEY product_category_unique(category_id, product_id)
);