package org.codingmatters.poom.crons.domain.trigger;

public class TriggerResult {
    private final boolean success;

    public TriggerResult(boolean success) {
        this.success = success;
    }

    public boolean success() {
        return success;
    }
}
