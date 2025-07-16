package com.phraiz.back.summary.enums;

import com.phraiz.back.common.enums.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SummaryPrompt {

    ONE_LINE("다음 글을 전체 내용을 하나의 문장으로 간결하게 요약해줘."),
    FULL("다음 글의 전반적인 내용을 여러 문장으로 자연스럽게 요약해줘."),
    PARAGRAPH("다음 글의 각 문단의 핵심 내용을 따로따로 요약해줘."),
    KEY_POINT("다음 글의 전체 내용을 문단 구분 없이 핵심 문장 리스트 형식으로 요약해줘."),
    QUESTION_BASED("다음 글을 읽고 '%s'에 대해 요약해서 답변해줘."),
    TARGETED("다음 글을 '%s' 에게 설명하듯 요약해줘.");

    private final String prompt;

}
