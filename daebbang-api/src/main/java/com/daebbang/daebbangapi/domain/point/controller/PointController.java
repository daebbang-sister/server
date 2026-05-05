package com.daebbang.daebbangapi.domain.point.controller;

import com.daebbang.daebbangapi.domain.point.dto.response.PointBalanceResponse;
import com.daebbang.daebbangapi.domain.point.dto.response.PointHistoryResponse;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/points")
public class PointController {

    private final PointService pointService;

    @GetMapping("/me")
    public ResponseEntity<@NonNull CommonResponse<PointBalanceResponse>> getMyBalance(
        @AuthenticationPrincipal Long userId
    ) {
        PointBalanceResponse response = PointBalanceResponse.from(pointService.getBalance(userId));
        return ResponseEntity.ok(
            CommonResponse.success(UserSuccessCode.POINT_BALANCE_RETRIEVED, response));
    }

    @GetMapping("/me/history")
    public ResponseEntity<@NonNull CommonResponse<Page<@NonNull PointHistoryResponse>>> getMyHistory(
        @AuthenticationPrincipal Long userId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<@NonNull PointHistoryResponse> response = pointService.getHistory(userId, pageable)
            .map(PointHistoryResponse::from);
        return ResponseEntity.ok(
            CommonResponse.success(UserSuccessCode.POINT_HISTORY_RETRIEVED, response));
    }
}
