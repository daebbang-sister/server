package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcore.domain.order.command.ClaimCommand;
import com.daebbang.daebbangcore.domain.order.entity.Claims;

public interface ClaimService {

    Claims createClaim(ClaimCommand command);
}
