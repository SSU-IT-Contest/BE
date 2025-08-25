package com.phraiz.back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Plan {

    FREE(1L, 0, 0, 100000),
    BASIC(2L, 5900, 49000, 2900000),
    STANDARD(3L, 7900, 99000, 6800000),
    PRO(4L, 12900, 119000, 2147483647);

    private final Long planId;
    private final int monthlyFee;
    private final int annualFee;
    private final int maxTokensPerMonth;

    public static Plan fromName(String name) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제: " + name));
    }

    public static Plan fromId(Long planId) {
        return Arrays.stream(values())
                .filter(p -> p.getPlanId() == planId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제 ID: " + planId));
    }
}
