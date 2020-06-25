package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.Scheduled;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.codingmatters.poom.services.support.date.UTC;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Random;
import java.util.TimeZone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EveryExpressionTaskSelectorTest {

    private static final LocalDateTime START = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 854);
    private static final LocalDateTime AT =  LocalDateTime.of(2013, Month.APRIL, 5, 10, 0, 0, 735);

    static private final TimeZone PARIS_TZ = TimeZone.getTimeZone("Europe/Paris");
    static private final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");
    static private final TimeZone GMT2_TZ = TimeZone.getTimeZone("GMT+2");

    @Test
    public void givenSecondsPrecision__whenEverySecond__thenIsSelectableAnytime() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().seconds(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    assertTrue("selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondsPrecision__whenEveryTwoSecond__thenIsSelectableEveryTwoSeconds() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().seconds(2L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    if(second % 2 == 0) {
                        assertTrue("selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("not selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                }
            }
        }
    }
    @Test
    public void givenSecondsPrecision__whenEveryTwoSecond_andTZ__thenIsSelectableEveryTwoSeconds() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().timezone("Europe/Paris").scheduled(Scheduled.builder()
                .every(Every.builder().seconds(2L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    if(second % 2 == 0) {
                        assertTrue("selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime, UTC_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("not selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime, UTC_TZ).selectable(taskSpec));
                    }
                }
            }
        }
    }

    @Test
    public void givenMinutesPrecision__whenEveryMinute__thenIsSelectableOnEveryCall() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().minutes(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenMinutesPrecision__whenEveryMinute_andTZ__thenIsSelectableOnEveryCall() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().timezone("Europe/Paris").scheduled(Scheduled.builder()
                .every(Every.builder().minutes(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondsPrecision__whenEveryMinute__thenIsSelectableWhenSecondIs0() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().minutes(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    if(second == 0) {
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                }
            }
        }
    }

    @Test
    public void givenSecondsPrecision__whenEveryMinute_andTZ__thenIsSelectableWhenSecondIs0() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().timezone("Europ/Paris").scheduled(Scheduled.builder()
                .every(Every.builder().minutes(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    if(second == 0) {
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                }
            }
        }
    }

    @Test
    public void givenMinutePrecision__whenEveryTwoMinutes__thenIsSelectableEveryTwoMinutesWhateverTheSecond() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().minutes(2L).startingAt(START).build())
                .build()).build();

        assertTrue("START", DateTimeTaskSelector.minutesPrecision(START, PARIS_TZ).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(minute % 2 == 0) {
                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenMinutePrecision__whenEveryTwoMinutes_andTZ__thenIsSelectableEveryTwoMinutesWhateverTheSecond() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().timezone("Europ/Paris").scheduled(Scheduled.builder()
                .every(Every.builder().minutes(2L).startingAt(START).build())
                .build()).build();

        assertTrue("START", DateTimeTaskSelector.minutesPrecision(START, PARIS_TZ).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(minute % 2 == 0) {
                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.minutesPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryTwoMinutes__thenIsSelectableEveryTwoMinutesOnSecond0() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().minutes(2L).startingAt(START).build())
                .build()).build();

        assertTrue("START", DateTimeTaskSelector.secondsPrecision(START, PARIS_TZ).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(second == 0) {
                    if (minute % 2 == 0) {
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryTwoMinutes_andTZ__thenIsSelectableEveryTwoMinutesOnSecond0() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().timezone("Europe/Paris").scheduled(Scheduled.builder()
                .every(Every.builder().minutes(2L).startingAt(START).build())
                .build()).build();

        assertTrue("START", DateTimeTaskSelector.secondsPrecision(START, PARIS_TZ).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(second == 0) {
                    if (minute % 2 == 0) {
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryXHours__thenIsSelectableOnHourX_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().hours(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(2), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusHours(2), GMT2_TZ).selectable(taskSpec));


        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2018, 10, 2, 4, 0), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2018, 10, 2, 6, 0), PARIS_TZ).selectable(taskSpec));

    }

    @Test
    public void givenSecondPrecision__whenEveryXHours_andTZ__thenIsSelectableOnHourX_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder()
                .timezone("GMT+00:18")
                .scheduled(Scheduled.builder()
                .every(Every.builder().hours(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), UTC_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2018, 10, 2, 4, 0).minusMinutes(18), UTC_TZ).selectable(taskSpec));

        for (int hour = 0; hour < 24; hour++) {
            for (int minutes = 0; minutes < 60; minutes++) {
                for (int seconds = 0; seconds < 60; seconds++) {
                    LocalDateTime atTime = LocalDateTime.of(
                            2018, 10, 2, hour, minutes, seconds
                    );
                    if(seconds == 0 && minutes == 42 && hour % x == (x - 1)) {
                        assertTrue("selecteable in UTC", DateTimeTaskSelector.secondsPrecision(atTime, UTC_TZ).selectable(taskSpec));
                        assertTrue("selecteable in Paris", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    } else {
                        assertFalse("not selecteable in UTC", DateTimeTaskSelector.secondsPrecision(atTime, UTC_TZ).selectable(taskSpec));
                        assertFalse("not selecteable in Paris", DateTimeTaskSelector.secondsPrecision(atTime, PARIS_TZ).selectable(taskSpec));
                    }
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryXHours_andTZSetOnSpec__thenIsSelectableOnHourX_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder()
                .timezone("GMT+00:18")
                .scheduled(Scheduled.builder()
                    .every(Every.builder().hours(x).startingAt(START).build())
                .build()).build();

        assertFalse(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, 4, 0, 0), UTC_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, 23, 42, 0), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, 3, 42, 0), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, 7, 42, 0), UTC_TZ).selectable(taskSpec));

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                if(h % x == 3 && m == 42) {
                    System.out.println(h + ":" + m);
                    assertTrue(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, h, m, 0), UTC_TZ).selectable(taskSpec));
                } else {
                    assertFalse(DateTimeTaskSelector.secondsPrecision(LocalDateTime.of(2014, 11, 2, h, m, 0), UTC_TZ).selectable(taskSpec));
                }
            }
        }

    }

    @Test
    public void givenSecondPrecision__whenEveryXDays__thenIsSelectableOnDayX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().days(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, PARIS_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, GMT2_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(1), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(2), GMT2_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusDays(x - 1), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusDays(x), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusDays(x + 2), UTC_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusHours(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMinutes(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusSeconds(1), UTC_TZ).selectable(taskSpec));

    }

    @Test
    public void givenSecondPrecision__whenEveryXDays_andTZOnSpec__thenIsSelectableOnDayX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().timezone("GMT+00:18").scheduled(Scheduled.builder()
                .every(Every.builder().days(x).startingAt(START).build())
                .build()).build();

        assertFalse(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, PARIS_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, GMT2_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), GMT2_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusDays(x - 1), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusDays(x), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusDays(x + 2), UTC_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusHours(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusMinutes(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusSeconds(1), UTC_TZ).selectable(taskSpec));
    }

    @Test
    public void givenSecondPrecision__whenEveryXMonth__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().months(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(1), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(2), GMT2_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusYears(1), UTC_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusDays(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusHours(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMinutes(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusSeconds(1), UTC_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMonths(3), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusMonths(4), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMonths(5), UTC_TZ).selectable(taskSpec));
    }

    @Test
    public void givenSecondPrecision__whenEveryXMonth_withTZ__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder()
                .timezone("GMT+00:18")
                .scheduled(Scheduled.builder()
                .every(Every.builder().months(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), GMT2_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusMonths(3), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusMonths(4), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18).plusMonths(5), UTC_TZ).selectable(taskSpec));
    }

    @Test
    public void givenSecondPrecision__whenEveryXYear__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().years(x).startingAt(START).build())
                .build()).build();

        assertTrue(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, PARIS_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START, GMT2_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(1), PARIS_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusHours(2), GMT2_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusYears(x - 1), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusYears(x), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusYears(x + 1), UTC_TZ).selectable(taskSpec));

        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMonths(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusDays(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusHours(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusMinutes(1), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusSeconds(1), UTC_TZ).selectable(taskSpec));
    }

    @Test
    public void givenSecondPrecision__whenEveryXYear_andTZOnSpec__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().timezone("GMT+00:18").scheduled(Scheduled.builder()
                .every(Every.builder().years(x).startingAt(START).build())
                .build()).build();

        assertFalse(DateTimeTaskSelector.secondsPrecision(START, UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusYears(x - 1).minusMinutes(18), UTC_TZ).selectable(taskSpec));
        assertTrue(DateTimeTaskSelector.secondsPrecision(START.plusYears(x).minusMinutes(18), UTC_TZ).selectable(taskSpec));
        assertFalse(DateTimeTaskSelector.secondsPrecision(START.plusYears(x + 1).minusMinutes(18), UTC_TZ).selectable(taskSpec));

        assertTrue(DateTimeTaskSelector.secondsPrecision(START.minusMinutes(18), PARIS_TZ).selectable(taskSpec));

    }
}
