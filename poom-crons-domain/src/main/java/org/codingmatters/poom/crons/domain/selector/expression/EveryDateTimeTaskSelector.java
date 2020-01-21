package org.codingmatters.poom.crons.domain.selector.expression;

import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.codingmatters.poom.services.logging.CategorizedLogger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EveryDateTimeTaskSelector {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(EveryDateTimeTaskSelector.class);

    private final LocalDateTime atTime;
    private final ChronoUnit precision;

    public EveryDateTimeTaskSelector(LocalDateTime atTime, ChronoUnit precision) {
        this.atTime = atTime;
        this.precision = precision;
    }

    public boolean selectable(Every every) {
        LocalDateTime startingAt = this.atPrecision(every.startingAt());
        if(every.opt().seconds().isPresent()) {
            if(this.precision == ChronoUnit.MINUTES) {
                log.warn("task spec on every x seconds cannot be executed while precision is " + this.precision);
                return false;
            }
            return Math.abs(ChronoUnit.SECONDS.between(this.atTime, startingAt)) % every.seconds() == 0L;
        }
        if(every.opt().minutes().isPresent()) {
            return this.secondMatches(startingAt) &&
                    Math.abs(ChronoUnit.MINUTES.between(this.atTime, startingAt)) % every.minutes() == 0L;
        }
        if(every.opt().hours().isPresent()) {
            return this.minuteMatches(startingAt) &&
                    Math.abs(ChronoUnit.HOURS.between(this.atTime, startingAt)) % every.hours() == 0L;
        }
        if(every.opt().days().isPresent()) {
            return this.hourMatches(startingAt) &&
                    Math.abs(ChronoUnit.DAYS.between(this.atTime, startingAt)) % every.days() == 0L;
        }
        if(every.opt().months().isPresent()) {
            return this.dayOfMonthMatches(startingAt) &&
                    Math.abs(ChronoUnit.MONTHS.between(this.atTime, startingAt)) % every.months() == 0L;
        }
        if(every.opt().years().isPresent()) {
            return this.monthMatches(startingAt) &&
                    Math.abs(ChronoUnit.YEARS.between(this.atTime, startingAt)) % every.years() == 0L;
        }

        return false;
    }

    private boolean monthMatches(LocalDateTime startingAt) {
        return this.dayOfMonthMatches(startingAt) && this.atTime.getMonthValue() == startingAt.getMonthValue();
    }

    private boolean dayOfMonthMatches(LocalDateTime startingAt) {
        return this.hourMatches(startingAt) && this.atTime.getDayOfMonth() == startingAt.getDayOfMonth();
    }

    private boolean hourMatches(LocalDateTime startingAt) {
        return this.minuteMatches(startingAt) && this.atTime.getHour() == startingAt.getHour();
    }

    private boolean minuteMatches(LocalDateTime startingAt) {
        return this.secondMatches(startingAt) && this.atTime.getMinute() == startingAt.getMinute();
    }

    private boolean secondMatches(LocalDateTime startingAt) {
        if(this.precision.equals(ChronoUnit.MINUTES)) {
            return true;
        }
        return this.atTime.getSecond() == startingAt.getSecond();
    }

    private LocalDateTime atPrecision(LocalDateTime dt) {
        if(this.precision.equals(ChronoUnit.MINUTES)) {
            return dt.withNano(0).withSecond(0);
        } else if(this.precision.equals(ChronoUnit.SECONDS)) {
            return dt.withNano(0);
        } else {
            return dt;
        }
    }
}
