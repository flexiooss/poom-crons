package org.codingmatters.poom.services.domain.repositories;

import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.PagedEntityList;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObservableRepositoryTest {

    private Repository<String, Void> repository = new InMemoryRepository<String, Void>() {
        @Override
        public PagedEntityList<String> search(Void query, long startIndex, long endIndex) throws RepositoryException {
            return null;
        }
    };

    private ObservableRepository<String, Void> observable = new ObservableRepository<>(this.repository);

    private List<String> created = Collections.synchronizedList(new LinkedList<>());
    private List<String> updated = Collections.synchronizedList(new LinkedList<>());
    private List<String> deleted = Collections.synchronizedList(new LinkedList<>());

    private RepositoryObserver<String> observer = new RepositoryObserver<String>() {
        @Override
        public void entityCreated(Entity<String> entity) {
            created.add(entity.id());
        }

        @Override
        public void entityUpdated(Entity<String> entity) {
            updated.add(entity.id());
        }

        @Override
        public void entityDeleted(Entity<String> entity) {
            deleted.add(entity.id());
        }
    };

    @Test
    public void whenCreatingFromObserver__thenCreatedInDelegate() throws Exception {
        Entity<String> entity = this.observable.create("created");

        assertThat(this.repository.retrieve(entity.id()).value(), is(entity.value()));
    }

    @Test
    public void whenCreatingFromObserverWithId__thenCreatedInDelegate() throws Exception {
        Entity<String> entity = this.observable.createWithId("created", "created");

        assertThat(this.repository.retrieve("created").value(), is(entity.value()));
    }

    @Test
    public void givenEntityExistsInDelegate__whenUpdatingFromObserver__thenUpdatedInDelegate() throws Exception {
        Entity<String> entity = this.repository.create("created");

        entity = this.observable.update(entity, "changed");
        assertThat(this.repository.retrieve(entity.id()).value(), is("changed"));
    }

    @Test
    public void givenEntityExistsInDelegate__whenDeletingFromObserver__thenDeletedInDelegate() throws Exception {
        Entity<String> entity = this.repository.create("created");

        this.observable.delete(entity);
        assertThat(this.repository.retrieve(entity.id()), is(nullValue()));
    }


    @Test
    public void givenObserved__whenCreatingFromObserver__thenObserverInCalledOnCreated() throws Exception {
        this.observable.addObserver(this.observer);

        Entity<String> entity = this.observable.create("created");

        assertThat(this.created, contains(entity.id()));
    }

    @Test
    public void givenObserved__whenCreatingFromObserverWithId__thenObserverInCalledOnCreated() throws Exception {
        this.observable.addObserver(this.observer);

        Entity<String> entity = this.observable.createWithId("created", "created");

        assertThat(this.created, contains("created"));
    }

    @Test
    public void givenObserved_andEntityExistsInDelegate__whenUpdatingFromObserver__thenObserverInCalledOnUpdated() throws Exception {
        this.observable.addObserver(this.observer);

        Entity<String> entity = this.repository.create("created");
        entity = this.observable.update(entity, "changed");

        assertThat(this.updated, contains(entity.id()));
    }

    @Test
    public void givenObserved_andEntityExistsInDelegate__whenDeletingFromObserver__thenObserverInCalledOnDeleted() throws Exception {
        this.observable.addObserver(this.observer);

        Entity<String> entity = this.repository.create("created");

        this.observable.delete(entity);

        assertThat(this.deleted, contains(entity.id()));
    }

}