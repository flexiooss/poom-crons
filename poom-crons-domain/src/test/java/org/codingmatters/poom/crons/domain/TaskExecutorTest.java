package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.trigger.TaskTrigger;
import org.codingmatters.poom.crons.domain.trigger.TriggerResult;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.ImmutableEntity;
import org.junit.Test;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.codingmatters.poom.services.tests.DateMatchers.around;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TaskExecutorTest {

    private ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    public static final TaskTrigger SUCCESS = (spec, triggedAt, eventId) -> new TriggerResult(true);
    public static final TaskTrigger FAILURE = (spec, triggedAt, eventId) -> new TriggerResult(false);

    @Test
    public void whenExecutingEmptyTaskList__thenResultIsEmpty() throws Exception {
        List<Entity<Task>> tasks = new LinkedList<>();

        List<Entity<Task>> executed = new TaskExecutor(this.forkJoinPool, SUCCESS).execute(tasks);

        assertThat(executed, is(empty()));
    }

    @Test
    public void givenTriggerSucceeds__whenExecutingOneTask__thenTaskIsExecuted_andMarkedAsSuccess_andErrorCountIsInitializedAt0_andTimestamped() throws Exception {
        List<Entity<Task>> tasks = new LinkedList<>();
        tasks.add(this.entity(Task.builder().build()));

        List<Entity<Task>> executed = new TaskExecutor(this.forkJoinPool, SUCCESS).execute(tasks);

        assertThat(executed, hasSize(1));
        assertThat(executed.get(0).value().success(), is(true));
        assertThat(executed.get(0).value().errorCount(), is(0L));

        assertThat(executed.get(0).value().lastTrig(), is(around(UTC.now())));
    }

    @Test
    public void givenTriggerFails__whenExecutingOneTask__thenTaskIsExecuted_andMarkedAsFailure_andErrorCountIsInitializedAt1_andTimestamped() throws Exception {
        List<Entity<Task>> tasks = new LinkedList<>();
        tasks.add(this.entity(Task.builder().build()));

        List<Entity<Task>> executed = new TaskExecutor(this.forkJoinPool, FAILURE).execute(tasks);

        assertThat(executed, hasSize(1));
        assertThat(executed.get(0).value().success(), is(false));
        assertThat(executed.get(0).value().errorCount(), is(1L));
        assertThat(executed.get(0).value().lastTrig(), is(around(UTC.now())));
    }

    @Test
    public void givenTriggerFails_andTaskAsFailedBefore__whenExecutingOneTask__thenTaskIsExecuted_andMarkedAsFailure_andErrorCountIsIncremented_andTimestamped() throws Exception {
        List<Entity<Task>> tasks = new LinkedList<>();
        tasks.add(this.entity(Task.builder().errorCount(4L).build()));

        List<Entity<Task>> executed = new TaskExecutor(this.forkJoinPool, FAILURE).execute(tasks);

        assertThat(executed, hasSize(1));
        assertThat(executed.get(0).value().success(), is(false));
        assertThat(executed.get(0).value().errorCount(), is(5L));
        assertThat(executed.get(0).value().lastTrig(), is(around(UTC.now())));
    }

    private Entity<Task> entity(Task task) {
        return new ImmutableEntity<>(UUID.randomUUID().toString(), BigInteger.ZERO, task);
    }
}