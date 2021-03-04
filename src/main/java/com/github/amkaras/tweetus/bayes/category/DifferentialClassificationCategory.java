package com.github.amkaras.tweetus.bayes.category;

import java.util.Arrays;

public enum DifferentialClassificationCategory implements ClassificationCategory {

    STRONG_NEGATIVE(-4.0, "strongneg"),
    NEGATIVE(-2.0, "negative"),
    WEAK_NEGATIVE(-1.0, "weakneg"),
    NEUTRAL(0.0, "neutral"),
    WEAK_POSITIVE(1.0, "weakpos"),
    POSITIVE(2.0, "positive"),
    STRONG_POSITIVE(4.0, "strongpos");

    private final double polarityScore;
    private final String name;

    DifferentialClassificationCategory(double polarityScore, String name) {
        this.polarityScore = polarityScore;
        this.name = name;
    }

    @Override
    public double getPolarityScore() {
        return polarityScore;
    }

    @Override
    public String getName() {
        return name;
    }

    public static ClassificationCategory getByName(String name) {
        if ("both".equals(name)) {
            return NEUTRAL;
        }
        return Arrays.stream(DifferentialClassificationCategory.values())
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There's no category for name " + name));
    }
}
