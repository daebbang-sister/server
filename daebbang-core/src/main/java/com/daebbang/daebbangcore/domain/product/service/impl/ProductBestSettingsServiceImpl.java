package com.daebbang.daebbangcore.domain.product.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ProductErrorCode;
import com.daebbang.daebbangcore.domain.product.dto.ProductBestSettingsResult;
import com.daebbang.daebbangcore.domain.product.entity.ProductBestSettings;
import com.daebbang.daebbangcore.domain.product.repository.ProductBestSettingsRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductBestSettingsService;
import jakarta.annotation.Nullable;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductBestSettingsServiceImpl implements ProductBestSettingsService {

    /** 설정 행이 없을 때 사용할 기본값(시드와 동일). */
    private static final int FALLBACK_MAX_PERIOD_DAYS = 90;
    private static final int FALLBACK_DEFAULT_PERIOD_DAYS = 7;

    private final ProductBestSettingsRepository productBestSettingsRepository;

    @Override
    public int getMaxPeriodDays() {
        return productBestSettingsRepository.findTopByOrderByIdAsc()
            .map(ProductBestSettings::getMaxPeriodDays)
            .orElse(FALLBACK_MAX_PERIOD_DAYS);
    }

    @Override
    public int getDefaultPeriodDays() {
        return productBestSettingsRepository.findTopByOrderByIdAsc()
            .map(ProductBestSettings::getDefaultPeriodDays)
            .orElse(FALLBACK_DEFAULT_PERIOD_DAYS);
    }

    @Override
    public int resolvePeriodDays(@Nullable Integer requested) {
        ProductBestSettings settings = productBestSettingsRepository.findTopByOrderByIdAsc().orElse(null);
        int max = Objects.nonNull(settings) ? settings.getMaxPeriodDays() : FALLBACK_MAX_PERIOD_DAYS;
        int def = Objects.nonNull(settings) ? settings.getDefaultPeriodDays() : FALLBACK_DEFAULT_PERIOD_DAYS;

        int value = Objects.isNull(requested) ? def : requested;
        if (value < 1) {
            value = 1;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    @Override
    public ProductBestSettingsResult getSettings() {
        ProductBestSettings settings = productBestSettingsRepository.findTopByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_BEST_SETTINGS));
        return ProductBestSettingsResult.of(settings);
    }

    @Override
    @Transactional
    public ProductBestSettingsResult updateSettings(int maxPeriodDays, int defaultPeriodDays) {
        if (maxPeriodDays < 1 || defaultPeriodDays < 1 || defaultPeriodDays > maxPeriodDays) {
            throw new BusinessException(ProductErrorCode.INVALID_BEST_SETTINGS);
        }

        ProductBestSettings settings = productBestSettingsRepository.findTopByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_BEST_SETTINGS));

        settings.update(maxPeriodDays, defaultPeriodDays);
        productBestSettingsRepository.save(settings);

        return ProductBestSettingsResult.of(settings);
    }
}
