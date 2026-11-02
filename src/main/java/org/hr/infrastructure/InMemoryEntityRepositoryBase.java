package org.hr.infrastructure;

import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.ConcurrentUpdateException;
import org.hr.domain.common.EntityNotFoundException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Repository in-memory generic. Gestioneaza:
 * - id-ul (secventa),
 * - CR-11: createdAt se seteaza o singura data, updatedAt la fiecare salvare,
 * - RQ-12: versionare optimista (o entitate cu versiune invechita este respinsa).
 */
public abstract class InMemoryEntityRepositoryBase<T extends BaseEntity> implements IEntityRepository<T> {
    private final String entityName;
    private final Map<Long, T> entities = new LinkedHashMap<>();
    private final Map<Long, Long> storedVersions = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);

    protected InMemoryEntityRepositoryBase(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public Long getNextID() {
        return nextId.incrementAndGet();
    }

    @Override
    public synchronized T getById(Long id) {
        return entities.get(id);
    }

    @Override
    public synchronized T requireById(Long id) {
        T entity = entities.get(id);
        if (entity == null)
            throw new EntityNotFoundException(entityName, id);
        return entity;
    }

    @Override
    public T get(T sample) {
        return getById(sample.getId());
    }

    @Override
    public synchronized Collection<T> toCollection() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public synchronized List<T> find(Predicate<? super T> filter) {
        List<T> result = new ArrayList<>();
        for (T entity : entities.values())
            if (filter.test(entity))
                result.add(entity);
        return result;
    }

    @Override
    public Optional<T> findFirst(Predicate<? super T> filter) {
        return find(filter).stream().findFirst();
    }

    @Override
    public synchronized T add(T entity) {
        Instant now = Instant.now();
        if (entity.getId() == null)
            entity.setId(getNextID());
        else
            nextId.updateAndGet(current -> Math.max(current, entity.getId()));

        if (entity.getCreatedAt() == null) {            // entitate noua
            entity.setVersion(0L);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
        } else {                                        // modificare
            Long stored = storedVersions.get(entity.getId());
            if (stored != null && !stored.equals(entity.getVersion()))
                throw new ConcurrentUpdateException(entityName, entity.getId(), entity.getVersion(), stored);
            entity.setVersion(entity.getVersion() + 1);
            entity.setUpdatedAt(now);
        }
        storedVersions.put(entity.getId(), entity.getVersion());
        entities.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Collection<T> addAll(Collection<T> toAdd) {
        for (T entity : toAdd)
            this.add(entity);
        return toAdd;
    }

    @Override
    public synchronized boolean remove(T entity) {
        storedVersions.remove(entity.getId());
        return entities.remove(entity.getId()) != null;
    }

    @Override
    public boolean removeAll(Collection<T> toRemove) {
        boolean all = true;
        for (T entity : toRemove)
            if (!this.remove(entity))
                all = false;
        return all;
    }

    @Override
    public synchronized int size() {
        return entities.size();
    }
}
