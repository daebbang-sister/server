package com.daebbang.daebbangcore.domain.review.repository;

import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewPointConfigRepository extends JpaRepository<ReviewPointConfig, Long> {

    Optional<ReviewPointConfig> findTopByOrderByIdAsc();
}
