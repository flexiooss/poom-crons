package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;

public interface TaskSelector {
    boolean selectable(TaskSpec spec);
}
