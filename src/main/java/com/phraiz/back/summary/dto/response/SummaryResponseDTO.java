package com.phraiz.back.summary.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SummaryResponseDTO {

    Long historyId;
    String name;
    String result;
    long remainingToken;

    String originalText;

}
