package com.phraiz.back.summary.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SummaryResponseDTO {

    Long historyId;
    String name;
    String originalText;      // 원본 텍스트 추가
    String summarizedText;    // result → summarizedText로 변경
    Integer sequenceNumber;   // 몇 번째 content인지
    String mode;
    String question;
    String target;
    long remainingToken;

}
