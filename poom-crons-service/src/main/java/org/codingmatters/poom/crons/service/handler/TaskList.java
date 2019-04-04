package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.AccountCrontabGetRequest;
import org.codingmatters.poom.crons.crontab.api.AccountCrontabGetResponse;
import org.codingmatters.poom.crons.crontab.api.types.Error;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.paging.Rfc7233Pager;

import java.util.function.Function;

public class TaskList implements Function<AccountCrontabGetRequest, AccountCrontabGetResponse> {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(TaskList.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    public TaskList(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    @Override
    public AccountCrontabGetResponse apply(AccountCrontabGetRequest request) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(request.account());
        try {
            Rfc7233Pager.Page<Task> page = Rfc7233Pager.forRequestedRange(request.range()).unit("Task").maxPageSize(1000).pager(repository).page();
            if(page.isValid()) {
                log.audit().info("returning {} task list", page.isPartial() ? "partial" : "complete");
                if(page.isPartial()) {
                    return AccountCrontabGetResponse.builder()
                        .status206(status -> status
                                .acceptRange(page.acceptRange())
                                .contentRange(page.contentRange())
                                .payload(page.list().valueList())
                        )
                        .build();
                } else {
                    return AccountCrontabGetResponse.builder()
                        .status200(status -> status
                                .acceptRange(page.acceptRange())
                                .contentRange(page.contentRange())
                                .payload(page.list().valueList())
                        )
                        .build();
                }
            } else {
                return AccountCrontabGetResponse.builder()
                        .status416(status -> status
                                .acceptRange(page.acceptRange())
                                .contentRange(page.contentRange())
                                .payload(error -> error
                                    .code(Error.Code.ILLEGAL_RANGE_SPEC)
                                    .token(log.tokenized().info("illegal paged collection request {}", request))
                                )
                        )
                        .build();
            }
        } catch (RepositoryException e) {
            return AccountCrontabGetResponse.builder()
                    .status500(status -> status.payload(error -> error
                            .code(Error.Code.UNEXPECTED_ERROR)
                            .token(log.tokenized().error("failed accessing repository for request : " + request, e))
                    ))
                    .build();
        }
    }
}
