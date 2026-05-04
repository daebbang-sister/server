package com.daebbang.daebbangcore.domain.user.service;

import com.daebbang.daebbangcore.domain.user.command.MyInfoUpdateCommand;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    void joinOrUpdateSocial(Provider provider, String providerId, String name, String email, String phoneNumber);
    void existsByPhoneNumber(String phoneNumber);
    void existsActiveUsers(String loginId);
    void sendChangePhoneAuthCode(Long userId, String newPhoneNumber);
    void issueTemporaryPassword(String username, String userId, String email);
    Users getUser(String loginId);
    Users getUserById(Long userId);
    List<Users> getUsersByFindLoginId(String username, String email);
    void updateMyInfo(Long userId, MyInfoUpdateCommand command);
    void withdraw(Long userId);
}
