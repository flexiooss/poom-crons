package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskGetRequest;
import org.codingmatters.poom.crons.crontab.api.TaskGetResponse;
import org.codingmatters.poom.crons.crontab.api.types.Error;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.servives.domain.entities.Entity;

import java.util.function.Function;

public class GetTask implements Function<TaskGetRequest, TaskGetResponse> {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(GetTask.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    public GetTask(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    @Override
    public TaskGetResponse apply(TaskGetRequest request) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(request.account());
        try {
            Entity<Task> task = repository.retrieve(request.taskId());
            if(task != null) {
                return TaskGetResponse.builder()
                        .status200(status -> status
                                .xEntityId(task.id())
                                .payload(task.value())
                        )
                        .build();
            } else {
                return TaskGetResponse.builder()
                        .status404(status -> status.payload(error -> error
                                .code(Error.Code.RESOURCE_NOT_FOUND)
                                .token(log.tokenized().info("request for an unexisting task : {}", request))
                        ))
                        .build();
            }
        } catch (RepositoryException e) {
            return TaskGetResponse.builder()
                    .status500(status -> status.payload(error -> error
                            .code(Error.Code.UNEXPECTED_ERROR)
                            .token(log.tokenized().error("failed accessing repository for request : " + request, e))
                    ))
                    .build();
        }
    }
}
