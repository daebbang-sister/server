package com.daebbang.daebbangcore.domain.point.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private PolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private AmountType amountType;

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal value;

    @Column(name = "expiration_days")
    private Integer expirationDays;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    private PointPolicy(PolicyType policyType, AmountType amountType, BigDecimal value,
        Integer expirationDays, String name, boolean isActive) {
        this.policyType = policyType;
        this.amountType = amountType;
        this.value = value;
        this.expirationDays = expirationDays;
        this.name = name;
        this.isActive = isActive;
        this.update();
    }

    public static PointPolicy create(PolicyType policyType, AmountType amountType,
        BigDecimal value, Integer expirationDays, String name, boolean isActive) {
        return PointPolicy.builder()
            .policyType(policyType)
            .amountType(amountType)
            .value(value)
            .expirationDays(expirationDays)
            .name(name)
            .isActive(isActive)
            .build();
    }

    /**
     * 정책에 따라 지급할 적립금을 계산. baseAmount는 정률(RATE)일 때만 사용 (구매금액 등).
     * 정액이면 value를 그대로 정수로 사용한다.
     */
    public int calculateEarnAmount(int baseAmount) {
        if (this.amountType == AmountType.FIXED) {
            return value.setScale(0, RoundingMode.DOWN).intValueExact();
        }
        return BigDecimal.valueOf(baseAmount)
            .multiply(value)
            .setScale(0, RoundingMode.DOWN)
            .intValueExact();
    }

    /**
     * 적립 시점 기준의 소멸 일자. expirationDays가 NULL이면 무기한이라 NULL 반환.
     */
    public LocalDateTime resolveExpiredAt(LocalDateTime now) {
        if (expirationDays == null) {
            return null;
        }
        return now.plusDays(expirationDays);
    }
}
