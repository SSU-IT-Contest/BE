package com.phraiz.back.paraphrase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParaphrasePrompt {

    STANDARD("표준"),
    ACADEMIC("학술적인"),
    CREATIVE("창의적인"),
    FLUENCY("유창한"),
    EXPERIMENTAL("실험적인");

    private final String prompt;

}
