CREATE TABLE order_settings
(
    id                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '주문 설정 고유 아이디',
    auto_complete_days INT      NOT NULL DEFAULT 7 COMMENT '배송 완료 후 자동 구매 확정까지 일수',
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO order_settings (auto_complete_days, updated_at)
VALUES (7, NOW());
