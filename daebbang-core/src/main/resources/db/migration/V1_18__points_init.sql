CREATE TABLE point_policy
(
    id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '적립금 정책 고유 아이디',
    policy_type     VARCHAR(30)    NOT NULL COMMENT 'SIGNUP / REVIEW_TEXT / REVIEW_PHOTO / PURCHASE',
    amount_type     VARCHAR(10)    NOT NULL COMMENT 'FIXED(정액) / RATE(정률)',
    value           DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT '정액(원) 또는 정률(0~1)',
    expiration_days INT            NULL COMMENT '적립금 소멸 일수 (NULL = 무기한)',
    name            VARCHAR(100)   NOT NULL COMMENT '정책 이름',
    is_active       BOOLEAN        NOT NULL DEFAULT FALSE COMMENT '활성화 여부',
    created_at      DATETIME       NOT NULL,
    updated_at      DATETIME       NOT NULL,
    deleted_at      DATETIME       NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE points
(
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '적립금 고유 아이디',
    user_id        BIGINT   NOT NULL COMMENT '회원 고유 아이디',
    current_amount INT      NOT NULL DEFAULT 0 COMMENT '보유 적립금 (누적 적립 - 누적 사용 - 만료)',
    total_earned   INT      NOT NULL DEFAULT 0 COMMENT '누적 적립',
    total_used     INT      NOT NULL DEFAULT 0 COMMENT '누적 사용',
    created_at     DATETIME NOT NULL,
    updated_at     DATETIME NOT NULL,
    deleted_at     DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_points_user (user_id),
    CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_point_history
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '회원 포인트 내역 고유 아이디',
    points_id       BIGINT       NOT NULL COMMENT '적립금 고유 아이디',
    point_policy_id BIGINT       NULL COMMENT '적립금 정책 고유 아이디 (사용/환불 건은 NULL)',
    change_type     VARCHAR(30)  NOT NULL COMMENT 'EARN_SIGNUP / EARN_REVIEW / EARN_PURCHASE / USE_PAYMENT / REFUND_CANCEL / REFUND_REVERSE / EXPIRE',
    reference_id    BIGINT       NULL COMMENT 'order_id, review_id 등 외부 식별자',
    change_amount   INT          NOT NULL COMMENT '변동 금액 (양수)',
    point_amount    INT          NOT NULL COMMENT '변동 후 잔액 스냅샷',
    description     VARCHAR(255) NOT NULL COMMENT '설명',
    created_at      DATETIME     NOT NULL,
    expired_at      DATETIME     NULL COMMENT '소멸 일자 (적립 건에만 채움)',
    PRIMARY KEY (id),
    CONSTRAINT fk_user_point_history_points FOREIGN KEY (points_id) REFERENCES points (id),
    CONSTRAINT fk_user_point_history_policy FOREIGN KEY (point_policy_id) REFERENCES point_policy (id)
);

CREATE INDEX ix_user_point_history_points_created ON user_point_history (points_id, created_at DESC);
CREATE INDEX ix_user_point_history_points_expired ON user_point_history (points_id, expired_at);
