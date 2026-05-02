package com.daebbang.daebbangapi.domain.user.controller;

import com.daebbang.daebbangapi.domain.user.dto.request.CheckDuplicationIdRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.JoinRequest;
import com.daebbang.daebbangapi.domain.user.dto.response.UserInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/users")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<@NonNull CommonResponse<UserInfo>> getUser(@AuthenticationPrincipal Long userId) {
        Users user = userService.getUserById(userId);
        Address defaultAddress = addressService.findDefaultByUserId(userId).orElse(null);
        UserInfo info = UserInfo.from(user, defaultAddress);
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_RETRIEVED, info));
    }

    @DeleteMapping
    public ResponseEntity<@NonNull CommonResponse<Void>> withdrawUser(@AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_WITHDRAWN));
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
