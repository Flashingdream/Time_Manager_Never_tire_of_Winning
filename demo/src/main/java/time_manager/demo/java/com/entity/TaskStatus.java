package time_manager.demo.java.com.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

// 两种事件状态：已完成（COMPLETED）和待办（PENDING），兼容前端 TODO/DONE
public enum TaskStatus {
    COMPLETED,
    PENDING;

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toUpperCase()) {
            case "DONE":
            case "COMPLETED":
                return COMPLETED;
            case "TODO":
            case "PENDING":
                return PENDING;
            default:
                throw new IllegalArgumentException("Unknown TaskStatus: " + value);
        }
    }
}