package com.daebbang.daebbangcore.domain.product.dto;

import com.daebbang.daebbangcore.domain.product.entity.ProductBestSettings;
import java.time.LocalDateTime;

public record ProductBestSettingsResult(
    int maxPeriodDays,
    int defaultPeriodDays,
    LocalDateTime updatedAt
) {

    public static ProductBestSettingsResult of(ProductBestSettings settings) {
        return new ProductBestSettingsResult(
            settings.getMaxPeriodDays(),
            settings.getDefaultPeriodDays(),
            settings.getUpdatedAt()
        );
    }
}
