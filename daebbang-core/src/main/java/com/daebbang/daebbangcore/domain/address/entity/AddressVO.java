package com.daebbang.daebbangcore.domain.address.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressVO {

    @Column(length = 6, nullable = false)
    private String zipCode;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(length = 255, nullable = false)
    private String detailAddress;

    private AddressVO(String zipCode, String address, String detailAddress) {
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public static AddressVO of(String zipCode, String address, String detailAddress) {
        return new AddressVO(zipCode, address, detailAddress);
    }
}
