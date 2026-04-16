CREATE TABLE orders (
    id                    BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT      NULL,
    order_number          VARCHAR(19) NOT NULL UNIQUE,
    order_status          VARCHAR(20) NOT NULL,
    used_point            INT         NOT NULL DEFAULT 0,
    shipping_fee          INT         NOT NULL,
    total_original_amount INT         NOT NULL,
    total_selling_amount  INT         NOT NULL,
    payment_amount        INT         NOT NULL,
    created_at            DATETIME    NOT NULL,
    updated_at            DATETIME    NOT NULL,
    deleted_at            DATETIME    NULL,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_details (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id          BIGINT      NOT NULL,
    product_detail_id BIGINT      NOT NULL,
    quantity          INT         NOT NULL,
    original_price    INT         NOT NULL,
    discount_rate     INT         NOT NULL,
    discount_price    INT         NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at        DATETIME    NOT NULL,

    CONSTRAINT fk_order_details_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_details_product_detail
        FOREIGN KEY (product_detail_id) REFERENCES product_details(id)
);

CREATE TABLE payment (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT       NOT NULL UNIQUE,
    status              VARCHAR(20)  NOT NULL,
    payment_key         VARCHAR(255) NOT NULL,
    currency            VARCHAR(20)  NOT NULL,
    method              VARCHAR(20)  NOT NULL,
    total_amount        INT          NOT NULL,
    total_cancel_amount INT          NOT NULL DEFAULT 0,
    requested_at        DATETIME     NOT NULL,
    approved_at         DATETIME     NOT NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    deleted_at          DATETIME     NULL,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE payment_cancels (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_id    BIGINT       NOT NULL,
    cancel_reason VARCHAR(200) NOT NULL,
    cancel_amount INT          NOT NULL,
    cancelled_at  DATETIME     NOT NULL,
    created_at    DATETIME     NOT NULL,

    CONSTRAINT fk_payment_cancels_payment
        FOREIGN KEY (payment_id) REFERENCES payment(id)
);
