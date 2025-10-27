package com.phraiz.back.common.service;

import com.phraiz.back.common.domain.BaseFolder;
import com.phraiz.back.common.dto.response.FoldersResponseDTO;
import com.phraiz.back.common.exception.GlobalErrorCode;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseFolderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 *  도메인(SUMMARY / PARAPHRASE / CITATION …) 별 서비스가
 *  이 클래스를 상속해 공통 CRUD 로직을 재사용.
 */

@RequiredArgsConstructor
@Transactional
public abstract class AbstractFolderService<E extends BaseFolder> {

    protected final BaseFolderRepository<E> repo;

    @Transactional(readOnly = true)
    public Page<FoldersResponseDTO> getFolders(String memberId, int p, int s) {
        return repo.findAllByMemberId(memberId,
                        PageRequest.of(p, s, Sort.by("createdAt").descending()))
                .map(this::toDTO);
    }

    public void createFolder(String memberId, String name) {
        // 사용자의 요금제 불러오기 - free 요금제는 create 불가능
        validateCreateFolder(memberId);

        if (repo.existsByMemberIdAndName(memberId, name)) {
            throw new BusinessLogicException(GlobalErrorCode.FOLDER_NAME_EXISTS);
        }
        E entity = newFolderEntity(memberId, name);   // ← 도메인별 팩토리 메서드
        repo.save(entity);
    }
    public void renameFolder(String memberId, Long id, String name) {
        E folder = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new BusinessLogicException(GlobalErrorCode.FOLDER_NOT_EXISTS));
        // 같은 이름의 폴더 생성 불가능하게.
        try {
            folder.setName(name);
            repo.save(folder); // 변경 감지로도 가능하지만, save 호출해서 예외 포착 확실히
        } catch (DataIntegrityViolationException e) {
            throw new BusinessLogicException(GlobalErrorCode.FOLDER_NAME_EXISTS);
        }
    }
    public void deleteFolder(String memberId, Long id) {
        E folder = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new BusinessLogicException(GlobalErrorCode.FOLDER_NOT_EXISTS));
        repo.delete(folder);
    }

    protected abstract FoldersResponseDTO toDTO(E entity);
    protected abstract E newFolderEntity(String memberId, String name);
    protected abstract void validateCreateFolder(String memberId);
}
