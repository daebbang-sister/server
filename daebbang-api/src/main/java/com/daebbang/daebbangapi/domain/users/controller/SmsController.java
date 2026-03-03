package com.daebbang.daebbangapi.domain.users.controller;

import com.daebbang.daebbangapi.domain.users.dto.request.SmsSendRequest;
import com.daebbang.daebbangapi.domain.users.dto.request.SmsVerifyRequest;
import com.daebbang.daebbangapi.domain.users.dto.response.SmsSendAuthCode;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.SmsService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/sms")
public class SmsController {

    private final SmsService smsService;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<@NonNull CommonResponse<SmsSendAuthCode>> generateAuthCode(
        @Valid @RequestBody SmsSendRequest request
    ) {
        userService.existsByPhoneNumber(request.phoneNumber());
        String authCode = smsService.sendAuthMessage(request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.success(CommonSuccessCode.CREATE_SUCCESS, SmsSendAuthCode.toDto(authCode)));
    }

    @PostMapping("/verify")
    public ResponseEntity<@NonNull CommonResponse<Void>> verifyAuthCode(
        @Valid @RequestBody SmsVerifyRequest request
    ) {
        smsService.verifyAuthCode(request.phoneNumber(), request.authCode());
        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.VERIFY_AUTH_CODE));
    }
}
