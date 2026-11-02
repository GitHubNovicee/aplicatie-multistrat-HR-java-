package org.hr.infrastructure;

import org.hr.domain.common.BaseEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Contract comun al repository-urilor in-memory pentru radacinile de agregat. */
public interface IEntityRepository<T extends BaseEntity> {
    Long getNextID();

    //
    T getById(Long id);
    /** @throws org.hr.domain.common.EntityNotFoundException daca nu exista */
    T requireById(Long id);
    T get(T sample);
    Collection<T> toCollection(); // getAll
    List<T> find(Predicate<? super T> filter);
    Optional<T> findFirst(Predicate<? super T> filter);

    //
    T add(T entity);
    Collection<T> addAll(Collection<T> entities);
    boolean remove(T entity);
    boolean removeAll(Collection<T> entities);

    //
    int size();
}
