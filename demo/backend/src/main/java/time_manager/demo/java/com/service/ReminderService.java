package time_manager.demo.java.com.service;

import time_manager.demo.java.com.entity.Memorandum;
import time_manager.demo.java.com.repository.MemorandumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private MemorandumRepository memorandumRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60000) // ???????
    public void checkUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusMinutes(5); // ??5????

        List<Memorandum> upcomingMemos = memorandumRepository.findByStartTimeBetween(now, reminderTime);

        for (Memorandum memo : upcomingMemos) {
            String message = "??: ?? '" + memo.getTitle() + "' ????? " + memo.getStartTime();
            messagingTemplate.convertAndSend("/topic/reminders", message);
        }
    }
}
