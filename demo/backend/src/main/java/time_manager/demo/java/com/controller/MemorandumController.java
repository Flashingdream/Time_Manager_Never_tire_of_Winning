package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.config.JwtUtil;
import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.MemorandumDTO;
import time_manager.demo.java.com.service.MemorandumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memos")
public class MemorandumController {

    @Autowired
    private MemorandumService memorandumService;

    private String getUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            Map<String, Object> claims = JwtUtil.parseToken(auth.substring(7));
            if (claims != null) {
                return (String) claims.get("userId");
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllMemorandums(HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(new ApiResponse(401, "未登录", null));
        List<MemorandumDTO> memos = memorandumService.getAllMemorandums(userId);
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", memos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMemorandumById(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(new ApiResponse(401, "未登录", null));
        MemorandumDTO memo = memorandumService.getMemorandumById(id, userId);
        if (memo != null) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", memo));
        } else {
            return ResponseEntity.ok(new ApiResponse(400, "备忘录不存在", null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createMemorandum(@RequestBody MemorandumDTO memorandumDTO,
                                                         HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(new ApiResponse(401, "未登录", null));
        memorandumDTO.setUserId(userId);
        MemorandumDTO created = memorandumService.createMemorandum(memorandumDTO);
        return ResponseEntity.ok(new ApiResponse(200, "添加成功", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateMemorandum(@PathVariable Long id,
                                                         @RequestBody MemorandumDTO memorandumDTO) {
        MemorandumDTO updated = memorandumService.updateMemorandum(id, memorandumDTO);
        if (updated != null) {
            return ResponseEntity.ok(new ApiResponse(200, "更新成功", updated));
        } else {
            return ResponseEntity.ok(new ApiResponse(400, "备忘录不存在", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteMemorandum(@PathVariable Long id) {
        memorandumService.deleteMemorandum(id);
        return ResponseEntity.ok(new ApiResponse(200, "删除成功", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchMemorandums(@RequestParam String keyword,
                                                          HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.ok(new ApiResponse(401, "未登录", null));
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", memorandumService.getAllMemorandums(userId)));
        }
        List<MemorandumDTO> results = memorandumService.searchMemorandums(userId, keyword.trim());
        return ResponseEntity.ok(new ApiResponse(200, "搜索成功", results));
    }

    @PutMapping("/{id}/toggle-complete")
    public ResponseEntity<ApiResponse> toggleComplete(@PathVariable Long id) {
        MemorandumDTO updated = memorandumService.toggleComplete(id);
        if (updated != null) {
            return ResponseEntity.ok(new ApiResponse(200, "操作成功", updated));
        }
        return ResponseEntity.ok(new ApiResponse(400, "事件不存在", null));
    }
}
