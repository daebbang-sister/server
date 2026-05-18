package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.OrderSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSettingsRepository extends JpaRepository<OrderSettings, Long> {

    Optional<OrderSettings> findTopByOrderByIdAsc();
}
