package com.daebbang.daebbangcore.domain.order.command;

import com.daebbang.daebbangcore.domain.order.entity.ClaimType;
import com.daebbang.daebbangcore.domain.order.entity.ReasonType;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.util.List;

public record ClaimCommand(
    Long userId,
    Long orderDetailId,
    ClaimType claimType,
    ReasonType reasonType,
    String reasonDetail,
    int quantity,
    List<UploadFile> images
) {}
