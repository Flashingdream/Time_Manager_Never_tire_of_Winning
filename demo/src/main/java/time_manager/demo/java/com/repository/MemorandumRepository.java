package time_manager.demo.java.com.repository;

import time_manager.demo.java.com.entity.Memorandum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemorandumRepository extends JpaRepository<Memorandum, Long> {
}