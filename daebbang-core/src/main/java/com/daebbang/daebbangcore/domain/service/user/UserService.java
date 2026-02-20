package com.daebbang.daebbangcore.domain.service.user;

import com.daebbang.daebbangcore.domain.command.user.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Users;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    Users getUser(String loginId);
}
