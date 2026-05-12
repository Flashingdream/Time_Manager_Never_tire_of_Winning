package time_manager.demo.java.com.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class UserDTO {

    private Long id;
    private String userId;
    private String role;
    private Boolean banned;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(Long id, String userId, String role, Boolean banned, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.banned = banned;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
