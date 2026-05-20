package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ImageErrorCode;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcore.domain.order.command.ClaimCommand;
import com.daebbang.daebbangcore.domain.order.entity.ClaimImage;
import com.daebbang.daebbangcore.domain.order.entity.ClaimStatus;
import com.daebbang.daebbangcore.domain.order.entity.Claims;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.repository.ClaimRepository;
import com.daebbang.daebbangcore.domain.order.repository.OrderDetailsRepository;
import com.daebbang.daebbangcore.domain.order.service.ClaimService;
import com.daebbang.daebbangcore.infra.service.S3Service;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimServiceImpl implements ClaimService {

    private static final int MAX_CLAIM_IMAGES = 5;
    private static final String S3_CLAIM_DIRECTORY = "claim";

    private final ClaimRepository claimRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public Claims createClaim(ClaimCommand command) {
        List<UploadFile> images = safe(command.images());
        if (images.size() > MAX_CLAIM_IMAGES) {
            throw new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        if (command.quantity() <= 0) {
            throw new BusinessException(OrderErrorCode.CLAIM_QUANTITY_INVALID);
        }

        // PESSIMISTIC_WRITE 락: 동일 주문상세에 대한 동시 클레임 신청을 직렬화한다.
        OrderDetails orderDetail = orderDetailsRepository
            .findByIdAndUserIdAndStatusForUpdate(command.orderDetailId(), command.userId(), OrderDetailStatus.NORMAL)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_DETAIL_NOT_FOUND));

        if (claimRepository.existsByOrderDetailIdAndStatus(command.orderDetailId(), ClaimStatus.REQUESTED)) {
            throw new BusinessException(OrderErrorCode.CLAIM_ALREADY_EXISTS);
        }

        if (command.quantity() > orderDetail.getQuantity()) {
            throw new BusinessException(OrderErrorCode.CLAIM_QUANTITY_EXCEEDED);
        }

        Orders order = orderDetail.getOrder();
        int refundAmount = calculateRefundAmount(orderDetail, command.quantity());

        Claims claim = Claims.create(
            orderDetail,
            command.claimType(),
            command.reasonType(),
            command.reasonDetail(),
            command.quantity(),
            refundAmount,
            0,
            order.getReceiver(),
            order.getReceiverPhone(),
            order.getZipCode(),
            order.getAddress(),
            order.getDetailAddress()
        );

        List<String> uploadedUrls = s3Service.uploadAll(images, S3_CLAIM_DIRECTORY);
        s3Service.registerS3CleanupOnRollback(uploadedUrls);
        addImages(claim, uploadedUrls);

        orderDetail.claimRequest();
        claimRepository.save(claim);

        return claim;
    }

    private int calculateRefundAmount(OrderDetails orderDetail, int quantity) {
        return orderDetail.getDiscountPrice() * quantity / orderDetail.getQuantity();
    }

    private void addImages(Claims claim, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            claim.addImage(ClaimImage.create(claim, imageUrls.get(i), i + 1));
        }
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
