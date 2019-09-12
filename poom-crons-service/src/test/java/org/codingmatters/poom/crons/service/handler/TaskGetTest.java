package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskGetRequest;
import org.codingmatters.poom.crons.crontab.api.TaskGetResponse;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TaskGetTest {

    protected Repository<Task, Void> emptyRepo = new TestTaskRepository();
    protected Repository<Task, Void> notEmptyReposiroy = new TestTaskRepository();

    @Before
    public void setUp() throws Exception {
        this.notEmptyReposiroy.createWithId("existing-task", Task.builder().build());
    }

    @Test
    public void givenRepositoryIsEmpty__whenRequestingATask__then404() throws Exception {
        TaskGetResponse actual = new TaskGet(account -> emptyRepo).apply(TaskGetRequest.builder().account("account").taskId("task-id").build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expecting 404 got " + actual));
    }

    @Test
    public void givenRepositoryIsNotEmpty__whenRequestingNotExistingId__then404() throws Exception {
        TaskGetResponse actual = new TaskGet(account -> notEmptyReposiroy).apply(TaskGetRequest.builder().account("account").taskId("not-existing-task").build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expecting 404 got " + actual));
    }

    @Test
    public void givenRepositoryIsNotEmpty__whenRequestingExistingId_andUpdateIsValid__then200() throws Exception {
        TaskGetResponse actual = new TaskGet(account -> notEmptyReposiroy).apply(TaskGetRequest.builder().account("account").taskId("existing-task").build());

        actual.opt().status200().orElseThrow(() -> new AssertionError("expecting 200 got " + actual));
        assertThat(actual.status200().xEntityId(), is("existing-task"));
        assertThat(actual.status200().payload(), is(notNullValue()));
    }
}