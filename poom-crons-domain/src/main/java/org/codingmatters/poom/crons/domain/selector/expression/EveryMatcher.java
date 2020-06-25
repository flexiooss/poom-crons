package org.codingmatters.poom.crons.domain.selector.expression;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EveryMatcher {
    private final LocalDateTime atTime;
    private final ChronoUnit precision;

    public EveryMatcher(LocalDateTime atTime, ChronoUnit precision) {
        this.atTime = atTime;
        this.precision = precision;
    }


    public boolean monthMatches(LocalDateTime startingAt) {
        return this.dayOfMonthMatches(startingAt) && this.atTime.getMonthValue() == startingAt.getMonthValue();
    }

    public boolean dayOfMonthMatches(LocalDateTime startingAt) {
        return this.hourMatches(startingAt) && this.atTime.getDayOfMonth() == startingAt.getDayOfMonth();
    }

    public boolean hourMatches(LocalDateTime startingAt) {
        return this.minuteMatches(startingAt) && this.atTime.getHour() == startingAt.getHour();
    }

    public boolean minuteMatches(LocalDateTime startingAt) {
        return this.secondMatches(startingAt) && this.atTime.getMinute() == startingAt.getMinute();
    }

    public boolean secondMatches(LocalDateTime startingAt) {
        if(this.precision.equals(ChronoUnit.MINUTES)) {
            return true;
        }
        return this.atTime.getSecond() == startingAt.getSecond();
    }
}
