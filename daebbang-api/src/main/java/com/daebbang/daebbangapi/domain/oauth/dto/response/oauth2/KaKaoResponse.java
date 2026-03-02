package com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import java.util.Map;
import java.util.Optional;

public class KaKaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;

    @SuppressWarnings("unchecked")
    public KaKaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public String getUseEmail() {
        return kakaoAccount.get("userEmail").toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getUsername() {
        return Optional.ofNullable(kakaoAccount)
            .map(account -> (Map<String, Object>) account.get("profile"))
            .map(profile -> profile.get("nickname").toString())
            .orElse("Unknown");
    }

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getPhoneNumber() {
        return Optional.ofNullable(kakaoAccount)
            .map(account -> account.get("phone_number"))
            .map(Object::toString)
            .orElse("");
    }

    @Override
    public String getAttributeKeyName() {
        return "id";
    }
}
