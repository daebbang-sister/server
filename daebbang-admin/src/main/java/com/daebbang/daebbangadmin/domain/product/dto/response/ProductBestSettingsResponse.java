package com.daebbang.daebbangadmin.domain.product.dto.response;

import com.daebbang.daebbangcore.domain.product.dto.ProductBestSettingsResult;
import java.time.LocalDateTime;

public record ProductBestSettingsResponse(
    int maxPeriodDays,
    int defaultPeriodDays,
    LocalDateTime updatedAt
) {

    public static ProductBestSettingsResponse of(ProductBestSettingsResult result) {
        return new ProductBestSettingsResponse(
            result.maxPeriodDays(),
            result.defaultPeriodDays(),
            result.updatedAt()
        );
    }
}
