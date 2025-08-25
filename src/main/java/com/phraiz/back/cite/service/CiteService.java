package com.phraiz.back.cite.service;

import com.phraiz.back.cite.domain.Cite;
import com.phraiz.back.cite.domain.CiteFolder;
import com.phraiz.back.cite.dto.request.CitationRequestDTO;
import com.phraiz.back.cite.dto.request.RenameRequestDTO;
import com.phraiz.back.cite.dto.response.CitationResponseDTO;
import com.phraiz.back.cite.dto.response.FolderResponseDTO;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.cite.repository.CiteRepository;
import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CiteService {
    private final CiteRepository citeRepository;
    private final CiteHistoryService citeHistoryService;

    // 0. 소유권 체크(본인만 수정)
    public void checkCiteOwnership(Long memberId, Long citeOwnerId){
        if (!memberId.equals(citeOwnerId)) {
            throw new BusinessLogicException(CiteErrorCode.NO_PERMISSION_TO_UPDATE);
        }

    }
    // 1. 인용문 저장 과정
    // 1-1. 처음에 요청받은 url과 변환된 csl json 저장
    // 식별자 리턴
    public Long saveCslJson(String cslJson, String url, Member member) {
        Cite cite = Cite.builder()
                .member(member)
                .cslJson(cslJson)
                .title("제목없음")
                .url(url)
                .build();
        Cite result=citeRepository.save(cite);
        return result.getCiteId();
    }

    // 1-2. 인용문과 스타일 저장
    @Transactional
    public void saveCitation(String memberId, CitationRequestDTO citationRequestDTO) {
        Cite cite = citeRepository.findById(citationRequestDTO.getCiteId())
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));

        cite.setCitation(citationRequestDTO.getCitation());
        cite.setStyle(citationRequestDTO.getStyle());

        String result = citationRequestDTO.getCitation();
        Long citeId = cite.getCiteId();
        Long folderId = citationRequestDTO.getFolderId();
        Long historyId = citationRequestDTO.getHistoryId();

        // 내용 저장 (히스토리 업데이트)
        HistoryMetaDTO metaDTO = citeHistoryService.saveOrUpdateHistory(  // ★
                memberId,
                folderId,      // 루트면 null
                historyId,
                result,      // content
                cite
        );
    }

    // 1-3. 인용문 조회
    public CitationResponseDTO getCiteDetail(Member member, Long citeId) {
        Cite cite = citeRepository.findById(citeId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));

        // 소유권 체크 (본인만 조회 가능)
        checkCiteOwnership(member.getMemberId(), cite.getMember().getMemberId());

        // 엔티티 → DTO 변환
        return CitationResponseDTO.builder()
                .citeId(cite.getCiteId())
                .title(cite.getTitle())
                .style(cite.getStyle())
                .citation(cite.getCitation())
                .url(cite.getUrl())
                .createdAt(cite.getCreatedAt())
                .build();

    }

//    // 2. 히스토리
//    // 2-1. 사용자별 저장된 인용문 리스트 가져오기
//    public List<Map<String, Object>> getMyCitations(Member member) {
//        List<Cite> list=citeRepository.findAllByMemberOrderByCreatedAtDesc(member);
//        return list.stream()
//                .map(cite -> {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("citeId", cite.getCiteId());
//                    map.put("title", cite.getTitle());
//                    map.put("createdAt", cite.getCreatedAt());
//                    return map;
//                })
//                .toList();
//    }
//
//    // 2-2. 파일 이름 변경
//    @Transactional
//    public void renameCiteFile(RenameRequestDTO renameRequestDTO, Member member) {
//        Cite cite = citeRepository.findById(renameRequestDTO.getCiteId())
//                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));
//
//        // 소유권 체크 (본인만 수정 가능)
//        checkCiteOwnership(member.getMemberId(), cite.getMember().getMemberId());
//
//        cite.setTitle(renameRequestDTO.getNewTitle()); // 새로운 제목으로 파일 이름 수정
//        // jpa 자동 반영되므로 굳이 .save 할 필요 없음
//
//    }
//
//    // 2-3. 파일 삭제
//    @Transactional
//    public void deleteCiteFile(Long citeId, Member member) {
//        Cite cite=citeRepository.findById(citeId).orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));
//        // 소유권 체크 (본인만 삭제 가능)
//        checkCiteOwnership(member.getMemberId(), cite.getMember().getMemberId());
//        // 삭제
//        citeRepository.deleteById(citeId);
//    }
//
//    // 폴더 생성은 Basic 부터
//    // 3. 폴더
//    // 3-1. 폴더 생성
//    public CiteFolder createCiteFolder(Member member, String folderName) {
//        Plan userPlan = Plan.fromId(member.getPlanId());
//
//        if (userPlan.equals(Plan.FREE)) {
//            throw new BusinessLogicException(MemberErrorCode.PLAN_NOT_ENOUGH);
//        }
//
//        // 폴더 생성
//        CiteFolder citeFolder = new CiteFolder();
//        citeFolder.setName(folderName);
//        citeFolder.setMember(member);
//
//        return citeFolderRepository.save(citeFolder);
//
//    }
//    // 3-2. 폴더에 파일 저장
//    // 3-3. 사용자별 저장된 폴더 조회
//    public List<FolderResponseDTO> getMyFolders(Member member) {
//        List<CiteFolder> list=citeFolderRepository.findAllByMemberOrderByCreatedAtDesc(member);
//        return list.stream()
//                .map(citeFolder -> FolderResponseDTO.builder()
//                        .folderId(citeFolder.getFolderId())
//                        .folderName(citeFolder.getName())
//                        .createdAt(citeFolder.getCreatedAt())
//                        .build())
//                .toList();
//    }
//    // 3-4. 사용자별 폴더 속 아이템 조회
//    public List<Map<String, Object>> getMyFolderItems(Long folderId, Member member) {
//        CiteFolder folder = citeFolderRepository.findById(folderId)
//                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.FOLDER_NOT_FOUND));
//
//        // 소유권 체크 (본인만 조회 가능)
//        checkCiteOwnership(member.getMemberId(), folder.getMember().getMemberId());
//
//        return folder.getFiles().stream()
//                .map(item->{
//                    Cite cite = item.getCite();
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("citeId", cite.getCiteId());
//                    map.put("title", cite.getTitle());
//                    map.put("createdAt", cite.getCreatedAt());
//                    return map;
//                }).toList();
//
//    }
//        // 3-5. 아이템 이동



}
