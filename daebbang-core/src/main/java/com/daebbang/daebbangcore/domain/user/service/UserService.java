package com.daebbang.daebbangcore.domain.user.service;

import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    void joinOrUpdateSocial(Provider provider, String providerId, String name, String email, String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsActiveUsers(String username, String userId, String email);
    Users getUser(String loginId);
    List<Users> getUsersByFindLoginId(String username, String email);
}
