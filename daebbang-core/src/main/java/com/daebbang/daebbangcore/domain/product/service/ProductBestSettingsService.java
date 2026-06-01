package com.daebbang.daebbangcore.domain.product.service;

import com.daebbang.daebbangcore.domain.product.dto.ProductBestSettingsResult;
import jakarta.annotation.Nullable;

public interface ProductBestSettingsService {

    /**
     * 베스트 조회 최대 기간(일). 판매 로그 보존 기간과 동일한 단일 출처(SSOT).
     */
    int getMaxPeriodDays();

    /**
     * 베스트 조회 기본 기간(일). 사용자가 기간을 지정하지 않은 경우 사용한다.
     */
    int getDefaultPeriodDays();

    /**
     * 요청 기간을 설정값 기준으로 정규화한다.
     * null 이면 기본 기간을, 최대 기간을 초과하면 최대 기간으로 보정한다.
     */
    int resolvePeriodDays(@Nullable Integer requested);

    /**
     * 현재 베스트 설정을 조회한다. (관리자)
     */
    ProductBestSettingsResult getSettings();

    /**
     * 베스트 설정을 수정한다. (관리자)
     */
    ProductBestSettingsResult updateSettings(int maxPeriodDays, int defaultPeriodDays);
}
