package com.daebbang.daebbangcore.domain.address.command;

public record AddressCommand(
    String alias,
    String zipCode,
    String address,
    String detailAddress,
    boolean isDefault
) {

}
