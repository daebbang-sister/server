package com.daebbang.daebbangapi.domain.user.controller;

import com.daebbang.daebbangapi.domain.user.dto.request.SmsSendRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.SmsVerifyRequest;
import com.daebbang.daebbangapi.domain.user.dto.response.SmsSendAuthCode;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.SmsService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * Send an SMS authentication code to the specified phone number.
     *
     * Ensures the provided phone number exists, generates and sends an authentication code, and returns a response wrapping the created auth code DTO.
     *
     * @param request request payload containing the target phone number
     * @return a ResponseEntity whose body is a CommonResponse containing the created SmsSendAuthCode DTO and a create success code
     */
    @PostMapping("/send")
    public ResponseEntity<@NonNull CommonResponse<SmsSendAuthCode>> generateAuthCode(
        @Valid @RequestBody SmsSendRequest request
    ) {
        userService.existsByPhoneNumber(request.phoneNumber());
        String authCode = smsService.sendAuthMessage(request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.success(CommonSuccessCode.CREATE_SUCCESS, SmsSendAuthCode.toDto(authCode)));
    }

    /**
     * Generates and sends an SMS authentication code for changing the authenticated user's phone number.
     *
     * @param userId the authenticated user's id
     * @param request request containing the target phone number
     * @return a CommonResponse wrapping an SmsSendAuthCode DTO for the generated code; response is returned with HTTP 201 (Created)
     */
    @PostMapping("/send/change")
    public ResponseEntity<@NonNull CommonResponse<SmsSendAuthCode>> generateAuthCodeForChange(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody SmsSendRequest request
    ) {
        String authCode = userService.sendChangePhoneAuthCode(userId, request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.success(CommonSuccessCode.CREATE_SUCCESS, SmsSendAuthCode.toDto(authCode)));
    }

    /**
     * Verifies an SMS authentication code submitted for a phone number.
     *
     * @param request the request containing the phone number and the submitted authentication code
     * @return a CommonResponse with no payload indicating successful verification (`UserSuccessCode.VERIFY_AUTH_CODE`)
     */
    @PostMapping("/verify")
    public ResponseEntity<@NonNull CommonResponse<Void>> verifyAuthCode(
        @Valid @RequestBody SmsVerifyRequest request
    ) {
        smsService.verifyAuthCode(request.phoneNumber(), request.authCode());
        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.VERIFY_AUTH_CODE));
    }
}
