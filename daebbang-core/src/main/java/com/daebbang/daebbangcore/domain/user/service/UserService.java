package com.daebbang.daebbangcore.domain.user.service;

import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    void joinOrUpdateSocial(Provider provider, String providerId, String name, String email, String phoneNumber);
    Users getUser(String loginId);
}
