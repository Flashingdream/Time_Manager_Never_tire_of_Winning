package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.MemorandumDTO;
import time_manager.demo.java.com.service.MemorandumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memos")
public class MemorandumController {

    @Autowired
    private MemorandumService memorandumService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllMemorandums() {
        List<MemorandumDTO> memos = memorandumService.getAllMemorandums();
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", memos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMemorandumById(@PathVariable Long id) {
        MemorandumDTO memo = memorandumService.getMemorandumById(id);
        if (memo != null) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", memo));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "备忘录不存在", null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createMemorandum(@RequestBody MemorandumDTO memorandumDTO) {
        MemorandumDTO created = memorandumService.createMemorandum(memorandumDTO);
        return ResponseEntity.ok(new ApiResponse(200, "添加成功", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateMemorandum(@PathVariable Long id, @RequestBody MemorandumDTO memorandumDTO) {
        MemorandumDTO updated = memorandumService.updateMemorandum(id, memorandumDTO);
        if (updated != null) {
            return ResponseEntity.ok(new ApiResponse(200, "更新成功", updated));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "备忘录不存在", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteMemorandum(@PathVariable Long id) {
        memorandumService.deleteMemorandum(id);
        return ResponseEntity.ok(new ApiResponse(200, "删除成功", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchMemorandums(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", memorandumService.getAllMemorandums()));
        }
        List<MemorandumDTO> results = memorandumService.searchMemorandums(keyword.trim());
        return ResponseEntity.ok(new ApiResponse(200, "搜索成功", results));
    }

    @PutMapping("/{id}/toggle-complete")
    public ResponseEntity<ApiResponse> toggleComplete(@PathVariable Long id) {
        MemorandumDTO updated = memorandumService.toggleComplete(id);
        if (updated != null) {
            return ResponseEntity.ok(new ApiResponse(200, "操作成功", updated));
        }
        return ResponseEntity.badRequest().body(new ApiResponse(400, "事件不存在", null));
    }
}