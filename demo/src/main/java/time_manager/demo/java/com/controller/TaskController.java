package time_manager.demo.java.com.controller;
// 任务控制层
import time_manager.demo.java.com.dto.TaskDTO;
import time_manager.demo.java.com.entity.Task;
import time_manager.demo.java.com.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // 获取所有任务
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // 根据ID获取任务
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建待办事件
    @PostMapping
    public Task createTask(@RequestBody TaskDTO taskDTO) {
        Task task = new Task();
        task.setName(taskDTO.getName());
        task.setEventTime(taskDTO.getEventTime());
        task.setEventContent(taskDTO.getEventContent());
        task.setLocation(taskDTO.getLocation());
        task.setStartTime(taskDTO.getStartTime());
        task.setEndTime(taskDTO.getEndTime());
        task.setStatus(taskDTO.getStatus());
        task.setDuration(taskDTO.getDuration());
        return taskService.createTask(task);
    }

    // 更新任务
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        Task task = new Task();
        task.setName(taskDTO.getName());
        task.setEventTime(taskDTO.getEventTime());
        task.setEventContent(taskDTO.getEventContent());
        task.setLocation(taskDTO.getLocation());
        task.setStartTime(taskDTO.getStartTime());
        task.setEndTime(taskDTO.getEndTime());
        task.setStatus(taskDTO.getStatus());
        task.setDuration(taskDTO.getDuration());
        Task updatedTask = taskService.updateTask(id, task);
        if (updatedTask != null) {
            return ResponseEntity.ok(updatedTask);
        }
        return ResponseEntity.notFound().build();
    }

    // 删除任务
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // 事件到期提醒：获取即将到期的任务
    /*@GetMapping("/upcoming")
    public List<Task> getUpcomingTasks() {
        return taskService.getUpcomingTasks();
    }*/
}