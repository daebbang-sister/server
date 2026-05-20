package com.daebbang.daebbangapi.domain.order.controller;

import com.daebbang.daebbangapi.domain.order.dto.request.ClaimRequest;
import com.daebbang.daebbangapi.domain.order.dto.response.ClaimResponse;
import com.daebbang.daebbangapi.domain.order.dto.response.ReasonTypeResponse;
import com.daebbang.daebbangapi.domain.review.support.UploadFileMapper;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.order.entity.Claims;
import com.daebbang.daebbangcore.domain.order.entity.ReasonType;
import com.daebbang.daebbangcore.domain.order.service.ClaimService;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping("/claim/reason-types")
    public ResponseEntity<@NonNull CommonResponse<List<ReasonTypeResponse>>> getReasonTypes() {
        List<ReasonTypeResponse> reasons = Arrays.stream(ReasonType.values())
            .map(ReasonTypeResponse::from)
            .toList();
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.CLAIM_REASON_TYPES_RETRIEVED, reasons));
    }

    @PostMapping(value = "/{orderDetailId}/claim", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull CommonResponse<ClaimResponse>> createClaim(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long orderDetailId,
        @Valid @RequestPart("data") ClaimRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<UploadFile> uploadFiles = UploadFileMapper.toUploadFiles(images);
        Claims claim = claimService.createClaim(request.toCommand(userId, orderDetailId, uploadFiles));
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonResponse.success(UserSuccessCode.CLAIM_CREATED, ClaimResponse.from(claim)));
    }
}
