package org.codingmatters.poom.crons.domain.trigger;

import java.time.LocalDateTime;

public class TriggerResult {
    private final LocalDateTime triggedAt;
    private final boolean success;

    public TriggerResult(LocalDateTime triggedAt, boolean success) {
        this.triggedAt = triggedAt;
        this.success = success;
    }

    public LocalDateTime triggedAt() {
        return triggedAt;
    }

    public boolean success() {
        return success;
    }
}
