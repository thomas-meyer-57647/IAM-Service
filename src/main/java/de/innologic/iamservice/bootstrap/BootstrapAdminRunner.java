package de.innologic.iamservice.bootstrap;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.domain.SubjectType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class BootstrapAdminRunner {

    @Bean
    ApplicationRunner bootstrapAdmins(AdminService adminService,
                                      @Value("${iam.bootstrap.enabled:false}") boolean enabled,
                                      @Value("${iam.bootstrap.system-admin.subject-id:}") String sysSubjectId,
                                      @Value("${iam.bootstrap.system-admin.subject-type:USER}") String sysSubjectType,
                                      @Value("${iam.bootstrap.tenant-admin.tenant-id:}") String tenantId,
                                      @Value("${iam.bootstrap.tenant-admin.subject-id:}") String tenantSubjectId,
                                      @Value("${iam.bootstrap.tenant-admin.subject-type:USER}") String tenantSubjectType) {

        return args -> {
            if (!enabled) return;

            // System Admin (optional)
            if (sysSubjectId != null && !sysSubjectId.isBlank()) {
                try {
                    adminService.addSystemAdmin(sysSubjectId, SubjectType.valueOf(sysSubjectType));
                } catch (Exception ignore) {
                    // idempotent: schon vorhanden → ok
                }
            }

            // Tenant Admin (optional)
            if (tenantId != null && !tenantId.isBlank() && tenantSubjectId != null && !tenantSubjectId.isBlank()) {
                try {
                    adminService.addTenantAdmin(tenantId, tenantSubjectId, SubjectType.valueOf(tenantSubjectType));
                } catch (Exception ignore) {
                    // idempotent: schon vorhanden → ok
                }
            }
        };
    }
}
