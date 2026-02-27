package com.daebbang.daebbangapi.domain.users.dto.response;

import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;
import java.util.Objects;

public record UserIdFind(
    List<IdInfo> userIds
) {
    public static UserIdFind toDto(List<Users> users) {
        if (Objects.isNull(users) || users.isEmpty()) {
            return new UserIdFind(List.of());
        }

        List<IdInfo> maskedIdInfos = users.stream()
                                        .map(info -> IdInfo.ofMasked(info.getLoginId(), info.getProvider()))
                                        .toList();
        return new UserIdFind(maskedIdInfos);
    }
}
