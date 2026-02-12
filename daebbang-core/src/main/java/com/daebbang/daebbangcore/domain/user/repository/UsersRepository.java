package com.daebbang.daebbangcore.domain.user.repository;

import com.daebbang.daebbangcore.domain.user.entity.Users;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<@NonNull Users,@NonNull Long> {

}
