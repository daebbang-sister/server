CREATE TABLE wish_list(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    CONSTRAINT `fk_wish_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`),

    CONSTRAINT `fk_wish_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `products`(`id`),

    CONSTRAINT `uq_wish_user_product`
    UNIQUE KEY (`user_id`, `product_id`)
)