package time_manager.demo.java.com.service;

import time_manager.demo.java.com.entity.Memorandum;
import time_manager.demo.java.com.repository.MemorandumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private MemorandumRepository memorandumRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime lookAhead = now.plusMinutes(30);

        List<Memorandum> upcomingMemos = memorandumRepository.findByStartTimeBetween(now, lookAhead);

        for (Memorandum memo : upcomingMemos) {
            if (memo.getStartTime() == null) {
                continue;
            }
            int offset = memo.getReminderOffset() == null ? 5 : memo.getReminderOffset();
            LocalDateTime expectedReminderTime = memo.getStartTime().minusMinutes(offset).truncatedTo(ChronoUnit.MINUTES);
            if (expectedReminderTime.isEqual(now)) {
                String json = String.format(
                    "{\"title\":\"%s\",\"content\":\"%s\",\"tag\":\"%s\",\"startTime\":\"%s\",\"location\":\"%s\"}",
                    memo.getTitle() != null ? memo.getTitle().replace("\"", "'") : "",
                    memo.getContent() != null ? memo.getContent().replace("\"", "'") : "",
                    memo.getTag() != null ? memo.getTag() : "",
                    memo.getStartTime() != null ? memo.getStartTime().toString().replace("T", " ") : "",
                    memo.getLocation() != null ? memo.getLocation().replace("\"", "'") : ""
                );
                messagingTemplate.convertAndSend("/topic/reminders", json);
            }
        }
    }
}
