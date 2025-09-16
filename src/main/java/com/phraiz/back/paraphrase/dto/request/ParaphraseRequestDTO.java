package com.phraiz.back.paraphrase.dto.request;

import lombok.Getter;

@Getter
public class ParaphraseRequestDTO{
        // 우선은 한국어 기준
        Long folderId;
        Long historyId;
        String text;    // 패러프레이징 요청 text
        String userRequestMode;
        int scale; // 슬라이더 값
}
