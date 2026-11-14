package org.hr.infrastructure;

import jakarta.persistence.EntityManager;
import org.hr.domain.common.BaseEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clasa de baza a tuturor repository-urilor Spring Data JPA ale aplicatiei (repositoryBaseClass).
 * <p>
 * Pastreaza semantica Sprint 2 "add(entity) lucreaza pe aceeasi instanta": Spring Data apeleaza {@code merge} pentru
 * orice entitate cu id, iar {@code merge} pe o entitate deja gestionata (managed) copiaza componentele noi
 * intr-o instanta distincta, lasand originalul fara id. Pentru entitati gestionate se foloseste {@code persist},
 * care propaga (CascadeType.ALL) salvarea entitatilor componenta noi direct pe instantele existente.
 * Entitatile detasate (cu versiune) continua sa treaca prin {@code merge}, deci controlul concurentei optimiste ramane activ.
 */
public class EntityRepositoryJpaBase<T extends BaseEntity> extends SimpleJpaRepository<T, Long> {
    private final EntityManager entityManager;

    public EntityRepositoryJpaBase(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        if (entity.getId() != null && entityManager.contains(entity)) {
            entityManager.persist(entity);      // no-op pentru entitatea in sine; cascade PERSIST pentru componentele noi
            return entity;
        }
        return super.save(entity);
    }
}