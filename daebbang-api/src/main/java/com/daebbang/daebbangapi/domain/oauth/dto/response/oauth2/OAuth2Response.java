package com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2;

import com.daebbang.daebbangcore.domain.user.entity.Provider;

public interface OAuth2Response {
    String getUseEmail();
    String getUsername();
    Provider getProvider();
    String getProviderId();
    String getPhoneNumber();
    String getAttributeKeyName();
}
