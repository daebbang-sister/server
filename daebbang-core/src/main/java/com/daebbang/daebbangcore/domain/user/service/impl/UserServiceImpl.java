package com.daebbang.daebbangcore.domain.user.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcommon.error.SmsErrorCode;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.command.MyInfoUpdateCommand;
import com.daebbang.daebbangcore.domain.user.command.PasswordPort;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.event.UserJoinEvent;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.EmailService;
import com.daebbang.daebbangcore.infra.service.SmsService;
import com.daebbang.daebbangcore.infra.util.EmailUtils;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final PasswordPort passwordPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UsersRepository userRepository;
    private final SmsService smsService;
    private final EmailService emailService;
    private final AddressService addressService;

    @Override
    @Transactional
    public void join(UserJoinCommand joinCommand) {
        if (existsUsers(joinCommand.loginId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (!smsService.isVerified(joinCommand.phoneNumber())) {
            throw new BusinessException(SmsErrorCode.AUTH_CODE_EXPIRED);
        }
        String encoded = passwordPort.encode(joinCommand.password());

        Users joinUser = UserJoinCommand.toEntity(joinCommand, encoded);
        userRepository.save(joinUser);

        if (joinCommand.address() != null) {
            addressService.save(joinUser, joinCommand.address());
        }

        eventPublisher.publishEvent(UserJoinEvent.toEvent(joinCommand.phoneNumber()));
    }

    @Override
    @Transactional
    public void joinOrUpdateSocial(Provider provider, String providerId, String email, String name, String phoneNumber) {

        findActiveSocialUsersByLoginId(providerId, provider)
            .ifPresentOrElse(
                user -> user.updateSocialInfo(name, email, phoneNumber),
                () -> {
                    Users socialUser = Users.createSocialUser(provider, providerId, name, email, phoneNumber);
                    userRepository.save(socialUser);
                }
            );
    }

    @Override
    public void existsByPhoneNumber(String phoneNumber) {
        if (userRepository.existsPhoneNumber(phoneNumber, UserStatus.WITHDRAWN)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }

    @Override
    public void existsActiveUsers(String loginId) {
        if (userRepository.existsActiveUser(loginId, UserStatus.WITHDRAWN)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

    @Override
    @Transactional
    public void issueTemporaryPassword(String username, String userId, String email) {
        Users user = userRepository.findActiveUser(username, userId, email, UserStatus.WITHDRAWN)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String temporaryPassword = EmailUtils.generateTemporaryPassword();
        user.updatePassword(passwordPort.encode(temporaryPassword));

        emailService.sendTemporaryPassword(email, temporaryPassword);
    }

    @Override
    public Users getUser(String loginId) {
        return findActiveLocalUserByLoginId(loginId);
    }

    @Override
    public Users getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * Retrieve active users that match the given username and email for account lookup.
     *
     * @param username the username to match
     * @param email the email to match
     * @return a list of active Users that match the provided username and email (excludes withdrawn users)
     */
    @Override
    public List<Users> getUsersByFindLoginId(String username, String email) {
        return userRepository.findActiveUserIdsByUsernameAndEmail(username, email, UserStatus.WITHDRAWN);
    }

    /**
     * Sends an SMS authentication code to verify a requested phone number change.
     *
     * @param userId the id of the user requesting the phone number change
     * @param newPhoneNumber the new phone number to verify
     * @return the authentication code or message identifier returned by the SMS service
     * @throws BusinessException if the new phone number is identical to the user's current number (UserErrorCode.SAME_PHONE_NUMBER)
     * @throws BusinessException if the new phone number is already used by another active user (UserErrorCode.DUPLICATE_PHONE_NUMBER)
     */
    @Override
    public String sendChangePhoneAuthCode(Long userId, String newPhoneNumber) {
        Users user = getUserById(userId);
        if (newPhoneNumber.equals(user.getPhoneNumber())) {
            throw new BusinessException(UserErrorCode.SAME_PHONE_NUMBER);
        }
        if (userRepository.existsPhoneNumberExcludingSelf(newPhoneNumber, userId, UserStatus.WITHDRAWN)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_PHONE_NUMBER);
        }
        return smsService.sendAuthMessage(newPhoneNumber);
    }

    /**
     * Updates the authenticated user's profile fields (password, phone number, email) according to the provided command.
     *
     * Password update: only allowed for local users, requires password and confirmation to match, and the password is encoded before saving.
     * Phone number update: only applied when different from current number; requires the new number to be unique (excluding the current user) and SMS verification, and removes the verification record after update.
     * Email update: applied when different from the current email.
     *
     * @param userId  the id of the user to update
     * @param command the update command containing optional password, phone number, and email changes
     * @throws BusinessException if attempting to set a password for a social user (UserErrorCode.SOCIAL_PASSWORD_NOT_ALLOWED), if password confirmation does not match (UserErrorCode.PASSWORD_CONFIRM_MISMATCH), or if the new phone number is already used by another active user (UserErrorCode.DUPLICATE_PHONE_NUMBER)
     * @throws BusinessException if SMS verification for the new phone number is missing or expired (SmsErrorCode.AUTH_CODE_EXPIRED)
     */
    @Override
    @Transactional
    public void updateMyInfo(Long userId, MyInfoUpdateCommand command) {
        Users user = getUserById(userId);

        if (command.hasPassword()) {
            if (!user.isLocal()) {
                throw new BusinessException(UserErrorCode.SOCIAL_PASSWORD_NOT_ALLOWED);
            }
            if (!command.password().equals(command.passwordConfirm())) {
                throw new BusinessException(UserErrorCode.PASSWORD_CONFIRM_MISMATCH);
            }
            user.updatePassword(passwordPort.encode(command.password()));
        }

        if (command.hasPhoneNumber() && !command.phoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsPhoneNumberExcludingSelf(command.phoneNumber(), userId, UserStatus.WITHDRAWN)) {
                throw new BusinessException(UserErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            if (!smsService.isVerified(command.phoneNumber())) {
                throw new BusinessException(SmsErrorCode.AUTH_CODE_EXPIRED);
            }
            user.updatePhoneNumber(command.phoneNumber());
            smsService.deleteVerification(command.phoneNumber());
        }

        if (command.hasEmail() && !command.email().equals(user.getEmail())) {
            user.updateEmail(command.email());
        }
    }

    /**
     * Marks the specified user as withdrawn and removes all addresses associated with the user.
     *
     * @param userId the identifier of the user to withdraw
     */
    @Override
    @Transactional
    public void withdraw(Long userId) {
        Users user = getUserById(userId);
        user.withdraw();
        addressService.deleteAllByUserId(userId);
    }

    private Users findActiveLocalUserByLoginId(String loginId) {
        return userRepository.findActiveUserByLoginId(loginId, UserStatus.WITHDRAWN)
                                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private boolean existsUsers(String loginId) {
        return userRepository.existsActiveUser(loginId, UserStatus.WITHDRAWN);
    }

    private Optional<Users> findActiveSocialUsersByLoginId(String loginId, Provider provider) {
        return userRepository.findActiveUserByLoginIdAndProvider(loginId, UserStatus.WITHDRAWN, provider);
    }
}
