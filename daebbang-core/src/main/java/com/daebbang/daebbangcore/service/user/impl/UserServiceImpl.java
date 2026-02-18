package com.daebbang.daebbangcore.service.user.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import com.daebbang.daebbangcore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository userRepository;

    @Override
    public Users getUser(String loginId) {
        return findActiveUserByLoginId(loginId);
    }

    private Users findActiveUserByLoginId(String loginId) {
        return userRepository.findActiveUserByLoginId(loginId, UserStatus.WITHDRAWN)
                                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
