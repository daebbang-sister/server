package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.dsl.UserPointHistoryCustomRepository;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointHistoryRepository
    extends JpaRepository<@NonNull UserPointHistory, @NonNull Long>, UserPointHistoryCustomRepository {

}
