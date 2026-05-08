CREATE TABLE point_lots
(
    id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '적립 단위 고유 아이디',
    points_id        BIGINT   NOT NULL COMMENT '회원 적립금 고유 아이디',
    earn_history_id  BIGINT   NOT NULL COMMENT '이 lot을 만든 적립 history',
    initial_amount   INT      NOT NULL COMMENT '적립 당시 금액 (불변)',
    remaining_amount INT      NOT NULL COMMENT '현재 잔여 사용 가능량',
    expired_at       DATETIME NULL COMMENT '소멸 일자 (NULL = 무기한, 환불 lot 등)',
    created_at       DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_point_lots_earn (earn_history_id),
    CONSTRAINT fk_point_lots_points FOREIGN KEY (points_id) REFERENCES points (id),
    CONSTRAINT fk_point_lots_earn FOREIGN KEY (earn_history_id) REFERENCES user_point_history (id)
);

CREATE INDEX ix_point_lots_active ON point_lots (points_id, remaining_amount, created_at);
CREATE INDEX ix_point_lots_expirable ON point_lots (points_id, expired_at);
