package com.daebbang.daebbangcore.domain.order.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSessionItem {

    private Long productDetailId;
    private int quantity;
    private int originalPrice;
    private int discountRate;
    /** 할인 적용된 금액 × 수량 */
    private int discountPrice;
}
