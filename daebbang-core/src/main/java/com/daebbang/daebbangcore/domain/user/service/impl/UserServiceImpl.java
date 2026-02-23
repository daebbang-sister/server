package com.daebbang.daebbangcore.domain.user.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.user.command.PasswordPort;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.SmsService;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final PasswordPort passwordPort;

    private final UsersRepository userRepository;

    private final SmsService smsService;

    @Override
    @Transactional
    public void join(UserJoinCommand joinCommand) {
        if (existsUsers(joinCommand.loginId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }
        // TODO : 휴대폰 인증 및 사용중인 이메일도 체크 해줘야함
        String encoded = passwordPort.encode(joinCommand.password());

        smsService.sendAuthMessage(joinCommand.phoneNumber());

        Users joinUser = UserJoinCommand.toEntity(joinCommand, encoded);
        userRepository.save(joinUser);
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
    public Users getUser(String loginId) {
        return findActiveLocalUserByLoginId(loginId);
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
