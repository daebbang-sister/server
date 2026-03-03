package com.daebbang.daebbangapi.domain.users.controller;

import com.daebbang.daebbangapi.domain.users.dto.request.CheckDuplicationIdRequest;
import com.daebbang.daebbangapi.domain.users.dto.request.JoinRequest;
import com.daebbang.daebbangapi.domain.users.dto.request.PasswordResetRequest;
import com.daebbang.daebbangapi.domain.users.dto.response.UserInfo;
import com.daebbang.daebbangapi.domain.users.mapper.UserMapper;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/users")
public class UserController {

    private final UserMapper userMapper;

    private final UserService userService;

    @GetMapping
    public ResponseEntity<@NonNull CommonResponse<UserInfo>> getUser(@AuthenticationPrincipal String username) {
        UserInfo info = userMapper.toUserInfo(userService.getUser(username));
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_RETRIEVED, info));
    }

    @PostMapping
    public ResponseEntity<@NonNull CommonResponse<Void>> joinUser(@Valid @RequestBody JoinRequest joinRequest) {
        userService.join(JoinRequest.toCommand(joinRequest));
        return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(CommonResponse.success(UserSuccessCode.USER_JOINED));
    }

    @GetMapping("/check/id")
    public CommonResponse<Void> checkDuplicationLoginId(@Valid @ModelAttribute
        CheckDuplicationIdRequest request) {

        userService.existsActiveUsers(request.loginId());

        return CommonResponse.success(UserSuccessCode.USER_ID_AVAILABLE);
    }
}
