package com.daebbang.daebbangcore.domain.review.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.infra.converter.ReviewPointStatusConverter;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetails orderDetail;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 300, nullable = false)
    private String content;

    @Column(length = 300)
    private String reply;

    private LocalDateTime replyUpdatedAt;

    @Convert(converter = ReviewPointStatusConverter.class)
    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private ReviewPointStatus pointStatus;

    private LocalDateTime pointApprovedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @jakarta.persistence.OrderBy("imageOrder ASC")
    private final List<ReviewImage> images = new ArrayList<>();

    @Builder
    private Review(Users user, Products product, OrderDetails orderDetail, Integer rating, String content) {
        this.user = user;
        this.product = product;
        this.orderDetail = orderDetail;
        this.rating = rating;
        this.content = content;
        this.pointStatus = ReviewPointStatus.PENDING;
        this.update();
    }

    public static Review create(Users user, Products product, OrderDetails orderDetail, int rating, String content) {
        return Review.builder()
            .user(user)
            .product(product)
            .orderDetail(orderDetail)
            .rating(rating)
            .content(content)
            .build();
    }

    public void updateContent(int rating, String content) {
        if (this.pointStatus == ReviewPointStatus.APPROVED) {
            throw new BusinessException(UserErrorCode.REVIEW_ALREADY_APPROVED);
        }
        this.rating = rating;
        this.content = content;
        this.update();
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void clearImages() {
        this.images.clear();
    }

    public void writeReply(String replyContent) {
        this.reply = replyContent;
        this.replyUpdatedAt = LocalDateTime.now();
        this.update();
    }

    public void approvePoint() {
        this.pointStatus = ReviewPointStatus.APPROVED;
        this.pointApprovedAt = LocalDateTime.now();
        this.update();
    }

    public boolean isApproved() {
        return this.pointStatus == ReviewPointStatus.APPROVED;
    }

    public boolean isPhotoReview() {
        return !this.images.isEmpty();
    }

    public void softDelete() {
        if (this.pointStatus == ReviewPointStatus.APPROVED) {
            throw new BusinessException(UserErrorCode.REVIEW_ALREADY_APPROVED);
        }
        this.delete();
    }
}
