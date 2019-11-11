package by.nikolaichik.user.info.service.impl;

import by.nikolaichik.user.info.exceptions.UserServiceException;
import by.nikolaichik.user.info.io.entity.AddressEntity;
import by.nikolaichik.user.info.io.entity.UserEntity;
import by.nikolaichik.user.info.repository.AddressRepository;
import by.nikolaichik.user.info.repository.UserRepository;
import by.nikolaichik.user.info.shared.dto.AddressDTO;
import by.nikolaichik.user.info.ui.model.response.ErrorMessages;
import by.nikolaichik.user.info.service.AddressService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<AddressDTO> getAddresses(String userId) {
        UserEntity userEntity = getAndCheckUser(userId);

        List<AddressEntity> addresses = addressRepository.findAllByUserEntity(userEntity);

        Type addressesDTOListType = new TypeToken<List<AddressDTO>>() {}.getType();
        List<AddressDTO> characters = modelMapper.map(addresses, addressesDTOListType);
        return characters;
    }

    @Override
    public AddressDTO getUserAddress(String userId, String addressId) {
        UserEntity userEntity = getAndCheckUser(userId);

        AddressEntity addressEntity = addressRepository.findByAddressIdAndUserEntity(addressId, userEntity);
        return modelMapper.map(addressEntity, AddressDTO.class);
    }

    private UserEntity getAndCheckUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), userId);
        return userEntity;
    }
}
