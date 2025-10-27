package com.phraiz.back.paraphrase.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ParaphraseResponseDTO {
    Long resultHistoryId;
    String name;
    String originalText;      // 원본 텍스트 추가
    String paraphrasedText;   // result → paraphrasedText로 변경
    Integer sequenceNumber;   // 몇 번째 content인지
    long remainingToken;
}
