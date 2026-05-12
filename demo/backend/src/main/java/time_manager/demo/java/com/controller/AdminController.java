package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.config.JwtUtil;
import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.UserDTO;
import time_manager.demo.java.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(400, "用户ID不能为空", null));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(400, "密码不能为空", null));
        }
        UserDTO user = userService.loginUser(userId.trim(), password.trim());
        if (user == null) {
            return ResponseEntity.ok(new ApiResponse(400, "用户名或密码错误", null));
        }
        if (!"admin".equals(user.getRole())) {
            return ResponseEntity.ok(new ApiResponse(403, "非管理员，请使用普通用户登录", null));
        }
        if (Boolean.TRUE.equals(user.getBanned())) {
            return ResponseEntity.ok(new ApiResponse(403, "账号已被封禁", null));
        }
        String token = JwtUtil.generateToken(user.getUserId(), user.getRole());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("admin_token", token);
        result.put("user", user);
        return ResponseEntity.ok(new ApiResponse(200, "管理员登录成功", result));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers(@RequestParam(required = false) String keyword,
                                                    HttpServletRequest request) {
        List<UserDTO> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userService.searchUsers(keyword.trim());
        } else {
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", users));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<ApiResponse> getUserStats(@RequestParam(required = false) String keyword,
                                                     HttpServletRequest request) {
        List<UserDTO> all;
        if (keyword != null && !keyword.trim().isEmpty()) {
            all = userService.searchUsers(keyword.trim());
        } else {
            all = userService.getAllUsers();
        }
        long total = all.size();
        long admins = all.stream().filter(u -> "admin".equals(u.getRole())).count();
        long regularUsers = total - admins;
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long newThisMonth = all.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(thisMonth))
                .count();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("admins", admins);
        stats.put("users", regularUsers);
        stats.put("newThisMonth", newThisMonth);
        stats.put("allUsers", all);
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", stats));
    }

    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse> banUser(@PathVariable String userId, HttpServletRequest request) {
        if ("admin".equals(userId)) {
            return ResponseEntity.ok(new ApiResponse(400, "不能封禁主管理员", null));
        }
        UserDTO user = userService.banUser(userId, true);
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "封禁成功", user));
        }
        return ResponseEntity.ok(new ApiResponse(400, "用户不存在", null));
    }

    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<ApiResponse> unbanUser(@PathVariable String userId, HttpServletRequest request) {
        UserDTO user = userService.banUser(userId, false);
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "解封成功", user));
        }
        return ResponseEntity.ok(new ApiResponse(400, "用户不存在", null));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> getUserByUserId(@PathVariable String userId, HttpServletRequest request) {
        UserDTO user = userService.getUserByUserId(userId);
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", user));
        } else {
            return ResponseEntity.ok(new ApiResponse(400, "用户不存在", null));
        }
    }
}
