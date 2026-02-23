package com.daebbang.daebbangcore.domain.user.factory;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import java.util.Map;

public interface OAuth2ResponseFactory {
    Object createResponse(Provider provider, Map<String, Object> attributes);
}
