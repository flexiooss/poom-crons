package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.crons.domain.selector.expression.EveryDateTimeTaskSelector;
import org.codingmatters.poom.services.logging.CategorizedLogger;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class DateTimeTaskSelector implements TaskSelector {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(DateTimeTaskSelector.class);

    public static DateTimeTaskSelector minutesPrecision(LocalDateTime atTime) {
        return new DateTimeTaskSelector(atTime, ChronoUnit.MINUTES);
    }

    public static DateTimeTaskSelector secondsPrecision(LocalDateTime atTime) {
        return new DateTimeTaskSelector(atTime, ChronoUnit.SECONDS);
    }

    private final LocalDateTime atTime;
    private final ChronoUnit precision;
    private final EveryDateTimeTaskSelector everyTaskSelector;

    private DateTimeTaskSelector(LocalDateTime atTime, ChronoUnit precision) {
        this.atTime = atTime;
        this.precision = precision;

        if(!Arrays.asList(ChronoUnit.MINUTES, ChronoUnit.SECONDS).contains(precision)) {
            throw new InstantiationError("precision : " + precision + " is not supported");
        }

        this.everyTaskSelector = new EveryDateTimeTaskSelector(this.atTime, this.precision);
    }

    @Override
    public boolean selectable(TaskSpec spec) {
        if(spec.opt().scheduled().at().isPresent()) {
            return this.selectableAt(spec.scheduled().at());
        }
        if(spec.opt().scheduled().every().isPresent()) {
            return this.everyTaskSelector.selectable(spec.scheduled().every());
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

    static public boolean sameDayOfWeek(At.DayOfWeek atDay, DayOfWeek javaDay) {
        return atDay.name().equals(javaDay.name());
    }
}
