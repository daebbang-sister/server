package com.daebbang.daebbangcore.domain.point.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_lots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLot extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_id", nullable = false)
    private Points points;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "earn_history_id", nullable = false, unique = true)
    private UserPointHistory earnHistory;

    @Column(nullable = false)
    private Integer initialAmount;

    @Column(nullable = false)
    private Integer remainingAmount;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder
    private PointLot(Points points, UserPointHistory earnHistory, int initialAmount,
        LocalDateTime expiredAt) {
        this.points = points;
        this.earnHistory = earnHistory;
        this.initialAmount = initialAmount;
        this.remainingAmount = initialAmount;
        this.expiredAt = expiredAt;
    }

    public static PointLot create(Points points, UserPointHistory earnHistory,
        int initialAmount, LocalDateTime expiredAt) {
        return PointLot.builder()
            .points(points)
            .earnHistory(earnHistory)
            .initialAmount(initialAmount)
            .expiredAt(expiredAt)
            .build();
    }

    /**
     * 사용/만료 시 잔여를 줄인다. 보유량을 초과해 차감하지 않는다.
     * @return 실제 차감된 양
     */
    public int consume(int amount) {
        int consumed = Math.min(this.remainingAmount, Math.max(amount, 0));
        this.remainingAmount -= consumed;
        return consumed;
    }

    public boolean isActive() {
        return this.remainingAmount > 0;
    }

    public boolean isExpired(LocalDateTime now) {
        return this.expiredAt != null && !now.isBefore(this.expiredAt);
    }
}
