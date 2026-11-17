package org.ject.support.common.audit;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class AuditDummyService {

    private final AuditDummyEntityRepository repository;
    private final AuditDummyChildEntityRepository childRepository;

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.CREATE, targetId = "#name")
    public Long create(String name) {
        AuditDummyEntity entity = AuditDummyEntity.create(name);
        repository.save(entity);
        return entity.getId();
    }

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.UPDATE, targetId = "#id")
    public void rename(Long id, String newName) {
        AuditDummyEntity entity = repository.findById(id).orElseThrow();
        entity.rename(newName);
    }

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.DELETE, targetId = "#id")
    public void softDelete(Long id) {
        AuditDummyEntity entity = repository.findById(id).orElseThrow();
        entity.delete();
    }

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.RESTORE, targetId = "#id")
    public void restore(Long id) {
        AuditDummyEntity entity = repository.findById(id).orElseThrow();
        entity.restore();
    }

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.UPDATE, targetId = "#id")
    public void renameBoth(Long id, String newName, Long childId, String newLabel) {
        AuditDummyEntity entity = repository.findById(id).orElseThrow();
        entity.rename(newName);

        AuditDummyChildEntity child = childRepository.findById(childId).orElseThrow();
        child.relabel(newLabel);
    }

    @Audit(domain = "AUDIT_DUMMY", action = ChangeActionType.UPDATE, targetId = "#id")
    public void renameThenFail(Long id, String newName) {
        AuditDummyEntity entity = repository.findById(id).orElseThrow();
        entity.rename(newName);
        throw new IllegalStateException("강제 실패");
    }
}
