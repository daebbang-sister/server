package com.daebbang.daebbangapi.domain.user.controller;

import com.daebbang.daebbangapi.domain.user.dto.request.UserIdFindRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.UserPasswordFindRequest;
import com.daebbang.daebbangapi.domain.user.dto.response.UserIdFind;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/users/find")
public class UserInfoController {

    private final UserService userService;

    @GetMapping("/id")
    public CommonResponse<UserIdFind> findUserIdListByUsernameAndEmail(
        @ModelAttribute @Valid UserIdFindRequest request
    ) {
        UserIdFind ids = UserIdFind.toDto(userService.getUsersByFindLoginId(request.username(), request.userEmail()));
        return CommonResponse.success(UserSuccessCode.USER_RETRIEVED,ids);
    }

    @PostMapping("/password")
    public ResponseEntity<@NonNull CommonResponse<Void>> findUserPasswordByUserInfo(
        @Valid @RequestBody UserPasswordFindRequest request
    ) {
        userService.issueTemporaryPassword(request.username(), request.userId(), request.userEmail());
        return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(CommonResponse.success(UserSuccessCode.SEND_EMAIL));
    }
}
