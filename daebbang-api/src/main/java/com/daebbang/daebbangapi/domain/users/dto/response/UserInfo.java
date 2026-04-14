package com.daebbang.daebbangapi.domain.users.dto.response;

import com.daebbang.daebbangcommon.util.PhoneNumberUtils;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.time.LocalDateTime;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UserInfo(
    Long id,
    Provider provider,
    String loginId,
    String userName,
    String userEmail,
    String userPhoneNumber,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    @Nullable DefaultAddressInfo defaultAddress
) {

    public record DefaultAddressInfo(
        Long addressId,
        String alias,
        String receiver,
        String receiverPhoneNumber,
        String zipCode,
        String address,
        String detailAddress
    ) {
        public static DefaultAddressInfo from(Address address) {
            return new DefaultAddressInfo(
                address.getId(),
                address.getAlias(),
                address.getReceiver(),
                address.getReceiverPhoneNumber(),
                address.getAddressVO().getZipCode(),
                address.getAddressVO().getAddress(),
                address.getAddressVO().getDetailAddress()
            );
        }
    }

    public static UserInfo from(Users user, @Nullable Address defaultAddress) {
        return UserInfo.builder()
            .id(user.getId())
            .provider(user.getProvider())
            .loginId(user.getLoginId())
            .userName(user.getName())
            .userEmail(user.getEmail())
            .userPhoneNumber(PhoneNumberUtils.maskPhoneNumber(user.getPhoneNumber()))
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .defaultAddress(defaultAddress != null ? DefaultAddressInfo.from(defaultAddress) : null)
            .build();
    }
}
