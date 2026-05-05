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
@Table(name = "review_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer autoApproveDays;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(int autoApproveDays) {
        this.autoApproveDays = autoApproveDays;
        this.updatedAt = LocalDateTime.now();
    }
}
