CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_status TINYINT(1) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_price INT NOT NULL,
    simple_description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    main_image_url VARCHAR(255) NOT NULL
)