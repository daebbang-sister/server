-- 1) review_point_configs 의 적립 금액을 point_policy 로 시드한다.
INSERT INTO point_policy (
    policy_type, amount_type, value, expiration_days, name, is_active, created_at, updated_at, deleted_at
)
SELECT 'REVIEW_TEXT', 'FIXED', normal_review_point, NULL, '일반 리뷰 적립', TRUE, NOW(), NOW(), NULL
FROM review_point_configs
ORDER BY id ASC
LIMIT 1;

INSERT INTO point_policy (
    policy_type, amount_type, value, expiration_days, name, is_active, created_at, updated_at, deleted_at
)
SELECT 'REVIEW_PHOTO', 'FIXED', photo_review_point, NULL, '포토 리뷰 적립', TRUE, NOW(), NOW(), NULL
FROM review_point_configs
ORDER BY id ASC
LIMIT 1;

-- 2) 적립 금액 컬럼 제거 — 적립 정책은 이제 point_policy 가 단일 출처(SSOT)
ALTER TABLE review_point_configs DROP COLUMN normal_review_point;
ALTER TABLE review_point_configs DROP COLUMN photo_review_point;

-- 3) 테이블명 변경 — 더 이상 적립 설정이 아니므로 review_settings 로 일반화
ALTER TABLE review_point_configs RENAME TO review_settings;
