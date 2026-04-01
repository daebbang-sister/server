package com.daebbang.daebbangapi.domain.cart.dto.response;

import java.util.List;

public record CartPageResponse(
    List<UserCartInfo> carts,
    Long nextCursor,
    boolean hasNext
) {

}
