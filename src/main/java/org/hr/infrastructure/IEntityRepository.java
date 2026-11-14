package org.hr.infrastructure;

import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.ConcurrentUpdateException;
import org.hr.domain.common.EntityNotFoundException;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contractul comun al repository-urilor radacinilor de agregat (Sprint 3: persistenta JPA).
 * <p>
 * Extinde {@link JpaRepository}: implementarea este generata de Spring Data JPA. Operatiile din
 * Sprint 2 (add, find, requireById, ...) sunt pastrate ca metode implicite deasupra JpaRepository,
 * astfel incat serviciile de business raman neschimbate, iar operatiile specifice (findByXxx, @Query)
 * se declara in interfetele concrete.
 */
@NoRepositoryBean
public interface IEntityRepository<T extends BaseEntity> extends JpaRepository<T, Long> {

    //
    /** @return entitatea sau null daca nu exista */
    @Override
    default T getById(Long id) {
        return findById(id).orElse(null);
    }

    /** @throws org.hr.domain.common.EntityNotFoundException daca nu exista */
    default T requireById(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(entityNameOf(this), id));
    }

    default T get(T sample) {
        return getById(sample.getId());
    }

    /** getAll */
    default Collection<T> toCollection() {
        return findAll();
    }

    default List<T> find(Predicate<? super T> filter) {
        return findAll().stream().filter(filter).toList();
    }

    default Optional<T> findFirst(Predicate<? super T> filter) {
        return findAll().stream().filter(filter).findFirst();
    }

    //
    /** Salveaza (INSERT sau UPDATE) si sincronizeaza imediat cu baza de date (RQ-12: versionare optimista). */
    default T add(T entity) {
        try {
            return saveAndFlush(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException(entityNameOf(this), entity.getId(), entity.getVersion());
        }
    }

    default Collection<T> addAll(Collection<T> entities) {
        for (T entity : entities)
            add(entity);
        return entities;
    }

    default boolean remove(T entity) {
        if (entity.getId() == null || !existsById(entity.getId()))
            return false;
        delete(entity);
        flush();
        return true;
    }

    default boolean removeAll(Collection<T> entities) {
        boolean all = true;
        for (T entity : entities)
            if (!remove(entity))
                all = false;
        return all;
    }

    //
    default int size() {
        return (int) count();
    }

    /** Numele entitatii gestionate de repository-ul dat (din parametrul generic al interfetei concrete). */
    private static String entityNameOf(Object repository) {
        for (Class<?> type : repository.getClass().getInterfaces()) {
            Class<?> entity = GenericTypeResolver.resolveTypeArgument(type, IEntityRepository.class);
            if (entity != null)
                return entity.getSimpleName();
        }
        return "Entity";
    }
}
