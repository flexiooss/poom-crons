package org.codingmatters.poom.crons.service.handler;

import org.codingmatters.poom.crons.crontab.api.types.Task;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.codingmatters.poom.servives.domain.entities.PagedEntityList;

public class TestTaskRepository extends InMemoryRepository<Task, Void> {
    @Override
    public PagedEntityList<Task> search(Void query, long startIndex, long endIndex) throws RepositoryException {
        return this.all(startIndex, endIndex);
    }
}
