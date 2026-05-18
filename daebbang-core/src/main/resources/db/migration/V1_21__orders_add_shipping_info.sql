ALTER TABLE orders
    ADD COLUMN receiver       VARCHAR(50)  NOT NULL DEFAULT '' AFTER payment_amount,
    ADD COLUMN receiver_phone VARCHAR(20)  NOT NULL DEFAULT '' AFTER receiver,
    ADD COLUMN zip_code       VARCHAR(10)  NOT NULL DEFAULT '' AFTER receiver_phone,
    ADD COLUMN address        VARCHAR(255) NOT NULL DEFAULT '' AFTER zip_code,
    ADD COLUMN detail_address VARCHAR(255) NOT NULL DEFAULT '' AFTER address,
    ADD COLUMN order_note     VARCHAR(100)     NULL AFTER detail_address;
