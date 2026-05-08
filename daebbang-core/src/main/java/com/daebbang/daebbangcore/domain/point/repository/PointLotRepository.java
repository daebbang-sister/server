package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.PointLot;
import com.daebbang.daebbangcore.domain.point.repository.dsl.PointLotCustomRepository;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointLotRepository
    extends JpaRepository<@NonNull PointLot, @NonNull Long>, PointLotCustomRepository {
}
