CREATE TABLE claims
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '클레임 고유 아이디',
    order_detail_id       BIGINT       NOT NULL               COMMENT '주문 내역 고유 아이디',
    claim_type            VARCHAR(20)  NOT NULL               COMMENT 'REFUND / EXCHANGE',
    claim_status          VARCHAR(20)  NOT NULL               COMMENT 'REQUESTED / COMPLETED / REJECTED',
    reason_type           VARCHAR(50)  NOT NULL               COMMENT '사유 유형 (NOT_SATISFIED / WRONG_OPTION / WRONG_SIZE / DEFECT_OR_DAMAGE / WRONG_ITEM / DIFFERENT_FROM_DESC)',
    reason_detail         VARCHAR(300) NULL                   COMMENT '상세 사유 (선택, 최대 300자)',
    quantity              INT          NOT NULL               COMMENT '클레임 수량',
    refund_amount         INT          NOT NULL DEFAULT 0     COMMENT '환불 금액',
    refund_point          INT          NOT NULL DEFAULT 0     COMMENT '환불 포인트',
    pickup_receiver       VARCHAR(50)  NOT NULL               COMMENT '수거지 수령인',
    pickup_phone          VARCHAR(20)  NOT NULL               COMMENT '수거지 연락처',
    pickup_zip_code       VARCHAR(10)  NOT NULL               COMMENT '수거지 우편번호',
    pickup_address        VARCHAR(255) NOT NULL               COMMENT '수거지 주소',
    pickup_detail_address VARCHAR(255) NULL                   COMMENT '수거지 상세주소',
    created_at            DATETIME     NOT NULL               COMMENT '신청 날짜',
    completed_at          DATETIME     NULL                   COMMENT '완료 날짜',
    updated_at            DATETIME     NOT NULL               COMMENT '수정 날짜',
    deleted_at            DATETIME     NULL                   COMMENT '삭제 날짜',
    -- 동시 중복 신청 방지: REQUESTED 상태가 아닌 경우 NULL → MySQL 유니크 인덱스에서 NULL 제외
    active_flag           TINYINT(1) GENERATED ALWAYS AS (
        CASE WHEN claim_status = 'REQUESTED' AND deleted_at IS NULL THEN 1 ELSE NULL END
    ) VIRTUAL                                                  COMMENT '활성 클레임 여부 (REQUESTED + 미삭제 시 1, 나머지 NULL)',
    PRIMARY KEY (id),
    CONSTRAINT fk_claims_order_detail FOREIGN KEY (order_detail_id) REFERENCES order_details (id),
    UNIQUE KEY uq_claims_order_detail_active (order_detail_id, active_flag)
);

CREATE TABLE claim_images
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '이미지 고유 아이디',
    claim_id    BIGINT       NOT NULL               COMMENT '클레임 고유 아이디',
    image_url   VARCHAR(500) NOT NULL               COMMENT 'S3 이미지 URL',
    image_order INT          NOT NULL               COMMENT '이미지 순서 (1~5)',
    created_at  DATETIME     NOT NULL               COMMENT '등록 일자',
    PRIMARY KEY (id),
    CONSTRAINT fk_claim_images_claim FOREIGN KEY (claim_id) REFERENCES claims (id)
);
