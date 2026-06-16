package com.crossfitarmyjym.app.ui.progress;

import android.content.Context;

import com.crossfitarmyjym.app.R;
import com.crossfitarmyjym.app.data.model.PersonalRecord;
import com.crossfitarmyjym.app.data.model.Result;
import com.crossfitarmyjym.app.data.model.Wod;

import java.util.List;
import java.util.Locale;

public final class ProgressFormatter {

    private static final int PREVIEW_LIMIT = 8;

    private ProgressFormatter() {
    }

    public static String personalRecords(Context context, List<PersonalRecord> records) {
        if (records == null || records.isEmpty()) {
            return context.getString(R.string.no_pr_records);
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(records.size(), PREVIEW_LIMIT);
        for (int i = 0; i < limit; i++) {
            PersonalRecord record = records.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append("• ")
                    .append(resolveExerciseName(record))
                    .append(" - ")
                    .append(nonEmpty(record.getResultText(), formatResultValue(record)));
            String achievedAt = displayDate(record.getAchievedAt());
            if (!achievedAt.isEmpty()) {
                builder.append(" · ").append(achievedAt);
            }
        }
        appendMore(builder, records.size(), limit);
        return builder.toString();
    }

    public static String wodResults(Context context, List<Result> results) {
        if (results == null || results.isEmpty()) {
            return context.getString(R.string.no_wod_results);
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(results.size(), PREVIEW_LIMIT);
        for (int i = 0; i < limit; i++) {
            Result result = results.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            Wod wod = result.getWod();
            String wodName = wod != null ? wod.getName() : null;
            builder.append("• ")
                    .append(nonEmpty(wodName, "WOD"))
                    .append(" - ")
                    .append(nonEmpty(result.getFormattedScore(), String.valueOf(result.getScore())));
            String completedAt = displayDate(result.getCompletedAt());
            if (!completedAt.isEmpty()) {
                builder.append(" · ").append(completedAt);
            }
        }
        appendMore(builder, results.size(), limit);
        return builder.toString();
    }

    private static void appendMore(StringBuilder builder, int size, int limit) {
        if (size > limit) {
            builder.append("\n+").append(size - limit).append(" еще");
        }
    }

    private static String resolveExerciseName(PersonalRecord record) {
        if (record.getExerciseName() != null && !record.getExerciseName().trim().isEmpty()) {
            return record.getExerciseName();
        }
        if (record.getExercise() != null && record.getExercise().getName() != null
                && !record.getExercise().getName().trim().isEmpty()) {
            return record.getExercise().getName();
        }
        return "Упражнение";
    }

    private static String formatResultValue(PersonalRecord record) {
        if (record.getResultValue() == null) {
            return "";
        }
        String value = String.format(Locale.getDefault(), "%.2f", record.getResultValue())
                .replaceAll("[,.]00$", "");
        if (record.getUnit() != null && !record.getUnit().trim().isEmpty()) {
            return value + " " + record.getUnit().trim();
        }
        return value;
    }

    private static String displayDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        int dateLength = Math.min(10, value.length());
        return value.substring(0, dateLength);
    }

    private static String nonEmpty(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}
