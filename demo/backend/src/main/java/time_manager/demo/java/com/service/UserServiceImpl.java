package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.UserDTO;
import time_manager.demo.java.com.entity.User;
import time_manager.demo.java.com.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void initAdmin() {
        Optional<User> existing = userRepository.findByUserId("admin");
        if (existing.isPresent()) {
            User admin = existing.get();
            // 修复旧数据：若 password 为空则写入 123456
            if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
                admin.setPassword("123456");
                userRepository.save(admin);
                userRepository.flush();
            }
        } else {
            User admin = new User("admin", "123456", "admin");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            userRepository.flush();
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
        if (existing.isPresent()) {
            User user = existing.get();
            if (Boolean.TRUE.equals(user.getBanned())) {
                return null; // 被封禁，不能登录
            }
            if (user.getPassword().equals(password)) {
                return convertToDTO(user);
            }
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

    @Override
    public List<UserDTO> searchUsers(String keyword) {
        return userRepository.findByUserIdContaining(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO banUser(String userId, boolean banned) {
        Optional<User> existing = userRepository.findByUserId(userId);
        if (existing.isPresent()) {
            User user = existing.get();
            user.setBanned(banned);
            return convertToDTO(userRepository.save(user));
        }
        return null;
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUserId(),
                user.getRole(),
                user.getBanned(),
                user.getCreatedAt()
        );
    }
}
