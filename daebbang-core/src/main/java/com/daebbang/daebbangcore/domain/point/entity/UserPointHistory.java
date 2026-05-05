package com.daebbang.daebbangcore.domain.point.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPointHistory extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_id", nullable = false)
    private Points points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_policy_id")
    private PointPolicy pointPolicy;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private ChangeType changeType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private Integer changeAmount;

    @Column(nullable = false)
    private Integer pointAmount;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder
    private UserPointHistory(Points points, PointPolicy pointPolicy, ChangeType changeType,
        Long referenceId, int changeAmount, int pointAmount, String description,
        LocalDateTime expiredAt) {
        this.points = points;
        this.pointPolicy = pointPolicy;
        this.changeType = changeType;
        this.referenceId = referenceId;
        this.changeAmount = changeAmount;
        this.pointAmount = pointAmount;
        this.description = description;
        this.expiredAt = expiredAt;
    }

    /**
     * 적립 내역. 정책과 소멸일자가 함께 기록된다.
     */
    public static UserPointHistory ofEarn(Points points, PointPolicy policy, ChangeType changeType,
        Long referenceId, int changeAmount, int pointAmountAfter, String description,
        LocalDateTime expiredAt) {
        return UserPointHistory.builder()
            .points(points)
            .pointPolicy(policy)
            .changeType(changeType)
            .referenceId(referenceId)
            .changeAmount(changeAmount)
            .pointAmount(pointAmountAfter)
            .description(description)
            .expiredAt(expiredAt)
            .build();
    }

    /**
     * 사용·환불·만료 등 정책과 무관한 변동 내역.
     */
    public static UserPointHistory ofChange(Points points, ChangeType changeType, Long referenceId,
        int changeAmount, int pointAmountAfter, String description) {
        return UserPointHistory.builder()
            .points(points)
            .pointPolicy(null)
            .changeType(changeType)
            .referenceId(referenceId)
            .changeAmount(changeAmount)
            .pointAmount(pointAmountAfter)
            .description(description)
            .expiredAt(null)
            .build();
    }
}
