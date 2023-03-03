package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.codingmatters.poom.services.domain.entities.Entity;
import org.codingmatters.poom.services.domain.entities.PagedEntityList;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CrontabTest {

    private final HashMap<String, Repository<Task, Void>> accountRepositries = new HashMap<>();
    private final Crontab crontab = new Crontab(account -> {
        accountRepositries.computeIfAbsent(account, s -> createAccountRepository());
        return accountRepositries.get(account);
    });

    private InMemoryRepository<Task, Void> createAccountRepository() {
        return new InMemoryRepository<Task, Void>() {
                @Override
                public PagedEntityList<Task> search(Void query, long startIndex, long endIndex) throws RepositoryException {
                    return this.all(startIndex, endIndex);
                }
            };
    }

    private ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    @Test
    public void givenRepositoryForAccount__whenCreatingTask__thenTaskAddedToCrontab() throws Exception {
        Repository<Task, Void> accountRepository = this.crontab.forAccount("my-account");
        Entity<Task> task = accountRepository.create(Task.builder().spec(spec -> spec.url("created")).build());

        assertThat(this.crontab.tasks(), hasSize(1));
        assertThat(this.crontab.tasks().get(0).id(), is("my-account/" + task.id()));
        assertThat(this.crontab.tasks().get(0).value(), is(task.value()));
    }

    @Test
    public void givenRepositoryForAccount__whenUpdatingTask__thenTaskUpdatedInCrontab() throws Exception {
        Repository<Task, Void> accountRepository = this.crontab.forAccount("my-account");
        Entity<Task> task = accountRepository.create(Task.builder().spec(spec -> spec.url("created")).build());

        accountRepository.update(task, task.value().withSpec(TaskSpec.builder().url("modified").build()));

        assertThat(this.crontab.tasks(), hasSize(1));
        assertThat(this.crontab.tasks().get(0).value().spec().url(), is("modified"));
    }

    @Test
    public void givenRepositoryForAccount__whenDeletingTask__thenTaskDeletedInCrontab() throws Exception {
        Repository<Task, Void> accountRepository = this.crontab.forAccount("my-account");
        Entity<Task> task = accountRepository.create(Task.builder().spec(spec -> spec.url("created")).build());

        accountRepository.delete(task);

        assertThat(this.crontab.tasks(), hasSize(0));
    }

    @Test
    public void givenTwoAccount__whenAddingTasksInBoth__thenAllTasksAreInCrontab() throws Exception {
        Repository<Task, Void> account1 = this.crontab.forAccount("my-account-1");
        Repository<Task, Void> account2 = this.crontab.forAccount("my-account-2");

        account1.create(Task.builder().spec(spec -> spec.url("created")).build());
        account2.create(Task.builder().spec(spec -> spec.url("created")).build());

        assertThat(this.crontab.tasks(), hasSize(2));
    }

    @Test
    public void givenAllSelector__whenFilteringSelectableTask__thenAllSelected() throws Exception {
        for (int i = 0; i < 500; i++) {
            this.crontab.forAccount("my-account").create(Task.builder().spec(TaskSpec.builder().url("task-" + i).build()).build());
        }

        List<Entity<Task>> selectable = this.crontab.selectable(spec -> true, this.forkJoinPool);
        assertThat(selectable, hasSize(500));
    }

    @Test
    public void givenNoneSelector__whenFilteringSelectableTask__thenNoneSelected() throws Exception {
        for (int i = 0; i < 500; i++) {
            this.crontab.forAccount("my-account").create(Task.builder().spec(TaskSpec.builder().url("task-" + i).build()).build());
        }

        List<Entity<Task>> selectable = this.crontab.selectable(spec -> false, this.forkJoinPool);
        assertThat(selectable, hasSize(0));
    }

    @Test
    public void givenOneOutOf5Selector__whenFilteringSelectableTask__thenSomeSelected() throws Exception {
        for (int i = 0; i < 500; i++) {
            this.crontab.forAccount("my-account").create(Task.builder().spec(TaskSpec.builder().url("task-" + i).build()).build());
        }

        List<Entity<Task>> selectable = this.crontab.selectable(spec -> Integer.parseInt(spec.url().split("-")[1]) % 5 == 0, this.forkJoinPool);
        assertThat(selectable, hasSize(100));
    }

    @Test
    public void givenTasksAreInDifferentAccountWithSameId__whenUpdatingTasksFromCrontab__thenTasksAreModifiedInTheCorrectRepository() throws Exception {
        this.crontab.forAccount("my-account-1").createWithId("id", Task.builder().spec(TaskSpec.builder().url("account1").build()).build());
        this.crontab.forAccount("my-account-2").createWithId("id", Task.builder().spec(TaskSpec.builder().url("account2").build()).build());

        for (Entity<Task> task : this.crontab.tasks()) {
            this.crontab.update(task, task.value().spec().url().equals("account1") ?
                    task.value().withSpec(TaskSpec.builder().url("changed1").build()) :
                    task.value().withSpec(TaskSpec.builder().url("changed2").build())
            );
        }

        assertThat(this.crontab.forAccount("my-account-1").retrieve("id").value().spec().url(), is("changed1"));
        assertThat(this.crontab.forAccount("my-account-2").retrieve("id").value().spec().url(), is("changed2"));
    }

    @Test
    public void givenTasksAreInDifferentAccountWithSameId__whenDeletingTasksFromCrontab__thenTasksAreRemovedInTheCorrectRepository() throws Exception {
        this.crontab.forAccount("my-account-1").createWithId("id", Task.builder().spec(TaskSpec.builder().url("account1").build()).build());
        this.crontab.forAccount("my-account-2").createWithId("id", Task.builder().spec(TaskSpec.builder().url("account2").build()).build());

        for (Entity<Task> task : this.crontab.tasks()) {
            if(task.value().spec().url().equals("account1") ) {
                this.crontab.delete(task);
            }
        }

        assertThat(this.crontab.forAccount("my-account-1").all(0, 0).total(), is(0L));
        assertThat(this.crontab.forAccount("my-account-2").all(0, 0).total(), is(1L));
    }

    @Test
    public void givenAccountRepositoriesAreNotEmpty__whenLoadingAccountsCrontab__thenAccountsTasksAreLoaded() throws Exception {
        accountRepositries.put("my-account-1", createAccountRepository());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());
        accountRepositries.put("my-account-2", createAccountRepository());
        accountRepositries.get("my-account-2").create(Task.builder().spec(TaskSpec.builder().build()).build());

        this.crontab.loadAccounts("my-account-1", "my-account-2");

        assertThat(this.crontab.tasks(), hasSize(2));
    }

    @Test
    public void givenAccountRepositoryHasManyEntry__whenLoadingAccountsCrontab__thenAccountTasksAreLoaded() throws Exception {
        accountRepositries.put("my-account-1", createAccountRepository());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());

        this.crontab.loadAccounts("my-account-1");

        assertThat(this.crontab.tasks(), hasSize(3));
    }

    @Test
    public void givenAccountRepositoryHasOneEntry__whenLoadingAccountsCrontab_andCreatingATask__thenBothTasksAreLoaded() throws Exception {
        accountRepositries.put("my-account-1", createAccountRepository());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());

        this.crontab.loadAccounts("my-account-1");
        this.crontab.forAccount("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());

        assertThat(this.crontab.tasks(), hasSize(2));
    }

    @Test
    public void givenAccountRepositoriesAreNotEmpty__whenLoadingOneAccountCrontab__thenAccountTasksAreLoaded() throws Exception {
        accountRepositries.put("my-account-1", createAccountRepository());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());
        accountRepositries.put("my-account-2", createAccountRepository());
        accountRepositries.get("my-account-2").create(Task.builder().spec(TaskSpec.builder().build()).build());

        this.crontab.loadAccounts("my-account-1");

        assertThat(this.crontab.tasks(), hasSize(1));
    }

    @Test
    public void givenAccountRepositoriesAreNotEmpty__whenLoadingNotExistingAccountCrontab__thenAccountTasksNotLoaded() throws Exception {
        accountRepositries.put("my-account-1", createAccountRepository());
        accountRepositries.get("my-account-1").create(Task.builder().spec(TaskSpec.builder().build()).build());
        accountRepositries.put("my-account-2", createAccountRepository());
        accountRepositries.get("my-account-2").create(Task.builder().spec(TaskSpec.builder().build()).build());

        this.crontab.loadAccounts("my-account-3");

        assertThat(this.crontab.tasks(), hasSize(0));
    }
}