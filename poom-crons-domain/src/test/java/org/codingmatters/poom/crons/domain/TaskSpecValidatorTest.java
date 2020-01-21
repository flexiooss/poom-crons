package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.services.support.date.UTC;
import org.junit.Test;

import static org.codingmatters.poom.crons.domain.TaskSpecValidator.TaskSpecValidation.invalidSpec;
import static org.codingmatters.poom.crons.domain.TaskSpecValidator.TaskSpecValidation.validSpec;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TaskSpecValidatorTest {

    @Test
    public void whenNoExpressionProvided__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .build()).validate(),
                is(invalidSpec("must provide an expression (one of at, every)"))
        );
    }

    @Test
    public void whenNominalAtExpression__thenIsValid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                    .url("http://an.url")
                    .payload(p -> p.property("a", v -> v.stringValue("prop")))
                    .scheduled(s -> s.at(at -> at.hourOfDay(8L).minuteOfHours(30L)))
                    .build()).validate().valid(),
                is(true)
        );
    }

    @Test
    public void whenNominalEveryExpression__thenIsValid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                    .url("http://an.url")
                    .payload(p -> p.property("a", v -> v.stringValue("prop")))
                    .scheduled(s -> s.every(e -> e.minutes(2L).startingAt(UTC.now())))
                    .build()).validate().valid(),
                is(true)
        );
    }

    @Test
    public void givenAtExpression__whenNoUrl__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .payload(p -> p.property("a", v -> v.stringValue("prop")))
                        .scheduled(s -> s.at(at -> at.hourOfDay(8L).minuteOfHours(30L)))
                        .build()).validate(),
                is(invalidSpec("no url provided"))
        );
    }

    @Test
    public void givenEveryExpression__whenNoUrl__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .payload(p -> p.property("a", v -> v.stringValue("prop")))
                        .scheduled(s -> s.every(e -> e.minutes(2L).startingAt(UTC.now())))
                        .build()).validate(),
                is(invalidSpec("no url provided"))
        );
    }

    @Test
    public void givenAtExpression__whenNoPayload__thenIsValid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(at -> at.hourOfDay(8L).minuteOfHours(30L)))
                        .build()).validate().valid(),
                is(true)
        );
    }

    @Test
    public void givenEveryExpression__whenNoPayload__thenIsValid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.minutes(2L).startingAt(UTC.now())))
                        .build()).validate().valid(),
                is(true)
        );
    }

    @Test
    public void whenAtAndEveryExpressionAreProvided__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .payload(p -> p.property("a", v -> v.stringValue("prop")))
                        .scheduled(s -> s
                                .at(at -> at.hourOfDay(8L).minuteOfHours(30L))
                                .every(e -> e.minutes(2L).startingAt(UTC.now()))
                        )
                        .build()).validate(),
                is(invalidSpec("cannot provide both an at and an every expression"))
        );
    }

    @Test
    public void givenAtExpression__whenNoAtFieldProvided__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().build()))
                        .build()).validate(),
                is(invalidSpec("when providing an at expression, must at least provide one at field"))
        );
    }

    @Test
    public void givenEveryExpression__whenNoStartingAtField__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.minutes(2L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, must at least provide a starting-at field"))
        );
    }

    @Test
    public void givenEveryExpression__whenNoFieldProvided__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now())))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, must provide a field (beside starting-at)"))
        );
    }

    @Test
    public void givenEveryExpression__whenMoreThanOneFieldProvided__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).minutes(2L).hours(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).minutes(2L).days(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).minutes(2L).months(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).minutes(2L).years(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).hours(5L).days(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).hours(5L).months(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).hours(5L).years(5L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).days(2L).months(3L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).days(2L).years(3L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now()).months(2L).years(3L)))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingADayOfYearConstraint__thenMustBeIn1_366Range() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfYear(5L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfYear(0L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a day-of-year constraint, must be in 1-366 range"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfYear(367L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a day-of-year constraint, must be in 1-366 range"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingAnHourOfDayConstraint__thenMustBeIn0_23Range() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().hourOfDay(10L)
                                .minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().hourOfDay(-1L)
                                .minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a hour-of-day constraint, must be in 0-23 range"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().hourOfDay(24L)
                                .minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a hour-of-day constraint, must be in 0-23 range"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingAMinuteOfHourConstraint__thenMustBeIn0_23Range() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().minuteOfHours(-1L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a minute-of-hours constraint, must be in 0-59 range"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().minuteOfHours(60L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a minute-of-hours constraint, must be in 0-59 range"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingADayOfMonthConstraint__thenMustBeIn1_31Range() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(0L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a day-of-month constraint, must be in 1-31 range"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(32L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("when providing a day-of-month constraint, must be in 1-31 range"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingOneDayConstraint__thenCannotProvideTheOthers() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfWeek(At.DayOfWeek.SATURDAY)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfYear(120L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );

        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L).dayOfWeek(At.DayOfWeek.SATURDAY)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L).dayOfYear(120L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfWeek(At.DayOfWeek.SATURDAY).dayOfYear(120L)
                                .hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another"))
        );
    }

    @Test
    public void givenAtExpression__whenProvidingHourOfDay__thenMustProvideMinuteOfHour() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().hourOfDay(10L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing an hour-of-day, must provide a minute-of-hour"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
    }

    @Test
    public void givenAtExpression__whenProvidingDayOfMonth__thenMustProvideHourOfDay_andProvideMinuteOfHour() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing a day-of-month, must provide an hour-of-day and a minute-of-hour"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L).hourOfDay(10L).build()))
                        .build()).validate(),
                is(invalidSpec("for an at expression, when providing a day-of-month, must provide an hour-of-day and a minute-of-hour"))
        );
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.at(At.builder().dayOfMonth(5L).hourOfDay(10L).minuteOfHours(32L).build()))
                        .build()).validate(),
                is(validSpec())
        );
    }
}
