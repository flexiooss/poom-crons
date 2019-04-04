package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskDeleteRequest;
import org.codingmatters.poom.crons.crontab.api.TaskDeleteResponse;
import org.codingmatters.poom.crons.crontab.api.taskdeleteresponse.Status204;
import org.codingmatters.poom.crons.crontab.api.types.Error;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.servives.domain.entities.Entity;

import java.util.function.Function;

public class TaskDelete implements Function<TaskDeleteRequest, TaskDeleteResponse> {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(TaskDelete.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    public TaskDelete(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    @Override
    public TaskDeleteResponse apply(TaskDeleteRequest request) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(request.account());
        try {
            Entity<Task> task = repository.retrieve(request.taskId());
            if(task != null) {
                repository.delete(task);
                log.info("deleted task {}", task);
                return TaskDeleteResponse.builder()
                        .status204(Status204.builder().build())
                        .build();
            } else {
                return TaskDeleteResponse.builder()
                        .status404(status -> status.payload(error -> error
                                .code(Error.Code.RESOURCE_NOT_FOUND)
                                .token(log.tokenized().info("delete request for a not existing task : {}", request))
                                .description("no such task " + request.taskId())
                        ))
                        .build();
            }
        } catch (RepositoryException e) {
            return TaskDeleteResponse.builder()
                    .status500(status -> status.payload(error -> error
                            .code(Error.Code.UNEXPECTED_ERROR)
                            .token(log.tokenized().info("request for an unexisting task : {}", request))
                    ))
                    .build();
        }
    }
}
