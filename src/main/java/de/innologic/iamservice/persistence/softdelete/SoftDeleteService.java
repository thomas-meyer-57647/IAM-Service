package de.innologic.iamservice.persistence.softdelete;

import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteService {

    private final AuditorAware<String> auditorAware;

    public SoftDeleteService(AuditorAware<String> auditorAware) {
        this.auditorAware = auditorAware;
    }

    @Transactional
    public <T extends BaseAuditableEntity, ID> void softDeleteById(JpaRepository<T, ID> repo, ID id) {
        T entity = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));
        softDelete(repo, entity);
    }

    @Transactional
    public <T extends BaseAuditableEntity, ID> void softDelete(JpaRepository<T, ID> repo, T entity) {
        if (entity.isDeleted()) return;

        String by = auditorAware.getCurrentAuditor().orElse("system");
        entity.markDeleted(by);

        // save() triggert @LastModifiedDate/@LastModifiedBy automatisch
        repo.save(entity);
    }
}
