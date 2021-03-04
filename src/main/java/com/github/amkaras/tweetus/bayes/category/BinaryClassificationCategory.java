package com.github.amkaras.tweetus.bayes.category;

public enum BinaryClassificationCategory implements ClassificationCategory {

    POSITIVE(-1.0, "positive"),
    NEUTRAL(0.0, "neutral"),
    NEGATIVE(1.0, "negative");

    private final double polarityScore;
    private final String name;

    BinaryClassificationCategory(double polarityScore, String name) {
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

    public static BinaryClassificationCategory map(DifferentialClassificationCategory differentialCategory) {
        switch (differentialCategory) {
            case NEUTRAL:
                return NEUTRAL;
            case WEAK_POSITIVE:
            case POSITIVE:
            case STRONG_POSITIVE:
                return POSITIVE;
            case WEAK_NEGATIVE:
            case NEGATIVE:
            case STRONG_NEGATIVE:
                return NEGATIVE;
            default:
                throw new IllegalArgumentException("Unknown differential classification category: " + differentialCategory);
        }
    }
}
