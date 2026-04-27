package com.daebbang.daebbangcore.domain.review.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.repository.OrderDetailsRepository;
import com.daebbang.daebbangcore.domain.review.command.CreateReviewCommand;
import com.daebbang.daebbangcore.domain.review.command.UpdateReviewCommand;
import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewImage;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import com.daebbang.daebbangcore.domain.review.repository.ReviewPointConfigRepository;
import com.daebbang.daebbangcore.domain.review.repository.ReviewRepository;
import com.daebbang.daebbangcore.domain.review.service.ReviewService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewPointConfigRepository reviewPointConfigRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void createReview(CreateReviewCommand command) {
        if (reviewRepository.existsByOrderDetailIdAndDeletedAtIsNull(command.orderDetailId())) {
            throw new BusinessException(UserErrorCode.REVIEW_ALREADY_EXISTS);
        }

        OrderDetails orderDetail = orderDetailsRepository
            .findByIdAndUserIdAndStatus(command.orderDetailId(), command.userId(), OrderDetailStatus.NORMAL)
            .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_DETAIL_NOT_FOUND));

        if (orderDetail.getOrder().getOrderStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(UserErrorCode.ORDER_NOT_COMPLETED);
        }

        Users user = userService.getUserById(command.userId());

        Review review = Review.create(
            user,
            orderDetail.getProductDetail().getProduct(),
            orderDetail,
            command.rating(),
            command.content()
        );

        addImages(review, command.imageUrls());
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void updateReview(UpdateReviewCommand command) {
        Review review = reviewRepository.findActiveByIdAndUserId(command.reviewId(), command.userId())
            .orElseThrow(() -> new BusinessException(UserErrorCode.REVIEW_NOT_FOUND));

        review.updateContent(command.rating(), command.content());

        review.clearImages();
        addImages(review, command.imageUrls());
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findActiveByIdAndUserId(reviewId, userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.REVIEW_NOT_FOUND));

        review.softDelete();
    }

    @Override
    public Page<@NonNull Review> getMyReviews(Long userId, Pageable pageable) {
        return reviewRepository.findActiveReviewsByUserId(userId, pageable);
    }

    @Override
    public Page<@NonNull Review> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findActiveReviewsByProductId(productId, pageable);
    }

    @Override
    public ReviewStatsResult getProductReviewStats(Long productId) {
        return reviewRepository.getReviewStats(productId);
    }

    @Override
    public ReviewPointConfig getPointConfig() {
        return reviewPointConfigRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new BusinessException(UserErrorCode.REVIEW_POINT_CONFIG_NOT_FOUND));
    }

    private void addImages(Review review, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            review.addImage(ReviewImage.create(review, imageUrls.get(i), i + 1));
        }
    }
}
