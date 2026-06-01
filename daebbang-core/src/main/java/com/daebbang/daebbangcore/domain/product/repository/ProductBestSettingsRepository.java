package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductBestSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBestSettingsRepository extends JpaRepository<ProductBestSettings, Long> {

    Optional<ProductBestSettings> findTopByOrderByIdAsc();
}
