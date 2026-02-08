package de.innologic.iamservice.catalog.service;

import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.module.repo.IamModuleRepository;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.tenant.entity.IamTenantModuleEntity;
import de.innologic.iamservice.tenant.repo.IamTenantModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final IamModuleRepository moduleRepo;
    private final IamPermissionRepository permRepo;
    private final IamTenantModuleRepository tenantModuleRepo;

    public CatalogService(IamModuleRepository moduleRepo,
                          IamPermissionRepository permRepo,
                          IamTenantModuleRepository tenantModuleRepo) {
        this.moduleRepo = moduleRepo;
        this.permRepo = permRepo;
        this.tenantModuleRepo = tenantModuleRepo;
    }

    @Transactional
    public IamModuleEntity createModule(String moduleKey, String name, String description) {
        moduleRepo.findByModuleKey(moduleKey).ifPresent(m -> {
            throw new IllegalArgumentException("moduleKey already exists: " + moduleKey);
        });

        IamModuleEntity m = new IamModuleEntity();
        m.setModuleKey(moduleKey);
        m.setName(name);
        m.setDescription(description);
        m.setActive(true);
        return moduleRepo.save(m);
    }

    public List<IamModuleEntity> listModules() {
        return moduleRepo.findAll();
    }

    @Transactional
    public IamPermissionEntity createPermission(String moduleKey, String code, String description) {
        IamModuleEntity module = moduleRepo.findByModuleKey(moduleKey)
                .orElseThrow(() -> new EntityNotFoundException("module not found: " + moduleKey));

        IamPermissionEntity p = new IamPermissionEntity();
        p.setModule(module);
        p.setCode(code);
        p.setDescription(description);
        p.setActive(true);
        return permRepo.save(p);
    }

    public List<IamPermissionEntity> listPermissions(String moduleKey) {
        return permRepo.findByModule_ModuleKey(moduleKey);
    }

    @Transactional
    public IamTenantModuleEntity setTenantModuleEnabled(String tenantId, String moduleKey, boolean enabled) {
        IamModuleEntity module = moduleRepo.findByModuleKey(moduleKey)
                .orElseThrow(() -> new EntityNotFoundException("module not found: " + moduleKey));

        IamTenantModuleEntity tm = tenantModuleRepo.findByTenantIdAndModule_ModuleKey(tenantId, moduleKey)
                .orElseGet(() -> {
                    IamTenantModuleEntity x = new IamTenantModuleEntity();
                    x.setTenantId(tenantId);
                    x.setModule(module);
                    return x;
                });

        tm.setEnabled(enabled);
        return tenantModuleRepo.save(tm);
    }

    public boolean isTenantModuleEnabled(String tenantId, String moduleKey) {
        return tenantModuleRepo.findByTenantIdAndModule_ModuleKey(tenantId, moduleKey)
                .map(IamTenantModuleEntity::isEnabled)
                .orElse(false);
    }
}
