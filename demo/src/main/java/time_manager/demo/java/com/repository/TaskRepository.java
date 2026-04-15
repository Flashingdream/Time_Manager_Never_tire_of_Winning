package time_manager.demo.java.com.repository;
// 任务数据访问层
import time_manager.demo.java.com.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}