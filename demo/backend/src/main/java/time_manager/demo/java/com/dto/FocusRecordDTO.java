package time_manager.demo.java.com.dto;

public class FocusRecordDTO {

    private Long id;
    private String userId;
    private String yearMonth;
    private Integer minutes;
    private Integer points;

    public FocusRecordDTO() {}

    public FocusRecordDTO(Long id, String userId, String yearMonth, Integer minutes, Integer points) {
        this.id = id;
        this.userId = userId;
        this.yearMonth = yearMonth;
        this.minutes = minutes;
        this.points = points;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
