package com.daebbang.daebbangapi.mapper.user;

import com.daebbang.daebbangapi.dto.response.user.UserInfo;
import com.daebbang.daebbangcommon.util.PhoneNumberUtils;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {PhoneNumberUtils.class})
public interface UserMapper {
    @Mapping(source = "users.name", target = "userName")
    @Mapping(target = "userPhoneNumber", expression = "java(PhoneNumberUtils.maskPhoneNumber(users.getPhoneNumber()))")
    UserInfo toUserInfo(Users users);
}
