package com.daebbang.daebbangcore.domain.point.entity;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.PointErrorCode;
import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Points extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @Column(nullable = false)
    private Integer currentAmount;

    @Column(nullable = false)
    private Integer totalEarned;

    @Column(nullable = false)
    private Integer totalUsed;

    @Builder
    private Points(Users user) {
        this.user = user;
        this.currentAmount = 0;
        this.totalEarned = 0;
        this.totalUsed = 0;
        this.update();
    }

    public static Points create(Users user) {
        return Points.builder().user(user).build();
    }

    public void earn(int amount) {
        requirePositive(amount);
        this.currentAmount += amount;
        this.totalEarned += amount;
        this.update();
    }

    public void use(int amount) {
        requirePositive(amount);
        if (this.currentAmount < amount) {
            throw new BusinessException(PointErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
        this.currentAmount -= amount;
        this.totalUsed += amount;
        this.update();
    }

    /**
     * 결제 취소·환불로 사용했던 적립금을 되돌린다.
     */
    public void refund(int amount) {
        requirePositive(amount);
        this.currentAmount += amount;
        this.totalUsed = Math.max(0, this.totalUsed - amount);
        this.update();
    }

    /**
     * 환불 등으로 적립을 회수한다 (지급된 적립금을 다시 빼앗는 케이스).
     */
    public void reverseEarning(int amount) {
        requirePositive(amount);
        this.currentAmount = Math.max(0, this.currentAmount - amount);
        this.totalEarned = Math.max(0, this.totalEarned - amount);
        this.update();
    }

    /**
     * 만료에 따른 차감. 보유액을 넘는 만료 요청은 보유액까지만 차감한다.
     */
    public int expire(int amount) {
        requirePositive(amount);
        int expired = Math.min(this.currentAmount, amount);
        this.currentAmount -= expired;
        this.update();
        return expired;
    }

    private void requirePositive(int amount) {
        if (amount <= 0) {
            throw new BusinessException(PointErrorCode.POINT_AMOUNT_INVALID);
        }
    }
}
