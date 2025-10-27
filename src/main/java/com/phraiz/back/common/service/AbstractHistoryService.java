package com.phraiz.back.common.service;

import com.phraiz.back.common.domain.BaseHistory;
import com.phraiz.back.common.dto.request.HistoryUpdateDTO;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryContentResponseDTO;
import com.phraiz.back.common.exception.GlobalErrorCode;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractHistoryService<E extends BaseHistory> {

    protected final BaseHistoryRepository<E> repo;

    @Transactional(readOnly = true)
    public Page<HistoriesResponseDTO> getHistories(String memberId, @Nullable Long folderId, int p, int s) {
        Pageable pg = PageRequest.of(p, s, Sort.by("createdAt").descending());
        return (folderId == null)
                ? repo.findAllByMemberIdAndFolderIdIsNull(memberId, pg).map(this::toDTO)
                : repo.findAllByMemberIdAndFolderId(memberId, folderId, pg).map(this::toDTO);
    }

    public void createHistory(String memberId, @Nullable Long folderId, String name) {
        //사용자의 요금제 확인
        //free 요금제일 경우, 각 기능 별로 30개 제한
        validateRemainingHistoryCount(memberId);

        E entity = newHistoryEntity(memberId, folderId, name);
        try {
            repo.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessLogicException(GlobalErrorCode.HISTORY_NAME_EXISTS);
        }
    }
    public void updateHistory(String memberId, Long id, HistoryUpdateDTO dto) {
        E history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new BusinessLogicException(GlobalErrorCode.HISTORY_NOT_EXISTS));

        if (dto.name() != null && !dto.name().isBlank()) {
            try {
                history.setName(dto.name());
                repo.save(history); // 변경 감지로도 가능하지만, save 호출해서 예외 포착 확실히
            } catch (DataIntegrityViolationException e) {
                throw new BusinessLogicException(GlobalErrorCode.HISTORY_NAME_EXISTS);
            }
        }
        if (dto.folderId() != null) {
            try {
                history.setFolderId(dto.folderId());
                repo.save(history); // 변경 감지로도 가능하지만, save 호출해서 예외 포착 확실히
            } catch (DataIntegrityViolationException e) {
                throw new BusinessLogicException(GlobalErrorCode.DUPLICATED_HISTORY_EXISTS);
            }
        }
    }
    public void deleteHistory(String memberId, Long id) {
        E history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new BusinessLogicException(GlobalErrorCode.HISTORY_NOT_EXISTS));
        repo.delete(history);
    }
    // readHistoryContent는 각 도메인별 HistoryService에서 Content 조회와 함께 구현
    
    protected abstract HistoriesResponseDTO toDTO(E entity);
    protected abstract E newHistoryEntity(String memberId, Long folderId, String name);
    protected abstract void validateRemainingHistoryCount(String memberId);
}
