package com.daebbang.daebbangcore.infra.toss.dto;

public record TossConfirmRequest(
    String orderId,
    String paymentKey,
    int amount
) {}
