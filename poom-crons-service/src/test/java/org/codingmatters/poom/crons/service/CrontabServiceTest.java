package org.codingmatters.poom.crons.service;

import org.codingmatters.poom.crons.crontab.api.AccountCrontabPostRequest;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.trigger.TaskTrigger;
import org.codingmatters.poom.crons.domain.trigger.TriggerResult;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.poom.services.tests.Eventually;
import org.codingmatters.poom.services.domain.entities.PagedEntityList;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;

public class CrontabServiceTest {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(CrontabServiceTest.class);

    static Eventually eventually = Eventually.timeout(2, TimeUnit.MINUTES);

    private final AtomicLong hits = new AtomicLong(0L);

    private final TaskTrigger successTrigger = (spec, triggedAt, eventId) -> {
        hits.incrementAndGet();
        log.info("success trig");
        return new TriggerResult(true);
    };
    private final TaskTrigger failureTrigger = (spec, triggedAt, eventId) -> {
        log.info("failure trig");
        return new TriggerResult(false);
    };

    private final HashMap<String, Repository<Task, Void>> accountRepositries = new HashMap<>();
    private Function<String, Repository<Task, Void>> repositoryForAccount = account -> {
        accountRepositries.computeIfAbsent(account, s -> createAccountRepository());
        return accountRepositries.get(account);
    };

    private InMemoryRepository<Task, Void> createAccountRepository() {
        return new InMemoryRepository<Task, Void>() {
            @Override
            public PagedEntityList<Task> search(Void query, long startIndex, long endIndex) throws RepositoryException {
                return this.all(startIndex, endIndex);
            }
        };
    }

    @Test
    public void givenATaskInRepo__whenAddingTask_andTicking__thenBothTasksAreExecuted() throws Exception {
        LocalDateTime startingAt = UTC.now().minusHours(1L).withSecond(0)/*.plusSeconds(5L)*/.withNano(0);
        this.repositoryForAccount.apply("my-account").create(Task.builder()
                .spec(spec -> spec
                        .url("my-url")
                        .scheduled(scheduled -> scheduled.every(every -> every
                                .minutes(1L)
                                .startingAt(startingAt)
                        ))
                )
                .build());

        CrontabService service = new CrontabService(this.repositoryForAccount, new String[] {"my-account"}, this.successTrigger, new ForkJoinPool(4), CrontabService.Precision.SECONDS);

        try {
            service.start();

            service.api().handlers().accountCrontabPostHandler().apply(AccountCrontabPostRequest.builder()
                    .account("my-account")
                    .payload(spec -> spec
                            .url("my-url")
                            .scheduled(scheduled -> scheduled.every(every -> every
                                    .minutes(1L)
                                    .startingAt(startingAt)
                            ))
                    )
                    .build())
                    .opt().status201()
                    .orElseThrow(() -> new AssertionError("failed scheduling task"));
            eventually.assertThat(() -> hits.get(), is(2L));
        } finally {
            service.stop();
        }
    }

    @Test
    public void givenATaskWithFailures__whenReachingThreErrorThreshold__thenTaskIsEvicted() throws Exception {
        this.repositoryForAccount.apply("my-account").create(Task.builder()
                .errorCount(Long.parseLong(CrontabService.CRON_ERROR_THRESHOLD_DEFAULT) - 1L)
                .spec(spec -> spec
                        .url("my-url")
                        .scheduled(scheduled -> scheduled.every(every -> every
                                .minutes(1L)
                                .startingAt(UTC.now().minusHours(1L).withSecond(0).withNano(0))
                        ))
                )
                .build());

        CrontabService service = new CrontabService(this.repositoryForAccount, new String[] {"my-account"}, this.failureTrigger, new ForkJoinPool(4));

        try {
            service.start();

            eventually.assertThat(() -> this.repositoryForAccount.apply("my-account").all(0, 0).total(), is(0L));
        } finally {
            service.stop();
        }
    }

}