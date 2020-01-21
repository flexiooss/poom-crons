package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;
import org.codingmatters.poom.services.logging.CategorizedLogger;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class DateTimeTaskSelector implements TaskSelector {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(DateTimeTaskSelector.class);

    private final LocalDateTime atTime;
    private final ChronoUnit precision;

    public DateTimeTaskSelector(LocalDateTime atTime) {
        this(atTime, ChronoUnit.MINUTES);
    }
    public DateTimeTaskSelector(LocalDateTime atTime, ChronoUnit precision) {
        this.atTime = atTime;
        this.precision = precision;

        if(!Arrays.asList(ChronoUnit.MINUTES, ChronoUnit.SECONDS).contains(precision)) {
            throw new InstantiationError("precision : " + precision + " is not supported");
        }
    }

    @Override
    public boolean selectable(TaskSpec spec) {
        if(spec.opt().scheduled().at().isPresent()) {
            return this.selectableAt(spec.scheduled().at());
        }
        if(spec.opt().scheduled().every().isPresent()) {
            return this.selectableEvery(spec.scheduled().every());
        }
        return false;
    }

    private boolean selectableAt(At at) {
        if(at.opt().dayOfWeek().isPresent() && ! this.sameDayOfWeek(at.dayOfWeek(), this.atTime.getDayOfWeek())) {
            return false;
        }
        if(at.opt().dayOfMonth().isPresent() && at.dayOfMonth() != this.atTime.getDayOfMonth()) {
            return false;
        }
        if(at.opt().dayOfYear().isPresent() && at.dayOfYear() != this.atTime.getDayOfYear()) {
            return false;
        }
        if(at.opt().hourOfDay().isPresent() && at.hourOfDay() != this.atTime.getHour()) {
            return false;
        }
        if(at.opt().minuteOfHours().isPresent() && at.minuteOfHours() != this.atTime.getMinute()) {
            return false;
        }
        return true;
    }

    private boolean selectableEvery(Every every) {
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

    static public boolean sameDayOfWeek(At.DayOfWeek atDay, DayOfWeek javaDay) {
        return atDay.name().equals(javaDay.name());
    }
}
