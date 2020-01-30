package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.Scheduled;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EveryExpressionTaskSelectorTest {

    private static final LocalDateTime START = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 854);
    private static final LocalDateTime AT =  LocalDateTime.of(2013, Month.APRIL, 5, 10, 0, 0, 735);


    @Test
    public void givenSecondsPrecision__whenEverySecond__thenIsSelectableAnytime() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().seconds(1L).startingAt(START).build())
                .build()).build();

        for (int second = 0; second < 60; second++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);

                    assertTrue("selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
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
                        assertTrue("selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
                    } else {
                        assertFalse("not selectable at " + atTime, DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
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

                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime).selectable(taskSpec));
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
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
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

        assertTrue("START", DateTimeTaskSelector.minutesPrecision(START).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(minute % 2 == 0) {
                    assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.minutesPrecision(atTime).selectable(taskSpec));
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.minutesPrecision(atTime).selectable(taskSpec));
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryTwoMinutes__thenIsSelectableEveryTwoMinutesOnSecond0() throws Exception {
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().minutes(2L).startingAt(START).build())
                .build()).build();

        assertTrue("START", DateTimeTaskSelector.secondsPrecision(START).selectable(taskSpec));

        for (int minute = 0; minute < 60; minute++) {
            for (int second = 0; second < 60; second++) {
                LocalDateTime atTime = AT.withMinute(minute).withSecond(second);
                if(second == 0) {
                    if (minute % 2 == 0) {
                        assertTrue("at " + atTime + " selectable", DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
                    } else {
                        assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
                    }
                } else {
                    assertFalse("at " + atTime + " not selectable", DateTimeTaskSelector.secondsPrecision(atTime).selectable(taskSpec));
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

        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute++) {
                for (int second = 0; second < 60; second++) {
                    LocalDateTime atTime = AT.withHour(hour).withMinute(minute).withSecond(second);
                    DateTimeTaskSelector selector = DateTimeTaskSelector.secondsPrecision(atTime);

                    if(second == 0 && minute == 0 && hour % x == 0) {
                        assertTrue("selectable at " + atTime, selector.selectable(taskSpec));
                    } else {
                        assertFalse("not selectable at " + atTime, selector.selectable(taskSpec));
                    }
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

        for (int day = 1; day < 40; day++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute++) {
                    for (int second = 0; second < 60; second++) {
                        LocalDateTime atTime = START.plusDays(day).withHour(hour).withMinute(minute).withSecond(second);
                        DateTimeTaskSelector selector = DateTimeTaskSelector.secondsPrecision(atTime);

                        if(second == 0 && minute == 0 && hour == 0 && day % x == 0) {
                            assertTrue("selectable at " + atTime, selector.selectable(taskSpec));
                        } else {
                            assertFalse("not selectable at " + atTime, selector.selectable(taskSpec));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryXMonth__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().months(x).startingAt(START).build())
                .build()).build();

        for (int month = 0; month < 12; month++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute++) {
                    for (int second = 0; second < 60; second++) {
                        LocalDateTime atTime = START.plusMonths(month).withHour(hour).withMinute(minute).withSecond(second);
                        DateTimeTaskSelector selector = DateTimeTaskSelector.secondsPrecision(atTime);

                        if(second == 0 && minute == 0 && hour == 0 && month % x == 0) {
                            assertTrue("selectable at " + atTime, selector.selectable(taskSpec));
                        } else {
                            assertFalse("not selectable at " + atTime, selector.selectable(taskSpec));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void givenSecondPrecision__whenEveryXYear__thenIsSelectableOnMonthX_andHour0_andMinute0_andSecond0() throws Exception {
        long x = 4;
        TaskSpec taskSpec = TaskSpec.builder().scheduled(Scheduled.builder()
                .every(Every.builder().years(x).startingAt(START).build())
                .build()).build();

        for (int year = 0; year < 10; year++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute++) {
                    for (int second = 0; second < 60; second++) {
                        LocalDateTime atTime = START.plusYears(year).withHour(hour).withMinute(minute).withSecond(second);
                        DateTimeTaskSelector selector = DateTimeTaskSelector.secondsPrecision(atTime);

                        if(second == 0 && minute == 0 && hour == 0 && year % x == 0) {
                            assertTrue("selectable at " + atTime, selector.selectable(taskSpec));
                        } else {
                            assertFalse("not selectable at " + atTime, selector.selectable(taskSpec));
                        }
                    }
                }
            }
        }
    }
}
