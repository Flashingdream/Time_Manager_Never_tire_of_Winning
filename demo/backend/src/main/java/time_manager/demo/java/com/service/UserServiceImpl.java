package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.UserDTO;
import time_manager.demo.java.com.entity.User;
import time_manager.demo.java.com.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initAdmin() {
        if (!userRepository.existsByUserId("admin")) {
            User admin = new User("admin", "123456", "admin");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }
    }

    @Override
    public UserDTO registerUser(String userId, String password) {
        if (userRepository.existsByUserId(userId)) {
            return null;
        }
        User newUser = new User(userId, password, "user");
        newUser.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(newUser);
        return convertToDTO(saved);
    }

    @Override
    public UserDTO loginUser(String userId, String password) {
        Optional<User> existing = userRepository.findByUserId(userId);
        if (existing.isPresent() && existing.get().getPassword().equals(password)) {
            return convertToDTO(existing.get());
        }
        return null;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByUserId(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        return user.map(this::convertToDTO).orElse(null);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUserId(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
