package org.codingmatters.poom.services.domain.repositories;

import org.codingmatters.poom.servives.domain.entities.Entity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface RepositoryObserver<V> {
    void entityCreated(Entity<V> entity);
    void entityUpdated(Entity<V> entity);
    void entityDeleted(Entity<V> entity);

    class Cluster<V> implements RepositoryObserver<V> {
        private final List<RepositoryObserver<V>> observers = Collections.synchronizedList(new LinkedList<>());

        public void add(RepositoryObserver<V> observer) {
            this.observers.add(observer);
        }

        @Override
        public void entityCreated(Entity<V> entity) {
            this.observers.forEach(observer -> observer.entityCreated(entity));
        }

        @Override
        public void entityUpdated(Entity<V> entity) {
            this.observers.forEach(observer -> observer.entityUpdated(entity));
        }

        @Override
        public void entityDeleted(Entity<V> entity) {
            this.observers.forEach(observer -> observer.entityDeleted(entity));
        }
    }
}
