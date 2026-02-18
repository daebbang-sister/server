package com.daebbang.daebbangapi.controller.user;

import com.daebbang.daebbangapi.dto.response.user.UserInfo;
import com.daebbang.daebbangapi.mapper.user.UserMapper;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/users")
public class UserController {

    private final UserMapper userMapper;

    private final UserService userService;

    @GetMapping
    public ResponseEntity<@NonNull CommonResponse<UserInfo>> getUser() {

        UserInfo info = userMapper.toUserInfo(userService.getUser(""));

        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_RETRIEVED, info));
    }
}
