package time_manager.demo.java.com.controller;

import time_manager.demo.java.com.dto.ApiResponse;
import time_manager.demo.java.com.dto.FocusRecordDTO;
import time_manager.demo.java.com.service.FocusRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/focus")
public class FocusRecordController {

    @Autowired
    private FocusRecordService focusRecordService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveFocus(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String yearMonth = (String) body.get("yearMonth");
        Integer minutes = (Integer) body.get("minutes");
        if (userId == null || yearMonth == null || minutes == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "参数不完整", null));
        }
        FocusRecordDTO record = focusRecordService.saveFocus(userId, yearMonth, minutes);
        return ResponseEntity.ok(new ApiResponse(200, "保存成功", record));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse> getRanking(@RequestParam String yearMonth) {
        List<FocusRecordDTO> ranking = focusRecordService.getMonthlyRanking(yearMonth, 3);
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", ranking));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse> getUserMonthly(@RequestParam String userId, @RequestParam String yearMonth) {
        FocusRecordDTO record = focusRecordService.getUserMonthly(userId, yearMonth);
        return ResponseEntity.ok(new ApiResponse(200, "获取成功", record));
    }
}
