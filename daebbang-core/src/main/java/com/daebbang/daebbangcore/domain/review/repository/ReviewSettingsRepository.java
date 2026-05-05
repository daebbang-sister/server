package com.daebbang.daebbangcore.domain.review.repository;

import com.daebbang.daebbangcore.domain.review.entity.ReviewSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewSettingsRepository extends JpaRepository<ReviewSettings, Long> {

    Optional<ReviewSettings> findTopByOrderByIdAsc();
}
