package com.daebbang.daebbangcore.domain.user.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import com.daebbang.daebbangcore.infra.converter.UserStatusConverter;
import jakarta.persistence.Column;
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
public class Users extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Convert(converter = UserStatusConverter.class)
    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private UserStatus status;

    @Column(nullable = false, length = 16)
    private String loginId;

    @Column(length = 255)
    private String loginPwd;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    private LocalDateTime lastLoginAt;

    @Builder
    private Users(Provider provider, UserStatus status, String loginId, String loginPwd, String name, String email, String phoneNumber) {
        this.provider = provider;
        this.status = status;
        this.loginId = loginId;
        this.loginPwd = loginPwd;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.lastLoginAt = null;
    }

    public static Users createLocalUser(String loginId, String loginPwd, String name, String email, String phoneNumber) {
        return Users.builder()
            .provider(Provider.LOCAL)
            .status(UserStatus.ACTIVE)
            .loginId(loginId)
            .loginPwd(loginPwd)
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();
    }

    public static Users createSocialUser(Provider provider, String providerId, String name, String email, String phoneNumber) {
        return Users.builder()
            .provider(provider)
            .status(UserStatus.ACTIVE)
            .loginId(providerId)
            .loginPwd(null)
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();
    }

    public void updateSocialInfo(String name, String email, String phoneNumber) {
        if (!this.name.equalsIgnoreCase(name)) {
            this.name = name;
        }
        if (!this.email.equalsIgnoreCase(email)) {
            this.email = email;
        }
        if (!this.phoneNumber.equalsIgnoreCase(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * Update the user's login password.
     *
     * @param newPwd the new login password to set
     */
    public void updatePassword(String newPwd) {
        this.loginPwd = newPwd;
    }

    /**
     * Updates the user's email address.
     *
     * @param newEmail the new email address to assign to the user
     */
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    /**
     * Updates the user's phone number to the provided value.
     *
     * @param newPhoneNumber the new phone number to set
     */
    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }

    /**
     * Determines whether this user was created via the local authentication provider.
     *
     * @return {@code true} if the user's provider is {@code Provider.LOCAL}, {@code false} otherwise.
     */
    public boolean isLocal() {
        return this.provider == Provider.LOCAL;
    }

    /**
     * Marks the user as withdrawn.
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }
}
