package com.phraiz.back.cite.service;

import com.phraiz.back.cite.domain.CiteFolder;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.common.dto.response.FoldersResponseDTO;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseFolderRepository;
import com.phraiz.back.common.service.AbstractFolderService;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@Slf4j
public class CiteFolderService extends AbstractFolderService<CiteFolder> {
    private final MemberRepository memberRepository;

    protected CiteFolderService(BaseFolderRepository<CiteFolder> repo, MemberRepository memberRepository) {
        super(repo);
        this.memberRepository = memberRepository;
    }

    @Override
    protected FoldersResponseDTO toDTO(CiteFolder entity) {
        FoldersResponseDTO.Folders folderItem = FoldersResponseDTO.Folders.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();

        return FoldersResponseDTO.builder()
                .folders(Collections.singletonList(folderItem))   // Java 8 호환
                // .folders(List.of(folderItem))                  // Java 9+ 에서는 이렇게도 가능
                .build();
    }

    @Override
    protected CiteFolder newFolderEntity(String memberId, String name) {
        return CiteFolder.builder()
                .memberId(memberId)
                .name(name)
                .build();
    }

    @Override
    protected void validateCreateFolder(String memberId) {
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            throw new BusinessLogicException(CiteErrorCode.PLAN_NOT_ACCESSED);
        }
    }

}
