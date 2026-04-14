package com.daebbang.daebbangcore.domain.address.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.address.entity.AddressVO;
import com.daebbang.daebbangcore.domain.address.repository.AddressRepository;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public void save(Users user, AddressCommand command) {
        if (addressRepository.countActiveByUserId(user.getId()) >= 5) {
            throw new BusinessException(UserErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }
        if (command.isDefault()) {
            addressRepository.findDefaultByUserId(user.getId())
                .ifPresent(Address::unsetDefault);
        }
        AddressVO addressVO = AddressVO.of(command.zipCode(), command.address(), command.detailAddress());
        Address address = Address.create(user, user.getName(), user.getPhoneNumber(), command.alias(), addressVO, command.isDefault());
        address.update();
        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        addressRepository.findAllActiveByUserId(userId)
            .forEach(address -> {
                address.delete();
                address.update();
            });
    }

    @Override
    public List<Address> findAllByUserId(Long userId) {
        return addressRepository.findAllActiveByUserId(userId);
    }

    @Override
    public Optional<Address> findDefaultByUserId(Long userId) {
        return addressRepository.findDefaultByUserId(userId);
    }
}
