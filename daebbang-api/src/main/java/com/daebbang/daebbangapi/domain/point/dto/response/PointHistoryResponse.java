package com.daebbang.daebbangapi.domain.point.dto.response;

import com.daebbang.daebbangcore.domain.point.entity.ChangeType;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import java.time.LocalDateTime;

public record PointHistoryResponse(
    Long id,
    LocalDateTime createdAt,
    ChangeType changeType,
    String changeTypeDescription,
    boolean earn,
    String policyName,
    Long referenceId,
    int changeAmount,
    int pointAmount,
    String description,
    LocalDateTime expiredAt
) {
    public static PointHistoryResponse from(UserPointHistory history) {
        PointPolicy policy = history.getPointPolicy();
        ChangeType type = history.getChangeType();
        return new PointHistoryResponse(
            history.getId(),
            history.getCreatedAt(),
            type,
            type.getDescription(),
            type.isEarn(),
            policy != null ? policy.getName() : null,
            history.getReferenceId(),
            history.getChangeAmount(),
            history.getPointAmount(),
            history.getDescription(),
            history.getExpiredAt()
        );
    }
}
