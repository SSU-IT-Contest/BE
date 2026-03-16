package com.phraiz.back.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRefDto {
    private final Long memberId;
    private final Long planId;
}
