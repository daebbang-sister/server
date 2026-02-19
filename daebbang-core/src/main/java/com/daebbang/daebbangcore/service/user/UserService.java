package com.daebbang.daebbangcore.service.user;

import com.daebbang.daebbangcore.domain.user.entity.Users;

public interface UserService {
    Users getUser(String loginId);
}
