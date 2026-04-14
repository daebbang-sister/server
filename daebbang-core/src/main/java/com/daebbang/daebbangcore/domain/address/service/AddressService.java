package com.daebbang.daebbangcore.domain.address.service;

import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;

public interface AddressService {

    void save(Users user, AddressCommand command);

    void deleteAllByUserId(Long userId);

    List<Address> findAllByUserId(Long userId);

    Optional<Address> findDefaultByUserId(Long userId);
}
