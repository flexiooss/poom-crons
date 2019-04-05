package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;

@FunctionalInterface
public interface TaskSelector {
    boolean selectable(TaskSpec spec);
}
