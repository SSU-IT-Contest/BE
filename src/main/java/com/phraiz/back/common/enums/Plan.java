package com.phraiz.back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Plan {

    FREE(1L, 0, 30000),
    BASIC(2L, 5900, 70000),
    STANDARD(3L, 7900, 250000),
    PRO(4L, 9900, 600000);

    private final Long planId;
    private final int monthlyFee;
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
