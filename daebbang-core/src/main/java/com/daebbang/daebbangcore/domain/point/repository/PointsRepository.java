package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.Points;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends JpaRepository<@NonNull Points, @NonNull Long> {

    Optional<Points> findByUserIdAndDeletedAtIsNull(Long userId);
}
