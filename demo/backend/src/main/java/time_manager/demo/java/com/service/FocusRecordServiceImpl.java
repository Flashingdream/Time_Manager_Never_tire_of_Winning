package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.FocusRecordDTO;
import time_manager.demo.java.com.entity.FocusRecord;
import time_manager.demo.java.com.repository.FocusRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FocusRecordServiceImpl implements FocusRecordService {

    @Autowired
    private FocusRecordRepository focusRecordRepository;

    @Override
    public FocusRecordDTO saveFocus(String userId, String yearMonth, Integer minutes) {
        Optional<FocusRecord> existing = focusRecordRepository.findByUserIdAndYearMonth(userId, yearMonth);
        FocusRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setMinutes(record.getMinutes() + minutes);
            record.setPoints(record.getMinutes() / 30);
        } else {
            int points = minutes / 30;
            record = new FocusRecord(userId, yearMonth, minutes, points);
        }
        FocusRecord saved = focusRecordRepository.save(record);
        return convertToDTO(saved);
    }

    @Override
    public List<FocusRecordDTO> getMonthlyRanking(String yearMonth, int limit) {
        return focusRecordRepository.findByYearMonthOrderByPointsDesc(yearMonth).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FocusRecordDTO getUserMonthly(String userId, String yearMonth) {
        Optional<FocusRecord> record = focusRecordRepository.findByUserIdAndYearMonth(userId, yearMonth);
        return record.map(this::convertToDTO).orElse(null);
    }

    private FocusRecordDTO convertToDTO(FocusRecord record) {
        return new FocusRecordDTO(
                record.getId(),
                record.getUserId(),
                record.getYearMonth(),
                record.getMinutes(),
                record.getPoints()
        );
    }
}
