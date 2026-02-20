package com.daebbang.daebbangcore.service.user;

import com.daebbang.daebbangcore.command.user.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Users;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    Users getUser(String loginId);
}
