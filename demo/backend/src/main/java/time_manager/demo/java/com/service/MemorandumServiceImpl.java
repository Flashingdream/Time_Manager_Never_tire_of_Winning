package time_manager.demo.java.com.service;

import time_manager.demo.java.com.dto.MemorandumDTO;
import time_manager.demo.java.com.entity.Memorandum;
import time_manager.demo.java.com.repository.MemorandumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemorandumServiceImpl implements MemorandumService {

    @Autowired
    private MemorandumRepository memorandumRepository;

    @Override
    public List<MemorandumDTO> getAllMemorandums() {
        return memorandumRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MemorandumDTO getMemorandumById(Long id) {
        Optional<Memorandum> memorandum = memorandumRepository.findById(id);
        return memorandum.map(this::convertToDTO).orElse(null);
    }

    @Override
    public MemorandumDTO createMemorandum(MemorandumDTO memorandumDTO) {
        Memorandum memorandum = new Memorandum();
        memorandum.setTitle(memorandumDTO.getTitle());
        memorandum.setContent(memorandumDTO.getContent());
        memorandum.setLocation(memorandumDTO.getLocation());
        memorandum.setTag(memorandumDTO.getTag());
        memorandum.setStartTime(memorandumDTO.getStartTime());
        memorandum.setEndTime(memorandumDTO.getEndTime());
        memorandum.setReminderOffset(memorandumDTO.getReminderOffset() == null ? 5 : memorandumDTO.getReminderOffset());
        memorandum.setCreatedAt(LocalDateTime.now());
        memorandum.setUpdatedAt(LocalDateTime.now());
        Memorandum saved = memorandumRepository.save(memorandum);
        return convertToDTO(saved);
    }

    @Override
    public MemorandumDTO updateMemorandum(Long id, MemorandumDTO memorandumDTO) {
        Optional<Memorandum> existing = memorandumRepository.findById(id);
        if (existing.isPresent()) {
            Memorandum memorandum = existing.get();
            memorandum.setTitle(memorandumDTO.getTitle());
            memorandum.setContent(memorandumDTO.getContent());
            memorandum.setLocation(memorandumDTO.getLocation());
            memorandum.setTag(memorandumDTO.getTag());
            memorandum.setStartTime(memorandumDTO.getStartTime());
            memorandum.setEndTime(memorandumDTO.getEndTime());
            memorandum.setReminderOffset(memorandumDTO.getReminderOffset() == null ? 5 : memorandumDTO.getReminderOffset());
            memorandum.setUpdatedAt(LocalDateTime.now());
            Memorandum updated = memorandumRepository.save(memorandum);
            return convertToDTO(updated);
        }
        return null;
    }

    @Override
    public void deleteMemorandum(Long id) {
        memorandumRepository.deleteById(id);
    }

    private MemorandumDTO convertToDTO(Memorandum memorandum) {
        return new MemorandumDTO(
                memorandum.getId(),
                memorandum.getTitle(),
                memorandum.getContent(),
                memorandum.getLocation(),
                memorandum.getTag(),
                memorandum.getStartTime(),
                memorandum.getEndTime(),
                memorandum.getReminderOffset(),
                memorandum.getCreatedAt(),
                memorandum.getUpdatedAt()
        );
    }
}