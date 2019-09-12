package org.codingmatters.poom.crons.domain.trigger;

import java.util.Objects;

public class TriggerResult {
    private final boolean success;
    private final boolean gone;

    public TriggerResult(boolean success, boolean gone) {
        this.success = success;
        this.gone = gone;
    }

    public TriggerResult(boolean success) {
        this(success, false);
    }

    public boolean success() {
        return success;
    }

    public boolean gone() {
        return gone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerResult that = (TriggerResult) o;
        return success == that.success &&
                gone == that.gone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, gone);
    }

    @Override
    public String toString() {
        return "TriggerResult{" +
                "success=" + success +
                ", gone=" + gone +
                '}';
    }
}
