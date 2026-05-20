package com.daebbang.daebbangapi.domain.order.dto.response;

import com.daebbang.daebbangcore.domain.order.entity.ClaimImage;
import com.daebbang.daebbangcore.domain.order.entity.ClaimStatus;
import com.daebbang.daebbangcore.domain.order.entity.ClaimType;
import com.daebbang.daebbangcore.domain.order.entity.Claims;
import com.daebbang.daebbangcore.domain.order.entity.ReasonType;
import java.time.LocalDateTime;
import java.util.List;

public record ClaimResponse(
    Long claimId,
    ClaimType claimType,
    ClaimStatus claimStatus,
    ReasonType reasonType,
    String reasonDetail,
    int quantity,
    int refundAmount,
    int refundPoint,
    String pickupReceiver,
    String pickupPhone,
    String pickupZipCode,
    String pickupAddress,
    String pickupDetailAddress,
    List<String> imageUrls,
    LocalDateTime createdAt
) {
    public static ClaimResponse from(Claims claim) {
        List<String> urls = claim.getImages().stream()
            .sorted((a, b) -> Integer.compare(a.getImageOrder(), b.getImageOrder()))
            .map(ClaimImage::getImageUrl)
            .toList();

        return new ClaimResponse(
            claim.getId(),
            claim.getClaimType(),
            claim.getClaimStatus(),
            claim.getReasonType(),
            claim.getReasonDetail(),
            claim.getQuantity(),
            claim.getRefundAmount(),
            claim.getRefundPoint(),
            claim.getPickupReceiver(),
            claim.getPickupPhone(),
            claim.getPickupZipCode(),
            claim.getPickupAddress(),
            claim.getPickupDetailAddress(),
            urls,
            claim.getCreatedAt()
        );
    }
}
