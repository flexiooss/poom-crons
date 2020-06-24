package org.codingmatters.poom.crons.domain.selector;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.scheduled.At;
import org.codingmatters.poom.crons.domain.selector.expression.EveryDateTimeTaskSelector;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.Env;
import org.codingmatters.poom.services.support.date.UTC;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.TimeZone;

public class DateTimeTaskSelector implements TaskSelector {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(DateTimeTaskSelector.class);

    private static final TimeZone DEFAULT_DEFAULT_TZ = TimeZone.getTimeZone(
            Env.optional("TASK_SELECTOR_DEFAULT_TIMEZONE")
                    .orElseGet(() -> new Env.Var("Europe/Paris"))
                    .asString()
    );

    public static DateTimeTaskSelector minutesPrecision(LocalDateTime atTime, TimeZone defaultTimeZone) {
        return new DateTimeTaskSelector(atTime, ChronoUnit.MINUTES, defaultTimeZone);
    }
    public static DateTimeTaskSelector minutesPrecision(LocalDateTime atTime) {
        return minutesPrecision(atTime, DEFAULT_DEFAULT_TZ);
    }

    public static DateTimeTaskSelector secondsPrecision(LocalDateTime atTime) {
        return secondsPrecision(atTime, DEFAULT_DEFAULT_TZ);
    }
    public static DateTimeTaskSelector secondsPrecision(LocalDateTime atTime, TimeZone defaultTimeZone) {
        return new DateTimeTaskSelector(atTime, ChronoUnit.SECONDS, defaultTimeZone);
    }

    private final LocalDateTime atTime;
    private final ChronoUnit precision;
    private final EveryDateTimeTaskSelector everyTaskSelector;
    private final TimeZone defaultTimeZone;

    private DateTimeTaskSelector(LocalDateTime atTime, ChronoUnit precision, TimeZone defaultTimeZone) {
        this.atTime = atTime;
        this.precision = precision;
        this.defaultTimeZone = defaultTimeZone;

        if(!Arrays.asList(ChronoUnit.MINUTES, ChronoUnit.SECONDS).contains(precision)) {
            throw new InstantiationError("precision : " + precision + " is not supported");
        }

        this.everyTaskSelector = new EveryDateTimeTaskSelector(this.atTime, this.precision);
    }

    @Override
    public boolean selectable(TaskSpec spec) {
        TimeZone tz = this.taskTimeZone(spec);
        if(spec.opt().scheduled().at().isPresent()) {
            return this.selectableAt(spec.scheduled().at(), tz);
        }
        if(spec.opt().scheduled().every().isPresent()) {
            return this.everyTaskSelector.selectable(spec.scheduled().every(), tz);
        }
        return false;
    }

    private TimeZone taskTimeZone(TaskSpec spec) {
        if(spec.opt().timezone().isPresent()) {
            return TimeZone.getTimeZone(spec.timezone());
        } else {
            return this.defaultTimeZone;
        }
    }

    private boolean selectableAt(At at, TimeZone tz) {
        LocalDateTime now = UTC.at(this.atTime, tz);
        if(at.opt().dayOfWeek().isPresent() && ! this.sameDayOfWeek(at.dayOfWeek(), now.getDayOfWeek())) {
            return false;
        }
        if(at.opt().dayOfMonth().isPresent() && at.dayOfMonth() != now.getDayOfMonth()) {
            return false;
        }
        if(at.opt().dayOfYear().isPresent() && at.dayOfYear() != now.getDayOfYear()) {
            return false;
        }
        if(at.opt().hourOfDay().isPresent() && at.hourOfDay() != now.getHour()) {
            return false;
        }
        if(at.opt().minuteOfHours().isPresent() && at.minuteOfHours() != now.getMinute()) {
            return false;
        }
        return true;
    }

    static public boolean sameDayOfWeek(At.DayOfWeek atDay, DayOfWeek javaDay) {
        return atDay.name().equals(javaDay.name());
    }
}
