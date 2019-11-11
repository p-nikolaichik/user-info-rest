package by.nikolaichik.user.info.service;

import by.nikolaichik.user.info.shared.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUser(String email);

    UserDTO getUserByUserId(String userId);

    UserDTO updateUser(String userId, UserDTO userDTO);

    List<UserDTO> getUsers(int page, int limit);

    void deleteUser(String userId);

    boolean verifyEmailToken(String token);

    boolean requestPasswordReset(String email);
}
