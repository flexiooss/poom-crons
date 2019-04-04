package org.codingmatters.poom.crons.domain;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.ObservableRepository;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.domain.repositories.RepositoryObserver;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.MutableEntity;
import org.codingmatters.poom.servives.domain.entities.PagedEntityList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Crontab {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(Crontab.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    private final Repository<Task, Void> cache = new InMemoryRepository<Task, Void>() {
        @Override
        public PagedEntityList<Task> search(Void query, long startIndex, long endIndex) throws RepositoryException {
            return this.all(startIndex, endIndex);
        }
    };

    public Crontab(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    public Repository<Task, Void> forAccount(String account) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(account);
        return new ObservableRepository<>(repository).addObserver(new AccountObserver(this, account));
    }

    public synchronized List<Entity<Task>> tasks() throws RepositoryException {
        List<Entity<Task>> result = new LinkedList<>();

        long start = 0;
        PagedEntityList<Task> entities;
        do {
            long end = start + 1000 - 1;
            entities = this.cache.all(start, end);
            result.addAll(entities);
            start = end + 1;
        } while(entities.size() == 1000);

        return result;
    }

    public synchronized List<Entity<Task>> selectable(TaskSelector selector, ForkJoinPool pool) throws RepositoryException, ExecutionException, InterruptedException {
        List<Entity<Task>> tasks = this.tasks();
        List<Entity<Task>> result = pool.submit(() -> tasks.parallelStream()
                .filter(taskEntity -> selector.selectable(taskEntity.value().spec()))
                .collect(Collectors.toList())
        ).get();
        return result;
    }

    static class AccountObserver implements RepositoryObserver<Task> {

        private final Crontab crontab;
        private final String account;

        AccountObserver(Crontab crontab, String account) {
            this.crontab = crontab;
            this.account = account;
        }

        @Override
        public void entityCreated(Entity<Task> entity) {
            this.crontab.created(this.account, entity);
        }

        @Override
        public void entityUpdated(Entity<Task> entity) {
            this.crontab.updated(this.account, entity);
        }

        @Override
        public void entityDeleted(Entity<Task> entity) {
            this.crontab.deleted(this.account, entity);
        }
    }

    private synchronized void created(String account, Entity<Task> entity) {
        try {
            this.cache.createWithId("account/" + entity.id(), entity.value());
        } catch (RepositoryException e) {
            this.error(e);
        }
    }

    private synchronized void updated(String account, Entity<Task> entity) {
        try {
            this.cache.update(new MutableEntity<>("account/" + entity.id(), entity.value()), entity.value());
        } catch (RepositoryException e) {
            this.error(e);
        }
    }

    private synchronized void deleted(String account, Entity<Task> entity) {
        try {
            this.cache.delete(new MutableEntity<>("account/" + entity.id(), entity.value()));
        } catch (RepositoryException e) {
            this.error(e);
        }
    }

    private void error(RepositoryException e) {
        String message = "failed syncing crontab in memory cache, crontab inconsistency risk, throwong runtime exception";
        log.error("GRAVE -- " + message);
        throw new RuntimeException(message, e);
    }
}
