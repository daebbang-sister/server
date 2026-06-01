package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductStats;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStatsRepository extends JpaRepository<@NonNull ProductStats, @NonNull Long> {

    Optional<ProductStats> findByProductId(Long productId);
}
