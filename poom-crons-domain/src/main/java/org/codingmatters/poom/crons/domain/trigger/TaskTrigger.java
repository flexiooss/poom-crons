package org.codingmatters.poom.crons.domain.trigger;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;

@FunctionalInterface
public interface TaskTrigger {
    TriggerResult trig(TaskSpec spec);
}
