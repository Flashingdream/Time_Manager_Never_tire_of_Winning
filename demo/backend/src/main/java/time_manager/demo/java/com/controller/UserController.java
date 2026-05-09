package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.UserDTO;
import time_manager.demo.java.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "用户ID不能为空", null));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "密码不能为空", null));
        }
        UserDTO user = userService.registerUser(userId.trim(), password.trim());
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "注册成功", user));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "用户ID已存在", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "用户ID不能为空", null));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "密码不能为空", null));
        }
        UserDTO user = userService.loginUser(userId.trim(), password.trim());
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "登录成功", user));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "用户名或密码错误", null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserByUserId(@PathVariable String userId) {
        UserDTO user = userService.getUserByUserId(userId);
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", user));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "用户不存在", null));
        }
    }
}
