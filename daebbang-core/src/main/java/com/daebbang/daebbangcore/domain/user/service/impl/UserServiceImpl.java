package com.daebbang.daebbangcore.domain.user.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcommon.error.SmsErrorCode;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.command.PasswordPort;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.event.UserJoinEvent;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.SmsService;
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
    public void verifyUserActiveToFindPassword(String username, String userId, String email) {
        if (!userRepository.existsActiveUser(username, userId, email, UserStatus.WITHDRAWN)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
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

    @Override
    public List<Users> getUsersByFindLoginId(String username, String email) {
        return userRepository.findActiveUserIdsByUsernameAndEmail(username, email, UserStatus.WITHDRAWN);
    }

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
