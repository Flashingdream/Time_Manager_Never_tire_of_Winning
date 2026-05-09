package time_manager.demo.java.com.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "memorandums")
public class Memorandum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 事件名称

    @Column(nullable = false, length = 500)
    private String content;


    @Column(length = 200)
    private String location; // 事件地点

    @Column(length = 20)
    private String tag; // 事件标签：生活、娱乐、工作、社交

    private LocalDateTime startTime; // 开始时间

    private LocalDateTime endTime; // 结束时间

    private Integer reminderOffset; // 提前提醒分钟数，0/5/15/30

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Memorandum() {}

    public Memorandum(String title, String content, String location, LocalDateTime startTime, LocalDateTime endTime, String tag) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tag = tag;
        this.reminderOffset = 5;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getReminderOffset() {
        return reminderOffset;
    }

    public void setReminderOffset(Integer reminderOffset) {
        this.reminderOffset = reminderOffset;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}