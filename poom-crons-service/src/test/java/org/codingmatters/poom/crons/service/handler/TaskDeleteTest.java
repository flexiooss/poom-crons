package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskDeleteRequest;
import org.codingmatters.poom.crons.crontab.api.TaskDeleteResponse;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class TaskDeleteTest {

    protected Repository<Task, Void> emptyRepo = new TestTaskRepository();
    protected Repository<Task, Void> notEmptyReposiroy = new TestTaskRepository();

    @Before
    public void setUp() throws Exception {
        this.notEmptyReposiroy.createWithId("task-id", Task.builder().spec(spec -> spec.url("original")).build());
    }

    @Test
    public void givenEmptyRepository__whenDeleting__then404() throws Exception {
        TaskDeleteResponse actual = new TaskDelete(account -> emptyRepo).apply(TaskDeleteRequest.builder().account("ac").taskId("no-such-task").build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expected 404, got " + actual));
    }

    @Test
    public void givenNotEmptyRepository__whenDeletingUnexistentTask__then404() throws Exception {
        TaskDeleteResponse actual = new TaskDelete(account -> notEmptyReposiroy).apply(TaskDeleteRequest.builder().account("ac").taskId("no-such-task").build());

        actual.opt().status404().orElseThrow(() -> new AssertionError("expected 404, got " + actual));
    }

    @Test
    public void givenNotEmptyRepository__whenDeletingExistingTask__then204_andTaskRemovedFromRepository() throws Exception {
        TaskDeleteResponse actual = new TaskDelete(account -> notEmptyReposiroy).apply(TaskDeleteRequest.builder().account("ac").taskId("task-id").build());

        actual.opt().status204().orElseThrow(() -> new AssertionError("expected 204, got " + actual));
        assertThat(this.notEmptyReposiroy.retrieve("task-is"), is(nullValue()));
    }
}