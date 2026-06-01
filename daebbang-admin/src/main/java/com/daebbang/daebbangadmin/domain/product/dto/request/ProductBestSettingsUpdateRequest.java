package com.daebbang.daebbangadmin.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 베스트 설정 수정 요청. (관리자)
 * defaultPeriodDays 는 maxPeriodDays 이하여야 하며, 이 정합성은 서비스 계층에서 검증한다.
 */
public record ProductBestSettingsUpdateRequest(

    @NotNull(message = "최대 기간을 입력해주세요.")
    @Min(value = 1, message = "최대 기간은 1일 이상이어야 합니다.")
    Integer maxPeriodDays,

    @NotNull(message = "기본 기간을 입력해주세요.")
    @Min(value = 1, message = "기본 기간은 1일 이상이어야 합니다.")
    Integer defaultPeriodDays
) {
}
