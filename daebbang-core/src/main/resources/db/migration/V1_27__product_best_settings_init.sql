CREATE TABLE product_best_settings
(
    id                  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '설정 고유 아이디',
    max_period_days     INT      NOT NULL DEFAULT 90 COMMENT '베스트 조회 최대 기간(일) - 판매 로그 보존 기간과 동일',
    default_period_days INT      NOT NULL DEFAULT 7  COMMENT '베스트 조회 기본 기간(일)',
    updated_at          DATETIME NOT NULL COMMENT '수정 일자',
    PRIMARY KEY (id)
);

INSERT INTO product_best_settings (max_period_days, default_period_days, updated_at)
VALUES (90, 7, NOW());
