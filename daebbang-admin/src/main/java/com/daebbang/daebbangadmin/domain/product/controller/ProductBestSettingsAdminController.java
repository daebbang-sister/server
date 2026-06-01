package com.daebbang.daebbangadmin.domain.product.controller;

import com.daebbang.daebbangadmin.domain.product.dto.request.ProductBestSettingsUpdateRequest;
import com.daebbang.daebbangadmin.domain.product.dto.response.ProductBestSettingsResponse;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.product.service.ProductBestSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 베스트 상품 설정 관리 API. (관리자)
 *
 * <p>관리자 모듈은 아직 인증/인가 등 본격적인 작업이 진행되지 않았으며,
 * 본 컨트롤러는 설정 관리 기능의 골격(skeleton)이다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/admin/products/best-settings")
public class ProductBestSettingsAdminController {

    private final ProductBestSettingsService productBestSettingsService;

    @GetMapping
    public CommonResponse<ProductBestSettingsResponse> getBestSettings() {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            ProductBestSettingsResponse.of(productBestSettingsService.getSettings())
        );
    }

    @PutMapping
    public CommonResponse<ProductBestSettingsResponse> updateBestSettings(
        @Valid @RequestBody ProductBestSettingsUpdateRequest request
    ) {
        return CommonResponse.success(
            CommonSuccessCode.UPDATE_SUCCESS,
            ProductBestSettingsResponse.of(
                productBestSettingsService.updateSettings(request.maxPeriodDays(), request.defaultPeriodDays())
            )
        );
    }
}
