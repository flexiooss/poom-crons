package org.codingmatters.poom.crons.service;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.Crontab;
import org.codingmatters.poom.crons.domain.TaskExecutor;
import org.codingmatters.poom.crons.domain.selector.DateTimeTaskSelector;
import org.codingmatters.poom.crons.domain.trigger.TaskTrigger;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.Env;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.poom.services.domain.entities.Entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class CrontabService {

    static private final CategorizedLogger log = CategorizedLogger.getLogger(CrontabService.class);
    private static final String CRON_ERROR_THRESHOLD = "CRON_ERROR_THRESHOLD";
    public static final String CRON_ERROR_THRESHOLD_DEFAULT = "30";
    private final Precision precision;

    public enum Precision {
        SECONDS(TimeUnit.SECONDS) {
            @Override
            public DateTimeTaskSelector selector(LocalDateTime now) {
                return DateTimeTaskSelector.secondsPrecision(now);
            }
        },
        MINUTES(TimeUnit.MINUTES) {
            @Override
            public DateTimeTaskSelector selector(LocalDateTime now) {
                return DateTimeTaskSelector.minutesPrecision(now);
            }
        };

        public final TimeUnit timeUnit;

        Precision(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public abstract DateTimeTaskSelector selector(LocalDateTime now) ;
    }

    private final Crontab crontab;
    private final PoomCronsApi api;

    private final TaskTrigger trigger;
    private final ForkJoinPool pool;

    private TaskExecutor executor;

    private ScheduledExecutorService scheduler;
    private final Long errorThreshold;

    public CrontabService(
            Function<String, Repository<Task, Void>> repositoryForAccount,
            String[] initialAccounts,
            TaskTrigger trigger,
            ForkJoinPool pool) throws RepositoryException {
        this(repositoryForAccount, initialAccounts, trigger, pool, Precision.MINUTES);
    }

    public CrontabService(
            Function<String, Repository<Task, Void>> repositoryForAccount,
            String[] initialAccounts,
            TaskTrigger trigger,
            ForkJoinPool pool,
            Precision precision) throws RepositoryException {
        this.precision = precision;
        this.crontab = new Crontab(repositoryForAccount).loadAccounts(initialAccounts);

        this.api = new PoomCronsApi(account -> this.crontab.forAccount(account));
        this.trigger = trigger;
        this.pool = pool;

        this.executor = new TaskExecutor(this.pool, this.trigger);
        errorThreshold = Env.optional(CRON_ERROR_THRESHOLD).orElse(new Env.Var(CRON_ERROR_THRESHOLD_DEFAULT)).asLong();
    }

    public PoomCronsApi api() {
        return this.api;
    }

    public void start() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        int nextMinuteStart = 60 - LocalDateTime.now().getSecond();


        this.scheduler.scheduleAtFixedRate(this::checkedTick, nextMinuteStart, TimeUnit.SECONDS.convert(1, this.precision.timeUnit), TimeUnit.SECONDS);
        this.scheduler.scheduleAtFixedRate(this::cleanupFailedTasks, nextMinuteStart + 30, TimeUnit.SECONDS.convert(1, this.precision.timeUnit), TimeUnit.SECONDS);
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
        DateTimeTaskSelector selector = this.precision.selector(UTC.now());
        List<Entity<Task>> selectable = this.crontab.selectable(selector, this.pool);
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
            log.error("error cleaning failed tasks", e);
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
