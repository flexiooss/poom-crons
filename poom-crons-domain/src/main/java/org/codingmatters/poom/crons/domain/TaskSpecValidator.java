package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;

import java.util.Objects;

public class TaskSpecValidator {
    private final TaskSpec spec;

    public TaskSpecValidator(TaskSpec spec) {
        this.spec = spec;
    }

    public TaskSpecValidation validate() {
        if(! this.spec.opt().url().isPresent()) {
            return TaskSpecValidation.invalidSpec("no url provided");
        }
        if(this.spec.opt().scheduled().at().isPresent() && this.spec.opt().scheduled().every().isPresent()) {
            return TaskSpecValidation.invalidSpec("cannot provide both an at and an every expression");
        }
        if(this.spec.opt().scheduled().at().isPresent()) {
            if(! (this.spec.opt().scheduled().at().minuteOfHours().isPresent() ||
                    this.spec.opt().scheduled().at().hourOfDay().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfWeek().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfMonth().isPresent() ||
                    this.spec.opt().scheduled().at().dayOfYear().isPresent())
            ) {
                return TaskSpecValidation.invalidSpec("when providing an at expression, must at least provide one at field");
            }
        }
        if(this.spec.opt().scheduled().every().isPresent()) {
            if(! this.spec.opt().scheduled().every().startingAt().isPresent()) {
                return TaskSpecValidation.invalidSpec("when providing an every expression, must at least provide a starting-at field");
            }
            if(!(this.spec.opt().scheduled().every().minutes().isPresent() ||
                    this.spec.opt().scheduled().every().hours().isPresent() ||
                    this.spec.opt().scheduled().every().days().isPresent() ||
                    this.spec.opt().scheduled().every().months().isPresent() ||
                    this.spec.opt().scheduled().every().years().isPresent())) {
                return TaskSpecValidation.invalidSpec("when providing an every expression, must at least provide another field than starting-at field");
            }
        }
        return TaskSpecValidation.validSpec();
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
