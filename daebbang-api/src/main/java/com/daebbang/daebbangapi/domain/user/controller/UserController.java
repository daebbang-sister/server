package com.daebbang.daebbangapi.domain.user.controller;

import com.daebbang.daebbangapi.domain.user.dto.request.CheckDuplicationIdRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.JoinRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.MyInfoUpdateRequest;
import com.daebbang.daebbangapi.domain.user.dto.response.MyInfoEdit;
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
import org.springframework.web.bind.annotation.PatchMapping;
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

    /**
     * Retrieve the authenticated user's profile information, including their default address when present.
     *
     * @param userId the authenticated user's ID extracted from the security context
     * @return a CommonResponse containing a UserInfo DTO with the user's profile and default address; marked with success code {@code USER_RETRIEVED}
     */
    @GetMapping
    public ResponseEntity<@NonNull CommonResponse<UserInfo>> getUser(@AuthenticationPrincipal Long userId) {
        Users user = userService.getUserById(userId);
        Address defaultAddress = addressService.findDefaultByUserId(userId).orElse(null);
        UserInfo info = UserInfo.from(user, defaultAddress);
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_RETRIEVED, info));
    }

    /**
     * Retrieve the authenticated user's editable profile information.
     *
     * @param userId the authenticated user's ID extracted from the security principal
     * @return a ResponseEntity containing a successful CommonResponse with a MyInfoEdit DTO and HTTP status 200 (OK)
     */
    @GetMapping("/me/edit")
    public ResponseEntity<@NonNull CommonResponse<MyInfoEdit>> getMyInfoForEdit(@AuthenticationPrincipal Long userId) {
        Users user = userService.getUserById(userId);
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_RETRIEVED, MyInfoEdit.from(user)));
    }

    /**
     * Updates the authenticated user's profile with the provided data.
     *
     * @param userId the authenticated user's id
     * @param request the update payload containing new profile values
     * @return a CommonResponse with no content indicating the user was updated ({@code UserSuccessCode.USER_UPDATED})
     */
    @PatchMapping("/me")
    public ResponseEntity<@NonNull CommonResponse<Void>> updateMyInfo(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody MyInfoUpdateRequest request
    ) {
        userService.updateMyInfo(userId, request.toCommand());
        return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(CommonResponse.success(UserSuccessCode.USER_UPDATED));
    }

    /**
     * Withdraws (deletes) the authenticated user's account.
     *
     * @param userId the authenticated user's ID extracted from the security context
     * @return a ResponseEntity containing a CommonResponse<Void> with success code USER_WITHDRAWN and HTTP status 200 OK
     */
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
