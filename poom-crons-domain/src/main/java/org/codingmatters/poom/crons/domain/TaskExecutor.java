package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.trigger.TaskTrigger;
import org.codingmatters.poom.crons.domain.trigger.TriggerResult;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.poom.services.domain.entities.Entity;
import org.codingmatters.poom.services.domain.entities.ImmutableEntity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class TaskExecutor {

    private final ForkJoinPool forkJoinPool;
    private final TaskTrigger trigger;

    public TaskExecutor(ForkJoinPool forkJoinPool, TaskTrigger trigger) {
        this.forkJoinPool = forkJoinPool;
        this.trigger = trigger;
    }

    public List<Entity<Task>> execute(List<Entity<Task>> tasks) throws ExecutionException, InterruptedException {
        List<Entity<Task>> result = Collections.synchronizedList(new LinkedList<>());

        LocalDateTime triggerdAt = UTC.now();
        String eventId = UUID.randomUUID().toString();

        this.forkJoinPool.submit(() -> tasks.parallelStream().forEach(task -> result.add(this.trig(task, triggerdAt, eventId)))).get();

        return result;
    }

    private Entity<Task> trig(Entity<Task> task, LocalDateTime triggedAt, String eventId) {
        TriggerResult triggerResult = this.trigger.trig(task.value(), triggedAt, eventId);
        if(triggerResult.success()) {
            return new ImmutableEntity<>(task.id(), task.version().add(BigInteger.ONE),
                    task.value()
                            .withLastTrig(UTC.now())
                            .withSuccess(true)
                            .withErrorCount(0L)
            );
        } else {
            return new ImmutableEntity<>(task.id(), task.version().add(BigInteger.ONE),
                    task.value()
                            .withLastTrig(UTC.now())
                            .withSuccess(false)
                            .withErrorCount(task.value().opt().errorCount().orElse(0L) + 1)
            );
        }
    }
}
