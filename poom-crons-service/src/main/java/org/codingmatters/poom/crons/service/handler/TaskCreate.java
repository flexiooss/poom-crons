package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.AccountCrontabPostRequest;
import org.codingmatters.poom.crons.crontab.api.AccountCrontabPostResponse;
import org.codingmatters.poom.crons.crontab.api.types.Error;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.TaskSpecValidator;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.domain.entities.Entity;

import java.util.function.Function;

public class TaskCreate implements Function<AccountCrontabPostRequest, AccountCrontabPostResponse> {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(TaskCreate.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    public TaskCreate(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    @Override
    public AccountCrontabPostResponse apply(AccountCrontabPostRequest request) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(request.account());
        TaskSpecValidator.TaskSpecValidation validation = new TaskSpecValidator(request.payload()).validate();

        if(validation.valid()) {
            try {
                Entity<Task> created = repository.create(Task.builder().spec(request.payload()).build());
                log.audit().info("created task for account {} : {}", request.account(), created);
                return AccountCrontabPostResponse.builder()
                        .status201(status -> status
                                .xEntityId(created.id())
                                .payload(created.value())
                        )
                        .build();
            } catch (RepositoryException e) {
                return AccountCrontabPostResponse.builder()
                        .status500(status -> status.payload( error -> error
                                .code(Error.Code.UNEXPECTED_ERROR)
                                .token(log.tokenized().info("request for an unexisting task : {}", request))
                        ))
                        .build();
            }
        } else {
            return AccountCrontabPostResponse.builder()
                    .status400(status -> status.payload(error -> error
                            .code(Error.Code.INVALID_REQUEST)
                            .token(log.tokenized().info("invalid task creation request : {}, request={}", validation.validationMessage(), request))
                    ))
                    .build();
        }
    }
}
