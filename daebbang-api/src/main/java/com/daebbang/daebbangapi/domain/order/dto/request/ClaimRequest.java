package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.ClaimCommand;
import com.daebbang.daebbangcore.domain.order.entity.ClaimType;
import com.daebbang.daebbangcore.domain.order.entity.ReasonType;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClaimRequest(
    @NotNull(message = "클레임 유형은 필수입니다.")
    ClaimType claimType,

    @NotNull(message = "사유 유형은 필수입니다.")
    ReasonType reasonType,

    @Size(max = 300, message = "상세 사유는 300자 이하로 입력해주세요.")
    String reasonDetail,

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 최소 1개입니다.")
    Integer quantity
) {
    public ClaimCommand toCommand(Long userId, Long orderDetailId, List<UploadFile> images) {
        return new ClaimCommand(userId, orderDetailId, claimType, reasonType, reasonDetail, quantity, images);
    }
}
