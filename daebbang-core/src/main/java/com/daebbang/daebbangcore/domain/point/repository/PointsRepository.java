package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.repository.dsl.PointsCustomRepository;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends JpaRepository<@NonNull Points, @NonNull Long>,
    PointsCustomRepository {

    Optional<Points> findByUserIdAndDeletedAtIsNull(Long userId);
}
