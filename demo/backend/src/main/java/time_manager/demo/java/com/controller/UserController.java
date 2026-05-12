package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.config.JwtUtil;
import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.UserDTO;
import time_manager.demo.java.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(400, "用户ID不能为空", null));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(400, "密码不能为空", null));
        }
        UserDTO user = userService.registerUser(userId.trim(), password.trim());
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUserId(), user.getRole());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("user", user);
            return ResponseEntity.ok(new ApiResponse(200, "注册成功", result));
        } else {
            return ResponseEntity.ok(new ApiResponse(400, "用户ID已存在", null));
        }
    }

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
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUserId(), user.getRole());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("user", user);
            return ResponseEntity.ok(new ApiResponse(200, "登录成功", result));
        } else {
            return ResponseEntity.ok(new ApiResponse(400, "用户名或密码错误", null));
        }
    }
}
