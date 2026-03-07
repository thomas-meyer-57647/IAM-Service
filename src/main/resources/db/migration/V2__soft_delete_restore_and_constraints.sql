-- V2__soft_delete_restore_and_constraints.sql
-- Portable Variante für MariaDB + H2:
-- keine generated columns im ALTER TABLE,
-- kein DROP INDEX/DROP CONSTRAINT-Konflikt auf bestehenden UNIQUE-Constraints,
-- stattdessen Tabellen-Rebuild mit transformierter Datenübernahme.

-- ------------------------------------------------------------
-- iam_role_permission
-- ------------------------------------------------------------

CREATE TABLE iam_role_permission_new (
                                         id BIGINT NOT NULL AUTO_INCREMENT,
                                         role_id BIGINT NOT NULL,
                                         permission_id BIGINT NOT NULL,
                                         active_key INTEGER NULL DEFAULT 1,

                                         created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                         created_by VARCHAR(128) NULL,
                                         modified_at DATETIME(3) NULL,
                                         modified_by VARCHAR(128) NULL,
                                         deleted_at DATETIME(3) NULL,
                                         deleted_by VARCHAR(128) NULL,

                                         PRIMARY KEY (id),
                                         UNIQUE KEY uq_role_permission_active_new (role_id, permission_id, active_key),
                                         KEY ix_role_permission_role_new (role_id),
                                         KEY ix_role_permission_perm_new (permission_id),
                                         CONSTRAINT fk_role_permission_role_new
                                             FOREIGN KEY (role_id) REFERENCES iam_role(id),
                                         CONSTRAINT fk_role_permission_permission_new
                                             FOREIGN KEY (permission_id) REFERENCES iam_permission(id)
);

INSERT INTO iam_role_permission_new (
    id, role_id, permission_id, active_key,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
)
SELECT
    id,
    role_id,
    permission_id,
    CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
FROM iam_role_permission;

DROP TABLE iam_role_permission;
ALTER TABLE iam_role_permission_new RENAME TO iam_role_permission;

-- ------------------------------------------------------------
-- iam_tenant_admin
-- ------------------------------------------------------------

CREATE TABLE iam_tenant_admin_new (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      tenant_id VARCHAR(64) NOT NULL,
                                      subject_pk BIGINT NOT NULL,
                                      active_key INTEGER NULL DEFAULT 1,

                                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                      created_by VARCHAR(128) NULL,
                                      modified_at DATETIME(3) NULL,
                                      modified_by VARCHAR(128) NULL,
                                      deleted_at DATETIME(3) NULL,
                                      deleted_by VARCHAR(128) NULL,

                                      PRIMARY KEY (id),
                                      UNIQUE KEY uq_tenant_admin_active_new (tenant_id, subject_pk, active_key),
                                      KEY ix_tenant_admin_tenant_new (tenant_id),
                                      CONSTRAINT fk_tenant_admin_subject_new
                                          FOREIGN KEY (subject_pk) REFERENCES iam_subject(id)
);

INSERT INTO iam_tenant_admin_new (
    id, tenant_id, subject_pk, active_key,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
)
SELECT
    id,
    tenant_id,
    subject_pk,
    CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
FROM iam_tenant_admin;

DROP TABLE iam_tenant_admin;
ALTER TABLE iam_tenant_admin_new RENAME TO iam_tenant_admin;

-- ------------------------------------------------------------
-- iam_system_admin
-- ------------------------------------------------------------

CREATE TABLE iam_system_admin_new (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      subject_pk BIGINT NOT NULL,
                                      active_key INTEGER NULL DEFAULT 1,

                                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                      created_by VARCHAR(128) NULL,
                                      modified_at DATETIME(3) NULL,
                                      modified_by VARCHAR(128) NULL,
                                      deleted_at DATETIME(3) NULL,
                                      deleted_by VARCHAR(128) NULL,

                                      PRIMARY KEY (id),
                                      UNIQUE KEY uq_system_admin_subject_active_new (subject_pk, active_key),
                                      CONSTRAINT fk_system_admin_subject_new
                                          FOREIGN KEY (subject_pk) REFERENCES iam_subject(id)
);

INSERT INTO iam_system_admin_new (
    id, subject_pk, active_key,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
)
SELECT
    id,
    subject_pk,
    CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
FROM iam_system_admin;

DROP TABLE iam_system_admin;
ALTER TABLE iam_system_admin_new RENAME TO iam_system_admin;

-- ------------------------------------------------------------
-- iam_tenant_module
-- ------------------------------------------------------------

CREATE TABLE iam_tenant_module_new (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       tenant_id VARCHAR(64) NOT NULL,
                                       module_id BIGINT NOT NULL,
                                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                       active_key INTEGER NULL DEFAULT 1,

                                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                       created_by VARCHAR(128) NULL,
                                       modified_at DATETIME(3) NULL,
                                       modified_by VARCHAR(128) NULL,
                                       deleted_at DATETIME(3) NULL,
                                       deleted_by VARCHAR(128) NULL,

                                       PRIMARY KEY (id),
                                       UNIQUE KEY uq_tenant_module_active_new (tenant_id, module_id, active_key),
                                       KEY ix_tenant_module_tenant_new (tenant_id),
                                       CONSTRAINT fk_tenant_module_module_new
                                           FOREIGN KEY (module_id) REFERENCES iam_module(id)
);

INSERT INTO iam_tenant_module_new (
    id, tenant_id, module_id, enabled, active_key,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
)
SELECT
    id,
    tenant_id,
    module_id,
    enabled,
    CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
FROM iam_tenant_module;

DROP TABLE iam_tenant_module;
ALTER TABLE iam_tenant_module_new RENAME TO iam_tenant_module;

-- ------------------------------------------------------------
-- iam_assignment
-- ------------------------------------------------------------

CREATE TABLE iam_assignment_new (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    tenant_id VARCHAR(64) NOT NULL,
                                    subject_pk BIGINT NOT NULL,
                                    role_id BIGINT NOT NULL,

                                    scope_type VARCHAR(32) NULL,
                                    scope_id VARCHAR(128) NULL,

                                    scope_type_norm VARCHAR(32) NOT NULL DEFAULT '__NULL__',
                                    scope_id_norm VARCHAR(128) NOT NULL DEFAULT '__NULL__',
                                    active_key INTEGER NULL DEFAULT 1,

                                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                    created_by VARCHAR(128) NULL,
                                    modified_at DATETIME(3) NULL,
                                    modified_by VARCHAR(128) NULL,
                                    deleted_at DATETIME(3) NULL,
                                    deleted_by VARCHAR(128) NULL,

                                    PRIMARY KEY (id),
                                    UNIQUE KEY uq_assignment_active_new (
                                                                         tenant_id, subject_pk, role_id, scope_type_norm, scope_id_norm, active_key
                                        ),
                                    KEY ix_assignment_tenant_new (tenant_id),
                                    KEY ix_assignment_subject_new (subject_pk),
                                    KEY ix_assignment_role_new (role_id),

                                    CONSTRAINT fk_assignment_subject_new
                                        FOREIGN KEY (subject_pk) REFERENCES iam_subject(id),
                                    CONSTRAINT fk_assignment_role_new
                                        FOREIGN KEY (role_id) REFERENCES iam_role(id)
);

INSERT INTO iam_assignment_new (
    id, tenant_id, subject_pk, role_id,
    scope_type, scope_id, scope_type_norm, scope_id_norm, active_key,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
)
SELECT
    id,
    tenant_id,
    subject_pk,
    role_id,
    scope_type,
    scope_id,
    COALESCE(scope_type, '__NULL__'),
    COALESCE(scope_id, '__NULL__'),
    CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END,
    created_at, created_by, modified_at, modified_by, deleted_at, deleted_by
FROM iam_assignment;

DROP TABLE iam_assignment;
ALTER TABLE iam_assignment_new RENAME TO iam_assignment;