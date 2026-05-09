package time_manager.demo.java.com.repository;

import time_manager.demo.java.com.entity.Memorandum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemorandumRepository extends JpaRepository<Memorandum, Long> {

    @Query("SELECT m FROM Memorandum m WHERE m.startTime BETWEEN :start AND :end")
    List<Memorandum> findByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT m FROM Memorandum m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Memorandum> searchByKeyword(@Param("keyword") String keyword);
}