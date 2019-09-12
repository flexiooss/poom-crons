package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.AccountCrontabGetRequest;
import org.codingmatters.poom.crons.crontab.api.AccountCrontabGetResponse;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskListTest {

    protected Repository<Task, Void> repository = new TestTaskRepository();

    @Test
    public void givenManyTasks__whenRequestingMiddleRange__then206() throws Exception {
        this.createTasks(500);

        AccountCrontabGetResponse actual = new TaskList(account -> repository).apply(AccountCrontabGetRequest.builder().account("a").range("100-199").build());

        actual.opt().status206().orElseThrow(() -> new AssertionError("expected 206, git " + actual));
        assertThat(actual.status206().contentRange(), is("Task 100-199/500"));
        assertThat(actual.status206().acceptRange(), is("Task 1000"));

        assertThat(actual.status206().payload().size(), is(100));
        for (int i = 0; i < 100; i++) {
            assertThat(actual.status206().payload().get(i).spec().url(), is("task-" + (100 + i)));
        }
    }

    @Test
    public void givenManyTasks__whenRequestingLastRange__then200() throws Exception {
        this.createTasks(500);

        AccountCrontabGetResponse actual = new TaskList(account -> repository).apply(AccountCrontabGetRequest.builder().account("a").range("400-499").build());

        actual.opt().status200().orElseThrow(() -> new AssertionError("expected 200, git " + actual));
        assertThat(actual.status200().contentRange(), is("Task 400-499/500"));
        assertThat(actual.status200().acceptRange(), is("Task 1000"));

        assertThat(actual.status200().payload().size(), is(100));
        for (int i = 0; i < 100; i++) {
            assertThat(actual.status200().payload().get(i).spec().url(), is("task-" + (400 + i)));
        }
    }

    @Test
    public void givenManyTasks__whenRequestingCompleteRange__then200() throws Exception {
        this.createTasks(50);

        AccountCrontabGetResponse actual = new TaskList(account -> repository).apply(AccountCrontabGetRequest.builder().account("a").range("0-99").build());

        actual.opt().status200().orElseThrow(() -> new AssertionError("expected 200, git " + actual));
        assertThat(actual.status200().contentRange(), is("Task 0-49/50"));
        assertThat(actual.status200().acceptRange(), is("Task 1000"));

        assertThat(actual.status200().payload().size(), is(50));
        for (int i = 0; i < 50; i++) {
            assertThat(actual.status200().payload().get(i).spec().url(), is("task-" + i));
        }
    }

    @Test
    public void givenManyTasks__whenRequestingLongerRangeThanLimit__then206_andLessResultsThanExpected() throws Exception {
        this.createTasks(5000);

        AccountCrontabGetResponse actual = new TaskList(account -> repository).apply(AccountCrontabGetRequest.builder().account("a").range("0-1999").build());

        actual.opt().status206().orElseThrow(() -> new AssertionError("expected 206, git " + actual));

        assertThat(actual.status206().contentRange(), is("Task 0-999/5000"));
        assertThat(actual.status206().acceptRange(), is("Task 1000"));
    }

    @Test
    public void whenIllegalRange__then416() throws Exception {
        AccountCrontabGetResponse actual = new TaskList(account -> repository).apply(AccountCrontabGetRequest.builder().account("a").range("yop").build());

        actual.opt().status416().orElseThrow(() -> new AssertionError("expected 416, git " + actual));

        assertThat(actual.status416().acceptRange(), is("Task 1000"));
        assertThat(actual.status416().contentRange(), is("Task */0"));
    }

    private void createTasks(int count) throws org.codingmatters.poom.services.domain.exceptions.RepositoryException {
        for (int i = 0; i < count; i++) {
            this.repository.create(Task.builder().spec(TaskSpec.builder().url("task-" + i).build()).build());
        }
    }


}