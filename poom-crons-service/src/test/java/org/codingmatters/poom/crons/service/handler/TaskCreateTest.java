package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.AccountCrontabPostRequest;
import org.codingmatters.poom.crons.crontab.api.AccountCrontabPostResponse;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.domain.entities.Entity;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskCreateTest {
    static private TaskSpec VALID_CREATE = TaskSpec.builder()
            .url("changed")
            .scheduled(scheduled -> scheduled.at(at -> at.minuteOfHours(1L))).build();
    static private TaskSpec INVALID_CREATE = TaskSpec.builder()
            .build();

    protected Repository<Task, Void> repository = new TestTaskRepository();

    @Test
    public void givenCreationIsValid__whenCreating__then200_andTaskCreated() throws Exception {
        AccountCrontabPostResponse actual = new TaskCreate(account -> repository).apply(AccountCrontabPostRequest.builder().account("a").payload(VALID_CREATE).build());

        actual.opt().status201().orElseThrow(() -> new AssertionError("expected 201, got " + actual));

        Entity<Task> task = this.repository.retrieve(actual.status201().xEntityId());
        assertThat(actual.status201().payload(), is(task.value()));
        assertThat(actual.status201().payload().spec(), is(VALID_CREATE));
    }

    @Test
    public void givenCreationIsInvalid__whenCreating__then400() throws Exception {
        AccountCrontabPostResponse actual = new TaskCreate(account -> repository).apply(AccountCrontabPostRequest.builder().account("a").payload(INVALID_CREATE).build());

        actual.opt().status400().orElseThrow(() -> new AssertionError("expected 400, got " + actual));
    }
}