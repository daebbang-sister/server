package com.daebbang.daebbangcore.domain.address.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(length = 50, nullable = false)
    private String receiver;

    @Column(length = 50, nullable = false)
    private String receiverPhoneNumber;

    @Column(length = 50)
    private String alias;

    @Embedded
    private AddressVO addressVO;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isDefault;

    @Builder
    private Address(Users user, String receiver, String receiverPhoneNumber, String alias,
        AddressVO addressVO, boolean isDefault) {
        this.user = user;
        this.receiver = receiver;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.alias = alias;
        this.addressVO = addressVO;
        this.isDefault = isDefault;
    }

    public static Address create(Users user, String receiver, String receiverPhoneNumber,
        String alias, AddressVO addressVO, boolean isDefault) {
        return Address.builder()
            .user(user)
            .receiver(receiver)
            .receiverPhoneNumber(receiverPhoneNumber)
            .alias(alias)
            .addressVO(addressVO)
            .isDefault(isDefault)
            .build();
    }

    public void unsetDefault() {
        this.isDefault = false;
    }
}
