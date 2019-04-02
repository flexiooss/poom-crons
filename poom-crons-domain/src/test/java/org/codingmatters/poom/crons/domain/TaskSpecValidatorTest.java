package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.services.support.date.UTC;
import org.junit.Test;

import static org.codingmatters.poom.crons.domain.TaskSpecValidator.TaskSpecValidation.invalidSpec;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskSpecValidatorTest {

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
    public void givenEveryExpression__whenNoFieldExcept__thenIsInvalid() throws Exception {
        assertThat(
                new TaskSpecValidator(TaskSpec.builder()
                        .url("http://an.url")
                        .scheduled(s -> s.every(e -> e.startingAt(UTC.now())))
                        .build()).validate(),
                is(invalidSpec("when providing an every expression, must at least provide another field than starting-at field"))
        );
    }
}
