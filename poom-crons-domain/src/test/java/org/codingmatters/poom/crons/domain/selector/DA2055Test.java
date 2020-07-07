package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.Scheduled;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.ImmutableEntity;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.time.*;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DA2055Test {

    static private final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");
    static private final TimeZone PARIS_TZ = TimeZone.getTimeZone("Europe/Paris");

    @Test
    public void givenEveryFirstOfMonthAtMidnightInParis__whenFirstDayOfMonth_andMidnightInParis_andWinterTime__thenSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt("Europe/Paris", LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 5, 1, 0, 0, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertTrue(DateTimeTaskSelector.minutesPrecision(at).selectable(spec));
    }

    @Test
    public void givenEveryFirstOfMonthAtMidnightWithout__whenFirstDayOfMonth_andMidnightInParis_andWinterTime__thenSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt(LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 5, 1, 0, 0, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertTrue(DateTimeTaskSelector.minutesPrecision(at, PARIS_TZ).selectable(spec));
    }

    @Test
    public void givenEveryFirstOfMonthAtMidnightInParis__whenFirstDayOfMonth_andAfterMidnightInParis_andWinterTime__thenNotSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt("Europe/Paris", LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 5, 1, 0, 1, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertFalse(DateTimeTaskSelector.minutesPrecision(at).selectable(spec));
    }

    @Test
    public void givenEveryFirstOfMonthAtMidnightInParis__whenFirstDayOfMonth_andMidnightInParis_andSummerTime__thenSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt("Europe/Paris", LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 8, 1, 0, 0, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertTrue(DateTimeTaskSelector.minutesPrecision(at).selectable(spec));
    }

    @Test
    public void givenEveryFirstOfMonthAtMidnightWithout__whenFirstDayOfMonth_andMidnightInParis_andSummerTime__thenSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt(LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 8, 1, 0, 0, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertTrue(DateTimeTaskSelector.minutesPrecision(at, PARIS_TZ).selectable(spec));
    }

    @Test
    public void givenEveryFirstOfMonthAtMidnightInParis__whenFirstDayOfMonth_andAfterMidnightInParis_andSummerTime__thenNotSelectable() throws Exception {
        TaskSpec spec = this.everyFirstDayOfMonthAt("Europe/Paris", LocalTime.of(0, 0, 0));

        LocalDateTime at = LocalDateTime.of(2020, 8, 1, 0, 1, 0)
                .atZone(PARIS_TZ.toZoneId())
                .withZoneSameInstant(UTC_TZ.toZoneId())
                .toLocalDateTime();

        assertFalse(DateTimeTaskSelector.minutesPrecision(at).selectable(spec));
    }

    private TaskSpec everyFirstDayOfMonthAt(String timezone, LocalTime at) {
        LocalDateTime startingAt = at.atDate(
                LocalDateTime.now().withYear(1970).withMonth(1).toLocalDate().withDayOfMonth(1)
        );

        return TaskSpec.builder().timezone(timezone)
                .scheduled(Scheduled.builder()
                        .every(Every.builder()
                                .months(1L)
                                .startingAt(startingAt)
                                .build())
                        .build())
                .build();
    }

    private TaskSpec everyFirstDayOfMonthAt(LocalTime at) {
        LocalDateTime startingAt = at.atDate(
                LocalDateTime.now().withYear(1970).withMonth(1).toLocalDate().withDayOfMonth(1)
        );

        return TaskSpec.builder()
                .scheduled(Scheduled.builder()
                        .every(Every.builder()
                                .months(1L)
                                .startingAt(startingAt)
                                .build())
                        .build())
                .build();
    }
}
