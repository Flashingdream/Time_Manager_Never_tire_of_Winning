package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.MemorandumDTO;
import java.util.List;

public interface MemorandumService {

    List<MemorandumDTO> getAllMemorandums();

    MemorandumDTO getMemorandumById(Long id);

    MemorandumDTO createMemorandum(MemorandumDTO memorandumDTO);

    MemorandumDTO updateMemorandum(Long id, MemorandumDTO memorandumDTO);

    void deleteMemorandum(Long id);

    List<MemorandumDTO> searchMemorandums(String keyword);

    MemorandumDTO toggleComplete(Long id);
}