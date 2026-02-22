package com.daebbang.daebbangapi.domain.oauth.provider;

import com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2.OAuth2Response;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import java.util.Map;

public interface OAuth2ResponseProvider {
    boolean supports(Provider provider);
    OAuth2Response create(Map<String, Object> attributes);
}
