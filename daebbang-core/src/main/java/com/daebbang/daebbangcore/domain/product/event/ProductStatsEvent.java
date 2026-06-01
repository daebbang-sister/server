package com.daebbang.daebbangcore.domain.product.event;

public record ProductStatsEvent(Long productId, String field, long delta) {

    public static ProductStatsEvent salesIncrement(Long productId, long quantity) {
        return new ProductStatsEvent(productId, "sales_count", quantity);
    }

    public static ProductStatsEvent salesDecrement(Long productId, long quantity) {
        return new ProductStatsEvent(productId, "sales_count", -quantity);
    }

    public static ProductStatsEvent reviewIncrement(Long productId) {
        return new ProductStatsEvent(productId, "review_count", 1);
    }

    public static ProductStatsEvent reviewDecrement(Long productId) {
        return new ProductStatsEvent(productId, "review_count", -1);
    }

    public static ProductStatsEvent wishIncrement(Long productId) {
        return new ProductStatsEvent(productId, "wish_count", 1);
    }

    public static ProductStatsEvent wishDecrement(Long productId) {
        return new ProductStatsEvent(productId, "wish_count", -1);
    }
}
