package com.phraiz.back.common.service;

import com.phraiz.back.common.domain.BaseHistory;
import com.phraiz.back.common.dto.request.HistoryUpdateDTO;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryContentResponseDTO;
import com.phraiz.back.common.repository.BaseHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
        repo.save(entity);
    }
    public void updateHistory(String memberId, Long id, HistoryUpdateDTO dto) {
        E history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));

        if (dto.name() != null && !dto.name().isBlank()) {
            history.setName(dto.name());
        }
        if (dto.folderId() != null) {
            history.setFolderId(dto.folderId());
        }
    }
    public void deleteHistory(String memberId, Long id) {
        E history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));
        repo.delete(history);
    }
    public HistoryContentResponseDTO readHistoryContent(String memberId, Long id) {
        E history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));

        return HistoryContentResponseDTO.builder()
                .id(history.getId())
                .content(history.getContent())
                .lastUpdate(history.getLastUpdate())
                .build();
    }

    protected abstract HistoriesResponseDTO toDTO(E entity);
    protected abstract E newHistoryEntity(String memberId, Long folderId, String name);
    protected abstract void validateRemainingHistoryCount(String memberId);
}
