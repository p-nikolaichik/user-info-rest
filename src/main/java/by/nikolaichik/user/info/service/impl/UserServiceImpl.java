package by.nikolaichik.user.info.service.impl;

import by.nikolaichik.user.info.exceptions.UserServiceException;
import by.nikolaichik.user.info.io.entity.PasswordResetTokenEntity;
import by.nikolaichik.user.info.io.entity.UserEntity;
import by.nikolaichik.user.info.service.UserService;
import by.nikolaichik.user.info.shared.dto.AddressDTO;
import by.nikolaichik.user.info.repository.PasswordResetTokenRepository;
import by.nikolaichik.user.info.repository.UserRepository;
import by.nikolaichik.user.info.shared.AmazonSES;
import by.nikolaichik.user.info.shared.Utils;
import by.nikolaichik.user.info.shared.dto.UserDTO;
import by.nikolaichik.user.info.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AmazonSES amazonSES;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public UserDTO createUser(UserDTO userDTO) {

        if (userRepository.findByEmail(userDTO.getEmail()) != null)
            throw new UserServiceException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        Iterator<AddressDTO> addressDTOIterator = userDTO.getAddresses().iterator();
        while (addressDTOIterator.hasNext()) {
            AddressDTO addressDTO = addressDTOIterator.next();
            addressDTO.setUserEntity(userDTO);
            addressDTO.setAddressId(utils.generateAddressId(10));
        }
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
        userEntity.setEmailVerificationToken(Utils.generateEmailVerificationToken(publicUserId));
        userEntity.setEmailVerificationStatus(false);
        UserDTO returnValue = modelMapper.map(userRepository.save(userEntity), UserDTO.class);
        //Send an email message to user to verify their email address
        amazonSES.verifyEmail(returnValue);
        return modelMapper.map(returnValue, UserDTO.class);
    }

    @Override
    public UserDTO getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) throw new UsernameNotFoundException(email);
        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Override
    public UserDTO getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), userId);
        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        UserEntity databaseUserEntity = userRepository.findByUserId(userId);
        if (databaseUserEntity == null)
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), userId);
        databaseUserEntity.setFirstName(userDTO.getFirstName());
        databaseUserEntity.setLastName(userDTO.getLastName());
        databaseUserEntity.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        UserEntity savedUser = userRepository.save(databaseUserEntity);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public List<UserDTO> getUsers(int page, int limit) {
        if (page > 0) page -= 1;
        Pageable pageableRequest = PageRequest.of(page, limit);
        List<UserEntity> userEntitiesDTOType = userRepository.findAll(pageableRequest).getContent();
        Type addressesDTOList = new TypeToken<List<UserDTO>>() {}.getType();
        return modelMapper.map(userEntitiesDTOType, addressesDTOList);
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage(), userId);
        userRepository.delete(userEntity);
    }

    @Override
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;
        UserEntity userEntity = userRepository.findByEmailVerificationToken(token);
        if (userEntity != null) {
            boolean hasTokenExpired = Utils.hasTokenExpired(token);
            if (!hasTokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public boolean requestPasswordReset(String email) {
        boolean returnValue = false;
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            return returnValue;
        }

        String token = Utils.generatePasswordResetToken(userEntity.getUserId());
        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        returnValue = new AmazonSES().sendPasswordResetRequest(userEntity.getFirstName(), userEntity.getEmail(), token);
        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) throw new UsernameNotFoundException(email);
        return new User(userEntity.getEmail(), userEntity.getPassword(), userEntity.getEmailVerificationStatus(),
                true, true, true, new ArrayList<>());
        //return new org.springframework.security.core.userdetails.User(userEntity.getEmail(), userEntity.getPassword(), new ArrayList<>());
    }
}
