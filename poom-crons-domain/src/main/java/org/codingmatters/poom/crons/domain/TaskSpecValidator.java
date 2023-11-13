package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;

import java.util.Objects;
import java.util.Optional;

public class TaskSpecValidator {
    private final TaskSpec spec;

    public TaskSpecValidator(TaskSpec spec) {
        this.spec = spec;
    }

    public TaskSpecValidation validate() {
        if(this.spec == null) {
            return TaskSpecValidation.invalidSpec("no task spec");
        }
        if(! this.spec.opt().url().isPresent()) {
            return TaskSpecValidation.invalidSpec("no url provided");
        }
        if(this.spec.opt().scheduled().at().isPresent() && this.spec.opt().scheduled().every().isPresent()) {
            return TaskSpecValidation.invalidSpec("cannot provide both an at and an every expression");
        }

        if(! this.spec.opt().scheduled().at().isPresent() && ! this.spec.opt().scheduled().every().isPresent()) {
            return TaskSpecValidation.invalidSpec("must provide an expression (one of at, every)");
        }

        if(this.spec.opt().scheduled().at().isPresent()) {
            if(! (this.spec.opt().scheduled().at().secondOfMinute().isPresent() ||
                    this.spec.opt().scheduled().at().minuteOfHours().isPresent() ||
                    this.spec.opt().scheduled().at().hourOfDay().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfWeek().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfMonth().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfYear().isPresent())
            ) {
                return TaskSpecValidation.invalidSpec("when providing an at expression, must at least provide one at field");
            }

            if(this.spec.opt().scheduled().at().dayOfWeek().isPresent() &&
                    (this.spec.scheduled().opt().at().dayOfMonth().isPresent() || this.spec.scheduled().opt().at().dayOfYear().isPresent())) {
                return TaskSpecValidation.invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another");
            }
            if(this.spec.opt().scheduled().at().dayOfMonth().isPresent() &&
                    (this.spec.scheduled().opt().at().dayOfWeek().isPresent() || this.spec.scheduled().opt().at().dayOfYear().isPresent())) {
                return TaskSpecValidation.invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another");
            }
            if(this.spec.opt().scheduled().at().dayOfYear().isPresent() &&
                    (this.spec.scheduled().opt().at().dayOfWeek().isPresent() || this.spec.scheduled().opt().at().dayOfMonth().isPresent())) {
                return TaskSpecValidation.invalidSpec("for an at expression, when providing a day-of constraint, cannot provide another");
            }

            if(this.spec.opt().scheduled().at().dayOfMonth().isPresent() &&
                    (! this.spec.opt().scheduled().at().hourOfDay().isPresent() || ! this.spec.opt().scheduled().at().minuteOfHours().isPresent())) {
                return TaskSpecValidation.invalidSpec("for an at expression, when providing a day-of-month, must provide an hour-of-day and a minute-of-hour");
            }
            if(this.spec.opt().scheduled().at().hourOfDay().isPresent() &&
                    ! this.spec.opt().scheduled().at().minuteOfHours().isPresent()) {
                return TaskSpecValidation.invalidSpec("for an at expression, when providing an hour-of-day, must provide a minute-of-hour");
            }

            if(this.spec.opt().scheduled().at().dayOfYear().isPresent()) {
                if(this.spec.scheduled().at().dayOfYear() < 1 || this.spec.scheduled().at().dayOfYear() > 366) {
                    return TaskSpecValidation.invalidSpec("when providing a day-of-year constraint, must be in 1-366 range");
                }
            }
            if(this.spec.opt().scheduled().at().dayOfMonth().isPresent()) {
                if(this.spec.scheduled().at().dayOfMonth() < 1 || this.spec.scheduled().at().dayOfMonth() > 31) {
                    return TaskSpecValidation.invalidSpec("when providing a day-of-month constraint, must be in 1-31 range");
                }
            }
            if(this.spec.opt().scheduled().at().hourOfDay().isPresent()) {
                if(this.spec.scheduled().at().hourOfDay() < 0 || this.spec.scheduled().at().hourOfDay() > 23) {
                    return TaskSpecValidation.invalidSpec("when providing a hour-of-day constraint, must be in 0-23 range");
                }
            }
            if(this.spec.opt().scheduled().at().minuteOfHours().isPresent()) {
                if(this.spec.scheduled().at().minuteOfHours() < 0 || this.spec.scheduled().at().minuteOfHours() > 59) {
                    return TaskSpecValidation.invalidSpec("when providing a minute-of-hours constraint, must be in 0-59 range");
                }
            }
            if(this.spec.opt().scheduled().at().secondOfMinute().isPresent()) {
                if(this.spec.scheduled().at().secondOfMinute() < 0 || this.spec.scheduled().at().secondOfMinute() > 59) {
                    return TaskSpecValidation.invalidSpec("when providing a second-of-minute constraint, must be in 0-59 range");
                }
            }
        }
        if(this.spec.opt().scheduled().every().isPresent()) {
            if(! this.spec.opt().scheduled().every().startingAt().isPresent()) {
                return TaskSpecValidation.invalidSpec("when providing an every expression, must at least provide a starting-at field");
            }
            int fieldDefined = 0;
            fieldDefined += this.spec.opt().scheduled().every().seconds().isPresent() ? 1 : 0;
            fieldDefined += this.spec.opt().scheduled().every().minutes().isPresent() ? 1 : 0;
            fieldDefined += this.spec.opt().scheduled().every().hours().isPresent() ? 1 : 0;
            fieldDefined += this.spec.opt().scheduled().every().days().isPresent() ? 1 : 0;
            fieldDefined += this.spec.opt().scheduled().every().months().isPresent() ? 1 : 0;
            fieldDefined += this.spec.opt().scheduled().every().years().isPresent() ? 1 : 0;
            if(fieldDefined == 0) {
                return TaskSpecValidation.invalidSpec("when providing an every expression, must provide a field (beside starting-at)");
            } else if(fieldDefined > 1) {
                return TaskSpecValidation.invalidSpec("when providing an every expression, cannot provide more than one field (beside starting-at)");
            }

            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().seconds())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a seconds field, must be strictly greater than 0");
            }
            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().minutes())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a minutes field, must be strictly greater than 0");
            }
            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().hours())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a hours field, must be strictly greater than 0");
            }
            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().days())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a days field, must be strictly greater than 0");
            }
            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().months())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a months field, must be strictly greater than 0");
            }
            if(this.invalidEveryFieldValue(this.spec.opt().scheduled().every().years())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression with a years field, must be strictly greater than 0");
            }
        }
        return TaskSpecValidation.validSpec();
    }

    private boolean invalidEveryFieldValue(Optional<Long> aLong) {
        return aLong.isPresent() && aLong.get() <= 0L;
    }

    static public class TaskSpecValidation {

        static public TaskSpecValidation validSpec() {
            return new TaskSpecValidation(true, null);
        }

        static public TaskSpecValidation invalidSpec(String message) {
            return new TaskSpecValidation(false, message);
        }

        private final boolean valid;
        private final String validationMessage;

        private TaskSpecValidation(boolean valid, String validationMessage) {
            this.valid = valid;
            this.validationMessage = validationMessage;
        }

        public boolean valid() {
            return valid;
        }

        public String validationMessage() {
            return validationMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaskSpecValidation that = (TaskSpecValidation) o;
            return valid == that.valid &&
                    Objects.equals(validationMessage, that.validationMessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(valid, validationMessage);
        }

        @Override
        public String toString() {
            return "TaskSpecValidation{" +
                    "valid=" + valid +
                    ", validationMessage='" + validationMessage + '\'' +
                    '}';
        }
    }
}
