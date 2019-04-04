package org.codingmatters.poom.services.domain.repositories;

import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.PagedEntityList;

import java.math.BigInteger;

public class ObservableRepository <V, Q> implements Repository<V, Q> {
    private final Repository<V, Q> delegate;
    private final RepositoryObserver.Cluster<V> observer = new RepositoryObserver.Cluster<V>();

    public ObservableRepository(Repository<V, Q> delegate) {
        this.delegate = delegate;
    }

    public ObservableRepository addObserver(RepositoryObserver<V> observer) {
        this.observer.add(observer);
        return this;
    }

    @Override
    public Entity<V> create(V withValue) throws RepositoryException {
        Entity<V> entity = delegate.create(withValue);
        this.observer.entityCreated(entity);
        return entity;
    }

    @Override
    public Entity<V> retrieve(String id) throws RepositoryException {
        return delegate.retrieve(id);
    }

    @Override
    public Entity<V> update(Entity<V> entity, V withValue) throws RepositoryException {
        Entity<V> update = delegate.update(entity, withValue);
        this.observer.entityUpdated(update);
        return update;
    }

    @Override
    public void delete(Entity<V> entity) throws RepositoryException {
        delegate.delete(entity);
        this.observer.entityDeleted(entity);
    }

    @Override
    public Entity<V> createWithId(String id, V withValue) throws RepositoryException {
        Entity<V> entity = delegate.createWithId(id, withValue);
        this.observer.entityCreated(entity);
        return entity;
    }

    @Override
    public Entity<V> createWithIdAndVersion(String id, BigInteger version, V withValue) throws RepositoryException {
        Entity<V> entity = delegate.createWithIdAndVersion(id, version, withValue);
        this.observer.entityCreated(entity);
        return entity;
    }

    @Override
    public PagedEntityList<V> all(long startIndex, long endIndex) throws RepositoryException {
        return delegate.all(startIndex, endIndex);
    }

    @Override
    public PagedEntityList<V> search(Q query, long startIndex, long endIndex) throws RepositoryException {
        return delegate.search(query, startIndex, endIndex);
    }
}
