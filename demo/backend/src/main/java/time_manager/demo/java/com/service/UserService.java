package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.UserDTO;
import java.util.List;

public interface UserService {

    UserDTO registerUser(String userId, String password);

    UserDTO loginUser(String userId, String password);

    List<UserDTO> getAllUsers();

    UserDTO getUserByUserId(String userId);
}
