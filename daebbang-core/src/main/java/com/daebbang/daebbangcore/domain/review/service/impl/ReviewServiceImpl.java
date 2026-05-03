package com.daebbang.daebbangcore.domain.review.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ImageErrorCode;
import com.daebbang.daebbangcommon.error.ReviewErrorCode;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.service.OrderDetailService;
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
import com.daebbang.daebbangcore.infra.service.S3Service;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final int MAX_REVIEW_IMAGES = 4;
    private static final String S3_REVIEW_DIRECTORY = "review";

    private final ReviewRepository reviewRepository;
    private final ReviewPointConfigRepository reviewPointConfigRepository;
    private final OrderDetailService orderDetailService;
    private final UserService userService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void createReview(CreateReviewCommand command) {
        List<UploadFile> images = safe(command.images());
        if (images.size() > MAX_REVIEW_IMAGES) {
            throw new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        if (reviewRepository.existsByOrderDetailIdAndDeletedAtIsNull(command.orderDetailId())) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        OrderDetails orderDetail = orderDetailService
            .getOrderDetailForReview(command.orderDetailId(), command.userId());

        Users user = userService.getUserById(command.userId());

        Review review = Review.create(
            user,
            orderDetail.getProductDetail().getProduct(),
            orderDetail,
            command.rating(),
            command.content()
        );

        List<String> uploadedUrls = uploadAll(images);
        addImages(review, uploadedUrls);
        reviewRepository.save(review);

        registerS3CleanupOnRollback(uploadedUrls);
    }

    @Override
    @Transactional
    public void updateReview(UpdateReviewCommand command) {
        List<String> keepUrls = safe(command.keepImageUrls());
        List<UploadFile> newImages = safe(command.newImages());
        if (keepUrls.size() + newImages.size() > MAX_REVIEW_IMAGES) {
            throw new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        Review review = reviewRepository.findActiveByIdAndUserId(command.reviewId(), command.userId())
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateContent(command.rating(), command.content());

        List<String> existingUrls = review.getImages().stream().map(ReviewImage::getImageUrl).toList();
        Set<String> keepSet = new HashSet<>(keepUrls);
        List<String> removedUrls = existingUrls.stream().filter(url -> !keepSet.contains(url)).toList();

        List<String> uploadedUrls = uploadAll(newImages);

        review.clearImages();
        List<String> finalUrls = new ArrayList<>(keepUrls.size() + uploadedUrls.size());
        finalUrls.addAll(keepUrls);
        finalUrls.addAll(uploadedUrls);
        addImages(review, finalUrls);

        registerS3CleanupOnCommit(removedUrls);
        registerS3CleanupOnRollback(uploadedUrls);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findActiveByIdAndUserId(reviewId, userId)
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        List<String> imageUrls = review.getImages().stream().map(ReviewImage::getImageUrl).toList();

        review.softDelete();

        registerS3CleanupOnCommit(imageUrls);
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
        return reviewPointConfigRepository.findTopByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_POINT_CONFIG_NOT_FOUND));
    }

    private List<String> uploadAll(List<UploadFile> files) {
        if (files.isEmpty()) return List.of();
        List<String> uploaded = new ArrayList<>(files.size());
        try {
            for (UploadFile file : files) {
                uploaded.add(s3Service.upload(file, S3_REVIEW_DIRECTORY));
            }
            return List.copyOf(uploaded);
        } catch (RuntimeException e) {
            s3Service.deleteAll(uploaded);
            throw e;
        }
    }

    private void addImages(Review review, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            review.addImage(ReviewImage.create(review, imageUrls.get(i), i + 1));
        }
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private void registerS3CleanupOnCommit(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            s3Service.deleteAll(urls);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                s3Service.deleteAll(urls);
            }
        });
    }

    private void registerS3CleanupOnRollback(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    s3Service.deleteAll(urls);
                }
            }
        });
    }
}
