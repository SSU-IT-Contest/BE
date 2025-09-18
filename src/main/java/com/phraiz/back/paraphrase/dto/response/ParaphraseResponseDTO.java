package com.phraiz.back.paraphrase.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ParaphraseResponseDTO {
    Long resultHistoryId;
    String name;
    String result;
    long remainingToken;
}
