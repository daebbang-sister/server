package com.daebbang.daebbangapi.dto.response.oauth2;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import java.util.Map;

public class KaKaoResponse implements Oauth2Response {

    private final Map<String, Object> attributes;

    public KaKaoResponse(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getUseEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getUsername() {
        return attributes.get("name").toString();
    }

    @Override
    public String getProvider() {
        return Provider.KAKAO.getDescription();
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }
}
