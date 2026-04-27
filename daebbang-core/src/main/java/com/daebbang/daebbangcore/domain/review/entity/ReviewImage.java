package com.daebbang.daebbangcore.domain.review.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(length = 500, nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer imageOrder;

    @Builder
    private ReviewImage(Review review, String imageUrl, Integer imageOrder) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static ReviewImage create(Review review, String imageUrl, int imageOrder) {
        return ReviewImage.builder()
            .review(review)
            .imageUrl(imageUrl)
            .imageOrder(imageOrder)
            .build();
    }
}
