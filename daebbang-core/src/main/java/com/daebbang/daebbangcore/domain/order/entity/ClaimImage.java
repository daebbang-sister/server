package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClaimImage extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claims claim;

    @Column(length = 500, nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer imageOrder;

    @Builder
    private ClaimImage(Claims claim, String imageUrl, Integer imageOrder) {
        this.claim = claim;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static ClaimImage create(Claims claim, String imageUrl, int imageOrder) {
        return ClaimImage.builder()
            .claim(claim)
            .imageUrl(imageUrl)
            .imageOrder(imageOrder)
            .build();
    }
}
