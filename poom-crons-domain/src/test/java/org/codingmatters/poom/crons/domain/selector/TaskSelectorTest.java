package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TaskSelectorTest {

    private static final LocalDateTime AT_2013_04_05_10_32_42_728 = LocalDateTime.of(2013, Month.APRIL, 5, 10, 32, 42, 728);
    private static final LocalDateTime AT_2013_04_05_10_32_42_000 = LocalDateTime.of(2013, Month.APRIL, 5, 10, 32, 42, 0);

    @Test
    public void givenAtExpression__whenMinuteOfHour__thenSelectableAtMinute() throws Exception {
        assertTrue("at minute", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.minuteOfHours(32L)
                )).build()));

        assertFalse("the minute before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.minuteOfHours(33L)
                )).build()));

        assertFalse("the minute after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.minuteOfHours(31L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenHourOfDay_andMinuteOfHour__thenSelectableAtHourMinute() throws Exception {
        assertTrue("at minute", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertTrue("one day before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728.minusDays(1)).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(32L)
                )).build()));


        assertFalse("the minute after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(31L)
                )).build()));

        assertFalse("the minute before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(33L)
                )).build()));

        assertFalse("an hours after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(9L).minuteOfHours(32L)
                )).build()));

        assertFalse("an hours before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(11L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenDayOfWeek_andHourOfDay_andMinuteOfHour__thenSelectableAtDayHourMinute() throws Exception {
        assertTrue("at minute", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.FRIDAY).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.SATURDAY).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.THURSDAY).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void dayOfWeekComparison() throws Exception {
        for (At.DayOfWeek atdayOfWeek : At.DayOfWeek.values()) {
            DayOfWeek javaDayOfWeek = DayOfWeek.valueOf(atdayOfWeek.name());
            assertTrue(atdayOfWeek.name(), DateTimeTaskSelector.sameDayOfWeek(atdayOfWeek, javaDayOfWeek));
        }

        assertFalse(DateTimeTaskSelector.sameDayOfWeek(At.DayOfWeek.MONDAY, DayOfWeek.SUNDAY));
    }

    @Test
    public void givenAtExpression__whenDayOfMonth_andHourOfDay_andMinuteOfHour__thenSelectableAtDayHourMinute() throws Exception {
        assertTrue("at minute", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(5L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(6L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(4L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenDayOfYear_andHourOfDay_andMinuteOfHour__thenSelectableAtDayHourMinute() throws Exception {
        assertTrue("at minute", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(95L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(96L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(94L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void givenEveryExpression__whenEvery1Minute__thenIsAlwaysSelectable() throws Exception {
        for (int i = 0; i < 500; i++) {
            int offset = i;
            assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMinutes(offset)).minutes(1L)
                    )).build()));
        }
    }

    @Test
    public void givenEveryExpression__whenEvery2Minute__thenIsSelectableOneMinuteOverTwo() throws Exception {
        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(i % 2 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMinutes(offset)).minutes(2L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMinutes(offset)).minutes(2L)
                        )).build()));
            }
        }
    }

    @Test
    public void givenEveryExpression__whenEvery5Minute__thenIsSelectableOneMinuteOverTwo() throws Exception {
        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(i % 5 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMinutes(offset)).minutes(5L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMinutes(offset)).minutes(5L)
                        )).build()));
            }
        }
    }

    @Test
    public void givenEveryExpression__whenEvery1Hour__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(1)).hours(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(1).minusMinutes(2L)).hours(1L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset)).hours(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset).minusMinutes(2L)).hours(1L)
                    )).build()));
        }
    }

    @Test
    public void givenEveryExpression__whenEvery3Hour__thenIsSelectableOnEveryHour() throws Exception {

        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(3)).hours(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(3).minusMinutes(2L)).hours(3L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(offset % 3 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset)).hours(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset).minusMinutes(2L)).hours(3L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset)).hours(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusHours(offset).minusMinutes(2L)).hours(3L)
                        )).build()));
            }
        }
    }

    @Test
    public void givenEveryExpression__whenEvery1Day__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(1)).days(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(1).minusMinutes(2L)).days(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(1).minusHours(1L)).days(1L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset)).days(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusMinutes(2L)).days(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusHours(1L)).days(1L)
                    )).build()));
        }
    }

    @Test
    public void givenEveryExpression__whenEvery3Day__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(3)).days(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(3).minusMinutes(2L)).days(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(3).minusHours(1L)).days(3L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(offset % 3 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset)).days(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusMinutes(2L)).days(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusHours(1L)).days(3L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset)).days(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusMinutes(2L)).days(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusDays(offset).minusHours(1L)).days(3L)
                        )).build()));
            }
        }
    }

    @Test
    public void givenEveryExpression__whenEvery1Month__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(1)).months(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(1).minusMinutes(2L)).months(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(1).minusHours(1L)).months(1L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset)).months(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusMinutes(2L)).months(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusHours(1L)).months(1L)
                    )).build()));
        }
    }

    @Test
    public void givenEveryExpression__whenEvery3Month__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(3)).months(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(3).minusMinutes(2L)).months(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(3).minusHours(1L)).months(3L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(offset % 3 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset)).months(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusMinutes(2L)).months(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusHours(1L)).months(3L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset)).months(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusMinutes(2L)).months(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusMonths(offset).minusHours(1L)).months(3L)
                        )).build()));
            }
        }
    }

    @Test
    public void givenEveryExpression__whenEvery1Year__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(1)).years(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(1).minusMinutes(2L)).years(1L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(1).minusHours(1L)).years(1L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset)).years(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusMinutes(2L)).years(1L)
                    )).build()));
            assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                    .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusHours(1L)).years(1L)
                    )).build()));
        }
    }

    @Test
    public void givenEveryExpression__whenEvery3Year__thenIsSelectableOnEveryHour() throws Exception {
        assertTrue(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(3)).years(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(3).minusMinutes(2L)).years(3L)
                )).build()));
        assertFalse(new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(3).minusHours(1L)).years(3L)
                )).build()));

        for (int i = 0; i < 500; i++) {
            int offset = i;
            if(offset % 3 == 0) {
                assertTrue("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset)).years(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusMinutes(2L)).years(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusHours(1L)).years(3L)
                        )).build()));
            } else {
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset)).years(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusMinutes(2L)).years(3L)
                        )).build()));
                assertFalse("" + i, new DateTimeTaskSelector(AT_2013_04_05_10_32_42_728).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .every(every -> every.startingAt(AT_2013_04_05_10_32_42_000.minusYears(offset).minusHours(1L)).years(3L)
                        )).build()));
            }
        }
    }


    @Test
    public void given__when__then() throws Exception {
        assertThat(ChronoUnit.HOURS.between(AT_2013_04_05_10_32_42_000, AT_2013_04_05_10_32_42_000), is(0L));
        assertThat(ChronoUnit.HOURS.between(AT_2013_04_05_10_32_42_000.minusHours(10), AT_2013_04_05_10_32_42_000), is(10L));
        assertThat(ChronoUnit.HOURS.between(AT_2013_04_05_10_32_42_000, AT_2013_04_05_10_32_42_000.minusHours(10)), is(-10L));

        assertThat((-4) % 3, is(-1));

        assertThat(
                Math.abs(ChronoUnit.MINUTES.between(AT_2013_04_05_10_32_42_000.minusMinutes(12), AT_2013_04_05_10_32_42_000)) % 12,
                is(0L)
        );
    }
}