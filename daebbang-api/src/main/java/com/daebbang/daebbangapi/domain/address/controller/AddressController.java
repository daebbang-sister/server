package com.daebbang.daebbangapi.domain.address.controller;

import com.daebbang.daebbangapi.domain.address.dto.response.AddressInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public CommonResponse<List<AddressInfo>> getAddresses(@AuthenticationPrincipal Long userId) {
        List<AddressInfo> addresses = addressService.findAllByUserId(userId).stream()
            .map(AddressInfo::from)
            .toList();
        return CommonResponse.success(UserSuccessCode.ADDRESS_RETRIEVED, addresses);
    }
}
