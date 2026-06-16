package com.crossfitarmyjym.app.ui.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public final class WodResultInputParser {

    private WodResultInputParser() {
    }

    public static ParsedResult parse(@Nullable String wodFormat,
                                     @Nullable String scoreText,
                                     @Nullable String displayText) {
        String score = trim(scoreText);
        String display = trim(displayText);
        boolean forTime = "for_time".equalsIgnoreCase(trim(wodFormat));

        if (!score.isEmpty()) {
            return new ParsedResult(parseNonNegativeNumber(score), display);
        }

        if (display.isEmpty()) {
            throw new NumberFormatException("Result is empty");
        }

        if (forTime) {
            return new ParsedResult(parseTimeToSeconds(display), display);
        }

        double parsedScore = parseNonNegativeNumber(firstToken(display));
        return new ParsedResult(parsedScore, display);
    }

    public static String scoreHint(@Nullable String wodFormat) {
        if ("for_time".equalsIgnoreCase(trim(wodFormat))) {
            return "Секунды для рейтинга, можно оставить пустым";
        }
        return "Очки / повторы для рейтинга";
    }

    public static String displayHint(@Nullable String wodFormat) {
        if ("for_time".equalsIgnoreCase(trim(wodFormat))) {
            return "Например 12:45";
        }
        return "Например 120 reps или 8 rounds";
    }

    private static double parseTimeToSeconds(String value) {
        String[] parts = value.trim().split(":");
        if (parts.length < 2 || parts.length > 3) {
            throw new NumberFormatException("Time must be mm:ss or hh:mm:ss");
        }

        int seconds = parseTimePart(parts[parts.length - 1], 0, 59);
        int minutes = parseTimePart(parts[parts.length - 2], 0, 59);
        int hours = parts.length == 3 ? parseTimePart(parts[0], 0, Integer.MAX_VALUE) : 0;
        return hours * 3600d + minutes * 60d + seconds;
    }

    private static int parseTimePart(String value, int min, int max) {
        int parsed = Integer.parseInt(value.trim());
        if (parsed < min || parsed > max) {
            throw new NumberFormatException("Time part is out of range");
        }
        return parsed;
    }

    private static double parseNonNegativeNumber(String value) {
        double parsed = Double.parseDouble(value.trim().replace(',', '.'));
        if (parsed < 0) {
            throw new NumberFormatException("Score must be non-negative");
        }
        return parsed;
    }

    private static String firstToken(String value) {
        String trimmed = value.trim();
        int separator = trimmed.indexOf(' ');
        return separator == -1 ? trimmed : trimmed.substring(0, separator);
    }

    private static String trim(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    public static final class ParsedResult {
        private final double score;
        private final String formattedScore;

        ParsedResult(double score, @NonNull String formattedScore) {
            this.score = score;
            this.formattedScore = formattedScore.trim().isEmpty()
                    ? String.format(Locale.US, "%.2f", score).replaceAll("\\.00$", "")
                    : formattedScore.trim();
        }

        public double getScore() {
            return score;
        }

        public String getFormattedScore() {
            return formattedScore;
        }
    }
}
