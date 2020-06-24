package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.TimeZone;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class AtExpressionTaskSelectorTest {

    private static final LocalDateTime AT_2013_04_05_10_32_42_728 = LocalDateTime.of(2013, Month.APRIL, 5, 10, 32, 42, 728);

    static private final TimeZone PARIS_TZ = TimeZone.getTimeZone("Europe/Paris");
    static private final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");

    @Test
    public void givenAtExpression__whenMinuteOfHour__thenSelectableAtMinute() throws Exception {
        assertTrue("at minute", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ)
                .selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                        .at(at -> at.minuteOfHours(32L))
                ).build()));

        assertFalse("the minute before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.minuteOfHours(33L)
                )).build()));

        assertFalse("the minute after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.minuteOfHours(31L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenHourOfDay_andMinuteOfHour__thenSelectableAtHourMinute() throws Exception {
        assertTrue("at minute (UTC)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(32L)
                )).build()));
        assertTrue("at minute (Europe/Paris)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(12L).minuteOfHours(32L)
                )).build()));

        assertTrue("one day before (UTC)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728.minusDays(1), UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(32L)
                )).build()));
        assertTrue("one day before (Europe/Paris)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728.minusDays(1), PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(12L).minuteOfHours(32L)
                )).build()));


        assertFalse("the minute after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(31L)
                )).build()));

        assertFalse("the minute before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(10L).minuteOfHours(33L)
                )).build()));

        assertFalse("an hours after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(9L).minuteOfHours(32L)
                )).build()));

        assertFalse("an hours before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.hourOfDay(11L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenDayOfWeek_andHourOfDay_andMinuteOfHour__thenSelectableAtDayHourMinute() throws Exception {
        assertTrue("at minute (UTC)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.FRIDAY).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
        assertTrue("at minute (Europe/Paris)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.FRIDAY).hourOfDay(12L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfWeek(At.DayOfWeek.SATURDAY).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
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
        assertTrue("at minute (Europ/Paris)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(5L).hourOfDay(12L).minuteOfHours(32L)
                )).build()));
        assertTrue("at minute", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(5L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(6L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfMonth(4L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
    }

    @Test
    public void givenAtExpression__whenDayOfYear_andHourOfDay_andMinuteOfHour__thenSelectableAtDayHourMinute() throws Exception {
        assertTrue("at minute (UTC)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(95L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
        assertTrue("at minute (Europe/Paris)", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, PARIS_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(95L).hourOfDay(12L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day before", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(96L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));

        assertFalse("one day after", DateTimeTaskSelector.minutesPrecision(AT_2013_04_05_10_32_42_728, UTC_TZ).selectable(TaskSpec.builder().scheduled(scheduled -> scheduled
                .at(at -> at.dayOfYear(94L).hourOfDay(10L).minuteOfHours(32L)
                )).build()));
    }

}