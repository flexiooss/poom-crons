package org.codingmatters.poom.crons.domain.selector.expression;

import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.date.UTC;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public class EveryDateTimeTaskSelector {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(EveryDateTimeTaskSelector.class);

    private final LocalDateTime atTime;
    private final ChronoUnit precision;

    public EveryDateTimeTaskSelector(LocalDateTime atTime, ChronoUnit precision) {
        this.atTime = atTime;
        this.precision = precision;
    }

    public boolean selectable(Every every, TimeZone tz) {
        LocalDateTime startingAt = this.atPrecision(every.startingAt());
        LocalDateTime now = UTC.at(this.atTime, tz);

        EveryMatcher matcher = new EveryMatcher(now, this.precision);

        if(every.opt().seconds().isPresent()) {
            if(this.precision == ChronoUnit.MINUTES) {
                log.warn("task spec on every x seconds cannot be executed while precision is " + this.precision);
                return false;
            }
            return Math.abs(ChronoUnit.SECONDS.between(now, startingAt)) % every.seconds() == 0L;
        }
        if(every.opt().minutes().isPresent()) {
            return matcher.secondMatches(startingAt) &&
                    Math.abs(ChronoUnit.MINUTES.between(now, startingAt)) % every.minutes() == 0L;
        }
        if(every.opt().hours().isPresent()) {
            return matcher.minuteMatches(startingAt) &&
                    Math.abs(ChronoUnit.HOURS.between(now, startingAt)) % every.hours() == 0L;
        }
        if(every.opt().days().isPresent()) {
            return matcher.hourMatches(startingAt) &&
                    Math.abs(ChronoUnit.DAYS.between(now, startingAt)) % every.days() == 0L;
        }
        if(every.opt().months().isPresent()) {
            return matcher.dayOfMonthMatches(startingAt) &&
                    Math.abs(ChronoUnit.MONTHS.between(now, startingAt)) % every.months() == 0L;
        }
        if(every.opt().years().isPresent()) {
            return matcher.monthMatches(startingAt) &&
                    Math.abs(ChronoUnit.YEARS.between(now, startingAt)) % every.years() == 0L;
        }

        return false;
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
