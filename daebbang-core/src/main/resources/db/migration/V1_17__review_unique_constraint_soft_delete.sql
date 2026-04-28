ALTER TABLE reviews
    ADD CONSTRAINT uq_review_order_detail_active UNIQUE (order_detail_id, deleted_at);

ALTER TABLE reviews
    DROP INDEX uq_review_order_detail;
