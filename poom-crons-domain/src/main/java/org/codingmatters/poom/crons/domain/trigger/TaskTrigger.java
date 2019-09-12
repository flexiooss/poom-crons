package org.codingmatters.poom.crons.domain.trigger;

import org.codingmatters.poom.crons.crontab.api.types.Task;

import java.time.LocalDateTime;

@FunctionalInterface
public interface TaskTrigger {
    TriggerResult trig(Task spec, LocalDateTime triggedAt, String eventId);
}
