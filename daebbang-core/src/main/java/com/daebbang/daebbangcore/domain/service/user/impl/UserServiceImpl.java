package com.daebbang.daebbangcore.domain.service.user.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.command.user.PasswordPort;
import com.daebbang.daebbangcore.domain.command.user.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import com.daebbang.daebbangcore.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordPort passwordPort;

    private final UsersRepository userRepository;

    @Override
    public void join(UserJoinCommand joinCommand) {
        if (existsUsers(joinCommand.loginId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }
        // TODO : 휴대폰 인증 및 사용중인 이메일도 체크 해줘야함
        String encoded = passwordPort.encode(joinCommand.password());

        Users joinUser = UserJoinCommand.toEntity(joinCommand, encoded);
        userRepository.save(joinUser);
    }

    @Override
    public Users getUser(String loginId) {
        return findActiveUserByLoginId(loginId);
    }

    private Users findActiveUserByLoginId(String loginId) {
        return userRepository.findActiveUserByLoginId(loginId, UserStatus.WITHDRAWN)
                                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private boolean existsUsers(String loginId) {
        return userRepository.existsActiveUser(loginId, UserStatus.WITHDRAWN);
    }
}
