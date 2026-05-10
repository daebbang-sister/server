package com.daebbang.daebbangcore.domain.order.entity;

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
@Table(name = "order_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer autoCompleteDays;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(int autoCompleteDays) {
        this.autoCompleteDays = autoCompleteDays;
        this.updatedAt = LocalDateTime.now();
    }
}
