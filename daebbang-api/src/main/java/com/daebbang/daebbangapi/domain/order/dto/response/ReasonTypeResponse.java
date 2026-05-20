package com.daebbang.daebbangapi.domain.order.dto.response;

import com.daebbang.daebbangcore.domain.order.entity.ReasonType;

public record ReasonTypeResponse(
    String code,
    String description
) {
    public static ReasonTypeResponse from(ReasonType reasonType) {
        return new ReasonTypeResponse(reasonType.name(), reasonType.getDescription());
    }
}
