package org.codingmatters.poom.crons.service;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.Crontab;
import org.codingmatters.poom.crons.domain.TaskExecutor;
import org.codingmatters.poom.crons.domain.selector.DateTimeTaskSelector;
import org.codingmatters.poom.crons.domain.trigger.TaskTrigger;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.poom.servives.domain.entities.Entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class CrontabService {

    static private final CategorizedLogger log = CategorizedLogger.getLogger(CrontabService.class);

    private final Crontab crontab;
    private final PoomCronsApi api;

    private final TaskTrigger trigger;
    private final ForkJoinPool pool;

    private TaskExecutor executor;

    private ScheduledExecutorService scheduler;
    private Long errorThreshold = 3L;

    public CrontabService(
            Function<String, Repository<Task, Void>> repositoryForAccount,
            String[] initialAccounts,
            TaskTrigger trigger,
            ForkJoinPool pool) throws RepositoryException {
        this.crontab = new Crontab(repositoryForAccount).loadAccounts(initialAccounts);

        this.api = new PoomCronsApi(account -> this.crontab.forAccount(account));
        this.trigger = trigger;
        this.pool = pool;

        this.executor = new TaskExecutor(this.pool, this.trigger);
    }

    public PoomCronsApi api() {
        return this.api;
    }

    public void start() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        int nextMinuteStart = 60 - LocalDateTime.now().getSecond();
        this.scheduler.scheduleAtFixedRate(this::checkedTick, nextMinuteStart, 60, TimeUnit.SECONDS);
        this.scheduler.scheduleAtFixedRate(this::cleanupFailedTasks, nextMinuteStart + 30, 60, TimeUnit.SECONDS);
        log.info("started crontab service");
    }

    private void checkedTick() {
        try {
            log.debug("tick");
            this.tick();
        } catch (RepositoryException | ExecutionException | InterruptedException e) {
            log.error("error ticking", e);
        }
    }

    private void tick() throws RepositoryException, ExecutionException, InterruptedException {
        List<Entity<Task>> selectable = this.crontab.selectable(new DateTimeTaskSelector(UTC.now()), this.pool);
        if(! selectable.isEmpty()) {
            List<Entity<Task>> executed = this.executor.execute(selectable);
            for (Entity<Task> task : executed) {
                this.crontab.update(task, task.value());
            }
        }
    }

    private void cleanupFailedTasks() {
        try {
            for (Entity<Task> task : this.crontab.tasks()) {
                if(task.value().errorCount() >= this.errorThreshold) {
                    log.info("task has reached the error threshold ({}), removing from crontab : {}", this.errorThreshold, task);
                    this.crontab.delete(task);
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        this.scheduler.shutdown();
        try {
            this.scheduler.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("error wating for normal scheduler shutdown", e);
        }
        if(! this.scheduler.isTerminated()) {
            log.warn("scheduler has not shutdown on gentle request, forcing");
            this.scheduler.shutdownNow();
            try {
                this.scheduler.awaitTermination(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("error waiting for scheduler forced shutdown", e);
            }
        }
        if(! this.scheduler.isTerminated()) {
            log.error("GRAVE - unable to stop scheduler");
        } else {
            log.info("stopped crontab service");
        }
    }
}
