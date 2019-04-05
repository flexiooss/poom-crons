package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.Every;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeTaskSelector implements TaskSelector {
    private final LocalDateTime atTime;

    public DateTimeTaskSelector(LocalDateTime atTime) {
        this.atTime = atTime;
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
        if(every.opt().minutes().isPresent()) {
            return Math.abs(ChronoUnit.MINUTES.between(this.atTime, every.startingAt())) % every.minutes() == 0L;
        }
        if(every.opt().hours().isPresent()) {
            return this.atTime.getMinute() == every.startingAt().getMinute() &&
                    Math.abs(ChronoUnit.HOURS.between(this.atTime, every.startingAt())) % every.hours() == 0L;
        }
        if(every.opt().days().isPresent()) {
            return this.atTime.getMinute() == every.startingAt().getMinute() &&
                    this.atTime.getHour() == every.startingAt().getHour() &&
                    Math.abs(ChronoUnit.DAYS.between(this.atTime, every.startingAt())) % every.days() == 0L;
        }
        if(every.opt().months().isPresent()) {
            return this.atTime.getMinute() == every.startingAt().getMinute() &&
                    this.atTime.getHour() == every.startingAt().getHour() &&
                    this.atTime.getDayOfMonth() == every.startingAt().getDayOfMonth() &&
                    Math.abs(ChronoUnit.MONTHS.between(this.atTime, every.startingAt())) % every.months() == 0L;
        }
        if(every.opt().years().isPresent()) {
            return this.atTime.getMinute() == every.startingAt().getMinute() &&
                    this.atTime.getHour() == every.startingAt().getHour() &&
                    this.atTime.getDayOfMonth() == every.startingAt().getDayOfMonth() &&
                    this.atTime.getMonthValue() == every.startingAt().getMonthValue() &&
                    Math.abs(ChronoUnit.YEARS.between(this.atTime, every.startingAt())) % every.years() == 0L;
        }

        return false;
    }

    static public boolean sameDayOfWeek(At.DayOfWeek atDay, DayOfWeek javaDay) {
        return atDay.name().equals(javaDay.name());
    }
}
