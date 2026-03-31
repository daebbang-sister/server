CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_detail_id BIGINT NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT `fk_carts_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`),

    CONSTRAINT `fk_carts_product_detail`
    FOREIGN KEY (`product_detail_id`)
    REFERENCES `product_details`(`id`)
)