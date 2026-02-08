-- V1__init.sql (MySQL 8+)
-- Charset/Collation werden idealerweise serverseitig über docker-compose gesetzt (utf8mb4)

CREATE TABLE iam_module (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            module_key VARCHAR(64) NOT NULL,
                            name VARCHAR(128) NULL,
                            description VARCHAR(512) NULL,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                            created_by VARCHAR(128) NULL,
                            modified_at DATETIME(3) NULL,
                            modified_by VARCHAR(128) NULL,
                            deleted_at DATETIME(3) NULL,
                            deleted_by VARCHAR(128) NULL,

                            PRIMARY KEY (id),
                            UNIQUE KEY uq_module_key (module_key)
) ENGINE=InnoDB;

CREATE TABLE iam_permission (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                module_id BIGINT NOT NULL,
                                code VARCHAR(128) NOT NULL,         -- z.B. "timeentry.read"
                                description VARCHAR(512) NULL,
                                is_active BOOLEAN NOT NULL DEFAULT TRUE,

                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                created_by VARCHAR(128) NULL,
                                modified_at DATETIME(3) NULL,
                                modified_by VARCHAR(128) NULL,
                                deleted_at DATETIME(3) NULL,
                                deleted_by VARCHAR(128) NULL,

                                PRIMARY KEY (id),
                                UNIQUE KEY uq_permission_code (code),
                                KEY ix_permission_module_id (module_id),
                                CONSTRAINT fk_permission_module
                                    FOREIGN KEY (module_id) REFERENCES iam_module(id)
) ENGINE=InnoDB;

CREATE TABLE iam_tenant_module (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   tenant_id VARCHAR(64) NOT NULL,
                                   module_id BIGINT NOT NULL,
                                   enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                   created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                   created_by VARCHAR(128) NULL,
                                   modified_at DATETIME(3) NULL,
                                   modified_by VARCHAR(128) NULL,
                                   deleted_at DATETIME(3) NULL,
                                   deleted_by VARCHAR(128) NULL,

                                   PRIMARY KEY (id),
                                   UNIQUE KEY uq_tenant_module (tenant_id, module_id),
                                   KEY ix_tenant_module_tenant (tenant_id),
                                   CONSTRAINT fk_tenant_module_module
                                       FOREIGN KEY (module_id) REFERENCES iam_module(id)
) ENGINE=InnoDB;

CREATE TABLE iam_role (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          tenant_id VARCHAR(64) NOT NULL,
                          name VARCHAR(128) NOT NULL,
                          description VARCHAR(512) NULL,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                          created_by VARCHAR(128) NULL,
                          modified_at DATETIME(3) NULL,
                          modified_by VARCHAR(128) NULL,
                          deleted_at DATETIME(3) NULL,
                          deleted_by VARCHAR(128) NULL,

                          PRIMARY KEY (id),
                          UNIQUE KEY uq_role_name_per_tenant (tenant_id, name),
                          KEY ix_role_tenant (tenant_id)
) ENGINE=InnoDB;

CREATE TABLE iam_role_permission (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     role_id BIGINT NOT NULL,
                                     permission_id BIGINT NOT NULL,

                                     created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                     created_by VARCHAR(128) NULL,
                                     modified_at DATETIME(3) NULL,
                                     modified_by VARCHAR(128) NULL,
                                     deleted_at DATETIME(3) NULL,
                                     deleted_by VARCHAR(128) NULL,

                                     PRIMARY KEY (id),
                                     UNIQUE KEY uq_role_permission (role_id, permission_id),
                                     KEY ix_role_permission_role (role_id),
                                     KEY ix_role_permission_perm (permission_id),
                                     CONSTRAINT fk_role_permission_role
                                         FOREIGN KEY (role_id) REFERENCES iam_role(id),
                                     CONSTRAINT fk_role_permission_permission
                                         FOREIGN KEY (permission_id) REFERENCES iam_permission(id)
) ENGINE=InnoDB;

CREATE TABLE iam_subject (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             subject_id VARCHAR(128) NOT NULL,       -- externe ID (UserId/ServiceId)
                             subject_type VARCHAR(16) NOT NULL,      -- USER|SERVICE
                             is_active BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                             created_by VARCHAR(128) NULL,
                             modified_at DATETIME(3) NULL,
                             modified_by VARCHAR(128) NULL,
                             deleted_at DATETIME(3) NULL,
                             deleted_by VARCHAR(128) NULL,

                             PRIMARY KEY (id),
                             UNIQUE KEY uq_subject (subject_id, subject_type)
) ENGINE=InnoDB;

CREATE TABLE iam_assignment (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                tenant_id VARCHAR(64) NOT NULL,
                                subject_pk BIGINT NOT NULL,
                                role_id BIGINT NOT NULL,

                                scope_type VARCHAR(32) NULL,            -- optional
                                scope_id VARCHAR(128) NULL,             -- optional

                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                created_by VARCHAR(128) NULL,
                                modified_at DATETIME(3) NULL,
                                modified_by VARCHAR(128) NULL,
                                deleted_at DATETIME(3) NULL,
                                deleted_by VARCHAR(128) NULL,

                                PRIMARY KEY (id),
                                UNIQUE KEY uq_assignment (tenant_id, subject_pk, role_id, scope_type, scope_id),
                                KEY ix_assignment_tenant (tenant_id),
                                KEY ix_assignment_subject (subject_pk),
                                KEY ix_assignment_role (role_id),

                                CONSTRAINT fk_assignment_subject
                                    FOREIGN KEY (subject_pk) REFERENCES iam_subject(id),
                                CONSTRAINT fk_assignment_role
                                    FOREIGN KEY (role_id) REFERENCES iam_role(id)
) ENGINE=InnoDB;

CREATE TABLE iam_tenant_admin (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  tenant_id VARCHAR(64) NOT NULL,
                                  subject_pk BIGINT NOT NULL,

                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  created_by VARCHAR(128) NULL,
                                  modified_at DATETIME(3) NULL,
                                  modified_by VARCHAR(128) NULL,
                                  deleted_at DATETIME(3) NULL,
                                  deleted_by VARCHAR(128) NULL,

                                  PRIMARY KEY (id),
                                  UNIQUE KEY uq_tenant_admin (tenant_id, subject_pk),
                                  KEY ix_tenant_admin_tenant (tenant_id),
                                  CONSTRAINT fk_tenant_admin_subject
                                      FOREIGN KEY (subject_pk) REFERENCES iam_subject(id)
) ENGINE=InnoDB;

CREATE TABLE iam_system_admin (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  subject_pk BIGINT NOT NULL,

                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  created_by VARCHAR(128) NULL,
                                  modified_at DATETIME(3) NULL,
                                  modified_by VARCHAR(128) NULL,
                                  deleted_at DATETIME(3) NULL,
                                  deleted_by VARCHAR(128) NULL,

                                  PRIMARY KEY (id),
                                  UNIQUE KEY uq_system_admin_subject (subject_pk),
                                  CONSTRAINT fk_system_admin_subject
                                      FOREIGN KEY (subject_pk) REFERENCES iam_subject(id)
) ENGINE=InnoDB;

CREATE TABLE iam_perm_version (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  tenant_id VARCHAR(64) NOT NULL,
                                  subject_pk BIGINT NOT NULL,
                                  version BIGINT NOT NULL DEFAULT 1,

                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  created_by VARCHAR(128) NULL,
                                  modified_at DATETIME(3) NULL,
                                  modified_by VARCHAR(128) NULL,
                                  deleted_at DATETIME(3) NULL,
                                  deleted_by VARCHAR(128) NULL,

                                  PRIMARY KEY (id),
                                  UNIQUE KEY uq_perm_version (tenant_id, subject_pk),
                                  KEY ix_perm_version_tenant (tenant_id),
                                  CONSTRAINT fk_perm_version_subject
                                      FOREIGN KEY (subject_pk) REFERENCES iam_subject(id)
) ENGINE=InnoDB;
