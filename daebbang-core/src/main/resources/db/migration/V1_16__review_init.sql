CREATE TABLE reviews
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '리뷰 고유 아이디',
    user_id            BIGINT       NOT NULL COMMENT '회원 고유 아이디',
    product_id         BIGINT       NOT NULL COMMENT '상품 고유 아이디',
    order_detail_id    BIGINT       NOT NULL COMMENT '주문 상세 고유 아이디',
    rating             INT          NOT NULL COMMENT '별점 1점 ~ 5점',
    content            VARCHAR(300) NOT NULL COMMENT '리뷰 내용 (최소 20자, 최대 300자)',
    reply              VARCHAR(300) NULL     COMMENT '관리자 답글 (최대 300자)',
    reply_updated_at   DATETIME     NULL     COMMENT '답글 등록/수정 일자',
    point_status       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=적립금 대기, 1=적립금 승인',
    point_approved_at  DATETIME     NULL     COMMENT '적립금 승인 일자',
    created_at         DATETIME     NOT NULL COMMENT '리뷰 등록 날짜',
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '리뷰 수정 날짜',
    deleted_at         DATETIME     NULL     DEFAULT NULL COMMENT '리뷰 삭제 날짜',
    PRIMARY KEY (id),
    UNIQUE KEY uq_review_order_detail (order_detail_id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_order_detail FOREIGN KEY (order_detail_id) REFERENCES order_details (id)
);

CREATE TABLE review_images
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '이미지 고유 아이디',
    review_id   BIGINT       NOT NULL COMMENT '리뷰 고유 아이디',
    image_url   VARCHAR(500) NOT NULL COMMENT 'S3 이미지 URL',
    image_order INT          NOT NULL COMMENT '이미지 순서 (1~4)',
    created_at  DATETIME     NOT NULL COMMENT '등록 일자',
    PRIMARY KEY (id),
    CONSTRAINT fk_review_images_review FOREIGN KEY (review_id) REFERENCES reviews (id)
);

CREATE TABLE review_point_configs
(
    id                  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '설정 고유 아이디',
    normal_review_point INT      NOT NULL DEFAULT 500  COMMENT '일반 리뷰 포인트 (원)',
    photo_review_point  INT      NOT NULL DEFAULT 1000 COMMENT '포토 리뷰 포인트 (원)',
    auto_approve_days   INT      NOT NULL DEFAULT 7    COMMENT '자동 승인 일수',
    updated_at          DATETIME NOT NULL COMMENT '수정 일자',
    PRIMARY KEY (id)
);

INSERT INTO review_point_configs (normal_review_point, photo_review_point, auto_approve_days, updated_at)
VALUES (500, 1000, 7, NOW());
