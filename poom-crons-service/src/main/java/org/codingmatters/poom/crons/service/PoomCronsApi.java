package org.codingmatters.poom.crons.service;

import org.codingmatters.poom.crons.crontab.api.PoomCronsHandlers;
import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.crons.service.handler.*;
import org.codingmatters.poom.services.domain.repositories.Repository;

import java.util.function.Function;

public class PoomCronsApi {

    private final PoomCronsHandlers handlers;

    public PoomCronsApi(Function<String, Repository<Task, Void>> repositoryForAccount) {
        handlers = new PoomCronsHandlers.Builder()
                .taskGetHandler(new TaskGet(repositoryForAccount))
                .taskPutHandler(new TaskUpdate(repositoryForAccount))
                .taskDeleteHandler(new TaskDelete(repositoryForAccount))
                .accountCrontabGetHandler(new TaskList(repositoryForAccount))
                .accountCrontabPostHandler(new TaskCreate(repositoryForAccount))
                .build();
    }

    public PoomCronsHandlers handlers() {
        return this.handlers;
    }
}
