package time_manager.demo.java.com.dto;

import time_manager.demo.java.com.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private String name;
    private LocalDateTime eventTime;
    private String eventContent;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TaskStatus status;
    private Integer duration;
}