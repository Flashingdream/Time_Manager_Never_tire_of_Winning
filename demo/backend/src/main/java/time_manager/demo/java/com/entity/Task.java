package time_manager.demo.java.com.entity;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 事件名称

    @Column(nullable = true)
    private LocalDateTime eventTime; // 事件时间

    @Column(length = 500)
    private String eventContent; // 事件内容

    @Column(length = 200)
    private String location; // 事件地点

    private LocalDateTime startTime; // 开始时间

    private LocalDateTime endTime; // 结束时间

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // 事件状态

    private Integer duration; // 事件持续时间（分钟为单位）
}