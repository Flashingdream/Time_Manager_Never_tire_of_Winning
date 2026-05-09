package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.FocusRecordDTO;
import java.util.List;

public interface FocusRecordService {

    FocusRecordDTO saveFocus(String userId, String yearMonth, Integer minutes);

    List<FocusRecordDTO> getMonthlyRanking(String yearMonth, int limit);

    FocusRecordDTO getUserMonthly(String userId, String yearMonth);
}
