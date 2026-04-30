package com.daebbang.daebbangcore.domain.address.command;

public record AddressCommand(
    String receiver,
    String receiverPhoneNumber,
    String alias,
    String zipCode,
    String address,
    String detailAddress,
    boolean isDefault
) {

}
