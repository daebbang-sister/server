CREATE TABLE address(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    receiver_phone_number VARCHAR(50) NOT NULL,
    alias VARCHAR(50) NULL,
    zip_code VARCHAR(6) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    is_default TINYINT(1) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,

    CONSTRAINT `fk_address_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`)
)