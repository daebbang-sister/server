package com.daebbang.daebbangapi.domain.address.dto.response;

import com.daebbang.daebbangcore.domain.address.entity.Address;

public record AddressInfo(
    Long addressId,
    String alias,
    String receiver,
    String receiverPhoneNumber,
    String zipCode,
    String address,
    String detailAddress,
    boolean isDefault
) {
    public static AddressInfo from(Address address) {
        return new AddressInfo(
            address.getId(),
            address.getAlias(),
            address.getReceiver(),
            address.getReceiverPhoneNumber(),
            address.getAddressVO().getZipCode(),
            address.getAddressVO().getAddress(),
            address.getAddressVO().getDetailAddress(),
            address.isDefault()
        );
    }
}
