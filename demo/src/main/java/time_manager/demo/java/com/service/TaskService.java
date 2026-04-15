package time_manager.demo.java.com.service;
// 任务业务逻辑层
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import time_manager.demo.java.com.entity.Task;
import time_manager.demo.java.com.entity.TaskStatus;
import time_manager.demo.java.com.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Path TASKS_JSON_PATH = Paths.get("src", "main", "resources", "tasks.json");

    @PostConstruct
    public void initJsonFile() {
        try {
            if (Files.notExists(TASKS_JSON_PATH.getParent())) {
                Files.createDirectories(TASKS_JSON_PATH.getParent());
            }
            if (Files.notExists(TASKS_JSON_PATH)) {
                writeTasksToJson(taskRepository.findAll());
            }
        } catch (IOException e) {
            throw new RuntimeException("无法初始化 tasks.json 文件", e);
        }
    }

    private void writeTasksToJson(List<Task> tasks) {
        try {
            Files.createDirectories(TASKS_JSON_PATH.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(TASKS_JSON_PATH.toFile(), tasks);
        } catch (IOException e) {
            throw new RuntimeException("将任务写入 tasks.json 时出错", e);
        }
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    //创建任务时默认状态为待办（PENDING）
    public Task createTask(Task task) {
        task.setStatus(TaskStatus.PENDING);
        Task saved = taskRepository.save(task);
        writeTasksToJson(taskRepository.findAll());
        return saved;
    }

    public Task updateTask(Long id, Task taskDetails) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setName(taskDetails.getName());
            task.setEventTime(taskDetails.getEventTime());
            task.setEventContent(taskDetails.getEventContent());
            task.setLocation(taskDetails.getLocation());
            task.setStartTime(taskDetails.getStartTime());
            task.setEndTime(taskDetails.getEndTime());
            task.setStatus(taskDetails.getStatus());
            task.setDuration(taskDetails.getDuration());
            Task saved = taskRepository.save(task);
            writeTasksToJson(taskRepository.findAll());
            return saved;
        }
        return null;
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
        writeTasksToJson(taskRepository.findAll());
    }

    // 事件到期提醒：检查即将到期的任务
    //public List<Task> getUpcomingTasks() {
        //LocalDateTime now = LocalDateTime.now();
        //LocalDateTime soon = now.plusMinutes(30); // 例如，30分钟内到期
        // 这里需要自定义查询，但为了简单，返回所有PENDING任务
        //return taskRepository.findAll().stream()
                //.filter(task -> task.getStatus() == TaskStatus.PENDING && task.getEventTime().isBefore(soon))
                //.toList();
    //}
}