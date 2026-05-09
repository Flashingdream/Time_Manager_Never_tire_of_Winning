package time_manager.demo.java.com.repository;

import time_manager.demo.java.com.entity.FocusRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FocusRecordRepository extends JpaRepository<FocusRecord, Long> {

    Optional<FocusRecord> findByUserIdAndYearMonth(String userId, String yearMonth);

    List<FocusRecord> findByYearMonthOrderByPointsDesc(String yearMonth);
}
