package com.daebbang.daebbangapi.domain.address.controller;

import com.daebbang.daebbangapi.domain.address.dto.request.AddressSaveRequest;
import com.daebbang.daebbangapi.domain.address.dto.request.AddressUpdateRequest;
import com.daebbang.daebbangapi.domain.address.dto.response.AddressInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/addresses")
public class AddressController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping
    public CommonResponse<List<AddressInfo>> getAddresses(@AuthenticationPrincipal Long userId) {
        List<AddressInfo> addresses = addressService.findAllByUserId(userId).stream()
            .map(AddressInfo::from)
            .toList();
        return CommonResponse.success(UserSuccessCode.ADDRESS_RETRIEVED, addresses);
    }

    @PostMapping
    public ResponseEntity<@NonNull CommonResponse<Void>> addAddress(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody AddressSaveRequest request
    ) {
        Users user = userService.getUserById(userId);
        addressService.save(user, request.toCommand());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonResponse.success(UserSuccessCode.ADDRESS_SAVED));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<@NonNull CommonResponse<Void>> updateAddress(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long addressId,
        @Valid @RequestBody AddressUpdateRequest request
    ) {
        addressService.update(userId, addressId, request.toCommand());
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.ADDRESS_UPDATED));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<@NonNull CommonResponse<Void>> deleteAddress(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long addressId
    ) {
        addressService.deleteById(userId, addressId);
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.ADDRESS_DELETED));
    }
}
