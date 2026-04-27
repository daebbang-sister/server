package com.daebbang.daebbangcore.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_point_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPointConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer normalReviewPoint;

    @Column(nullable = false)
    private Integer photoReviewPoint;

    @Column(nullable = false)
    private Integer autoApproveDays;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(int normalReviewPoint, int photoReviewPoint, int autoApproveDays) {
        this.normalReviewPoint = normalReviewPoint;
        this.photoReviewPoint = photoReviewPoint;
        this.autoApproveDays = autoApproveDays;
        this.updatedAt = LocalDateTime.now();
    }
}
