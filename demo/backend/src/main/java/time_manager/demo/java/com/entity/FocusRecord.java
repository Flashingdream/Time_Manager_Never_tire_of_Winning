package time_manager.demo.java.com.entity;

import javax.persistence.*;

@Entity
@Table(name = "focus_records")
public class FocusRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 7)
    private String yearMonth;

    @Column(nullable = false)
    private Integer minutes = 0;

    @Column(nullable = false)
    private Integer points = 0;

    public FocusRecord() {}

    public FocusRecord(String userId, String yearMonth, Integer minutes, Integer points) {
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
