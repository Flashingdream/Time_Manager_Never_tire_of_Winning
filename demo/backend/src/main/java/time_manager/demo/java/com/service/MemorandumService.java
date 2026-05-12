package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.MemorandumDTO;
import java.util.List;

public interface MemorandumService {

    List<MemorandumDTO> getAllMemorandums(String userId);

    MemorandumDTO getMemorandumById(Long id, String userId);

    MemorandumDTO createMemorandum(MemorandumDTO memorandumDTO);

    MemorandumDTO updateMemorandum(Long id, MemorandumDTO memorandumDTO);

    void deleteMemorandum(Long id);

    List<MemorandumDTO> searchMemorandums(String userId, String keyword);

    MemorandumDTO toggleComplete(Long id);
}