package com.daebbang.daebbangapi.domain.users.mapper;

import com.daebbang.daebbangapi.domain.users.dto.response.UserInfo;
import com.daebbang.daebbangcommon.util.PhoneNumberUtils;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {PhoneNumberUtils.class})
public interface UserMapper {
    @Mapping(source = "users.name", target = "userName")
    @Mapping(source = "users.email", target = "userEmail")
    @Mapping(target = "userPhoneNumber", expression = "java(PhoneNumberUtils.maskPhoneNumber(users.getPhoneNumber()))")
    UserInfo toUserInfo(Users users);
}
