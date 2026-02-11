package com.daebbang.daebbangcore.domain.user.entity;

import com.daebbang.daebbangcore.infra.converter.UserStatusConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Provider provider;
    @Convert(converter = UserStatusConverter.class)
    private UserStatus status;
    private String loginId;
    private String loginPwd;
    private String name;
    private String phoneNumber;
    private LocalDateTime lastLoginAt;

    @Builder
    private Users(Provider provider, UserStatus status, String loginId, String loginPwd, String name, String phoneNumber) {
        this.provider = provider;
        this.status = status;
        this.loginId = loginId;
        this.loginPwd = loginPwd;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.lastLoginAt = null;
    }

    public static Users createLocalUser(String loginId, String loginPwd, String name, String phoneNumber) {
        return Users.builder()
            .provider(Provider.LOCAL)
            .status(UserStatus.ACTIVE)
            .loginId(loginId)
            .loginPwd(loginPwd)
            .name(name)
            .phoneNumber(phoneNumber)
            .build();
    }

    public static Users createSocialUser(Provider provider, String loginId, String name, String phoneNumber) {
        return Users.builder()
            .provider(provider)
            .status(UserStatus.ACTIVE)
            .loginId(loginId)
            .loginPwd(null)
            .name(name)
            .phoneNumber(phoneNumber)
            .build();
    }
}
