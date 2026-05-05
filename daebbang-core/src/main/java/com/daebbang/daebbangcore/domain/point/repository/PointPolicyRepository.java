package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointPolicyRepository extends JpaRepository<@NonNull PointPolicy, @NonNull Long> {

    Optional<PointPolicy> findFirstByPolicyTypeAndIsActiveTrueAndDeletedAtIsNull(PolicyType policyType);
}
