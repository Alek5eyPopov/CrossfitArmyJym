package com.crossfitarmyjym.app.ui.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WodResultInputParserTest {

    @Test
    public void parseForTimeDisplayConvertsMinutesAndSecondsToScore() {
        WodResultInputParser.ParsedResult result =
                WodResultInputParser.parse("for_time", "", "12:45");

        assertEquals(765d, result.getScore(), 0.001d);
        assertEquals("12:45", result.getFormattedScore());
    }

    @Test
    public void parseForTimeDisplayConvertsHoursMinutesAndSecondsToScore() {
        WodResultInputParser.ParsedResult result =
                WodResultInputParser.parse("for_time", "", "1:02:03");

        assertEquals(3723d, result.getScore(), 0.001d);
        assertEquals("1:02:03", result.getFormattedScore());
    }

    @Test
    public void parseRepsUsesFirstNumericTokenWhenScoreIsEmpty() {
        WodResultInputParser.ParsedResult result =
                WodResultInputParser.parse("amrap", "", "120 reps");

        assertEquals(120d, result.getScore(), 0.001d);
        assertEquals("120 reps", result.getFormattedScore());
    }

    @Test
    public void parseExplicitScoreKeepsDisplayText() {
        WodResultInputParser.ParsedResult result =
                WodResultInputParser.parse("amrap", "8,5", "8.5 rounds");

        assertEquals(8.5d, result.getScore(), 0.001d);
        assertEquals("8.5 rounds", result.getFormattedScore());
    }

    @Test(expected = NumberFormatException.class)
    public void parseRejectsNegativeScore() {
        WodResultInputParser.parse("amrap", "-1", "");
    }

    @Test(expected = NumberFormatException.class)
    public void parseForTimeRejectsOutOfRangeSeconds() {
        WodResultInputParser.parse("for_time", "", "12:99");
    }
}
