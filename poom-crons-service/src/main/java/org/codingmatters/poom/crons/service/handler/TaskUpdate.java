package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.TaskPutRequest;
import org.codingmatters.poom.crons.crontab.api.TaskPutResponse;
import org.codingmatters.poom.crons.crontab.api.types.Error;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.domain.TaskSpecValidator;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.servives.domain.entities.Entity;

import java.util.function.Function;

public class TaskUpdate implements Function<TaskPutRequest, TaskPutResponse> {
    static private final CategorizedLogger log = CategorizedLogger.getLogger(TaskUpdate.class);

    private final Function<String, Repository<Task, Void>> repositoryForAccount;

    public TaskUpdate(Function<String, Repository<Task, Void>> repositoryForAccount) {
        this.repositoryForAccount = repositoryForAccount;
    }

    @Override
    public TaskPutResponse apply(TaskPutRequest request) {
        Repository<Task, Void> repository = this.repositoryForAccount.apply(request.account());


        try {
            Entity<Task> task = repository.retrieve(request.taskId());
            if(task != null) {
                Entity<Task> updated = repository.update(task, task.value().withSpec(request.payload()));

                TaskSpecValidator.TaskSpecValidation validation = new TaskSpecValidator(request.payload()).validate();
                if(! validation.valid()) {
                    return TaskPutResponse.builder()
                            .status400(status -> status.payload(error -> error
                                    .code(Error.Code.INVALID_REQUEST)
                                    .token(log.tokenized().info("invalid task change : {} ; reques={}", validation.validationMessage(), request))
                                    .description(validation.validationMessage())
                            ))
                            .build();
                }

                log.audit().info("updated task {}", updated);
                return TaskPutResponse.builder()
                        .status200(status -> status
                                .xEntityId(updated.id())
                                .payload(updated.value())
                        )
                        .build();
            } else {
                return TaskPutResponse.builder()
                    .status404(status -> status.payload(error -> error
                            .code(Error.Code.UNEXPECTED_ERROR)
                            .token(log.tokenized().info("tryied updating unexisting task : {}", request))
                    ))
                    .build();
            }
        } catch (RepositoryException e) {
            return TaskPutResponse.builder()
                    .status500(status -> status.payload(error -> error
                            .code(Error.Code.UNEXPECTED_ERROR)
                            .token(log.tokenized().error("failed accessing repository for request : " + request, e))
                    ))
                    .build();
        }
    }
}
