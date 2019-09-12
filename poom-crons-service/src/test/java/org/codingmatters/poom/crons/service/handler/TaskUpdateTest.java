package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskPutRequest;
import org.codingmatters.poom.crons.crontab.api.TaskPutResponse;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskUpdateTest {

    static private TaskSpec VALID_UPDATE = TaskSpec.builder()
            .url("changed")
            .scheduled(scheduled -> scheduled.at(at -> at.minuteOfHours(1L))).build();
    static private TaskSpec INVALID_UPDATE = TaskSpec.builder()
            .build();

    protected Repository<Task, Void> emptyRepo = new TestTaskRepository();
    protected Repository<Task, Void> notEmptyReposiroy = new TestTaskRepository();

    @Before
    public void setUp() throws Exception {
        this.notEmptyReposiroy.createWithId("task-id", Task.builder().spec(spec -> spec.url("original")).build());
    }

    @Test
    public void givenRepositoryIsEmpty__whenUpdatingTask__then404() throws Exception {
        TaskPutResponse actual = new TaskUpdate(account -> this.emptyRepo).apply(TaskPutRequest.builder().account("account").taskId("task-id").payload(VALID_UPDATE).build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expected 404 got " + actual));
    }

    @Test
    public void givenNotEmptyRepository__whenUpdatingUnexistingTask__then404() throws Exception {
        TaskPutResponse actual = new TaskUpdate(account -> this.notEmptyReposiroy).apply(TaskPutRequest.builder().account("account").taskId("unexisting-task-id").payload(VALID_UPDATE).build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expected 404 got " + actual));
    }

    @Test
    public void givenNotEmptyRepository__whenUpdatingExistingTask_withValidUpdate__then200_andTaskUdated() throws Exception {
        TaskPutResponse actual = new TaskUpdate(account -> this.notEmptyReposiroy).apply(TaskPutRequest.builder().account("account").taskId("task-id").payload(VALID_UPDATE).build());

        actual.opt().status200().orElseThrow(() -> new AssertionError("expected 200 got " + actual));
        assertThat(actual.status200().xEntityId(), is("task-id"));
        assertThat(actual.status200().payload().spec().url(), is("changed"));

        assertThat(this.notEmptyReposiroy.all(0, 0).total(), is(1L));
        assertThat(this.notEmptyReposiroy.retrieve("task-id").value().spec().url(), is("changed"));
    }

    @Test
    public void givenNotEmptyRepository__whenUpdatingExistingTask_withInvalidUpdate__then400() throws Exception {
        TaskPutResponse actual = new TaskUpdate(account -> this.notEmptyReposiroy).apply(TaskPutRequest.builder().account("account").taskId("task-id").payload(INVALID_UPDATE).build());

        actual.opt().status400().orElseThrow(() -> new AssertionError("expected 400 got " + actual));
    }
}