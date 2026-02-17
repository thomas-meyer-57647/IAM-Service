-- Allow re-create via restore semantics with soft-delete while keeping one active row per business key.

ALTER TABLE iam_role_permission
    ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED,
    DROP INDEX uq_role_permission,
    ADD UNIQUE KEY uq_role_permission_active (role_id, permission_id, active_key);

ALTER TABLE iam_tenant_admin
    ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED,
    DROP INDEX uq_tenant_admin,
    ADD UNIQUE KEY uq_tenant_admin_active (tenant_id, subject_pk, active_key);

ALTER TABLE iam_system_admin
    ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED,
    DROP INDEX uq_system_admin_subject,
    ADD UNIQUE KEY uq_system_admin_subject_active (subject_pk, active_key);

ALTER TABLE iam_tenant_module
    ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED,
    DROP INDEX uq_tenant_module,
    ADD UNIQUE KEY uq_tenant_module_active (tenant_id, module_id, active_key);

ALTER TABLE iam_assignment
    ADD COLUMN scope_type_norm VARCHAR(32) GENERATED ALWAYS AS (COALESCE(scope_type, '__NULL__')) STORED,
    ADD COLUMN scope_id_norm VARCHAR(128) GENERATED ALWAYS AS (COALESCE(scope_id, '__NULL__')) STORED,
    ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED,
    DROP INDEX uq_assignment,
    ADD UNIQUE KEY uq_assignment_active (tenant_id, subject_pk, role_id, scope_type_norm, scope_id_norm, active_key);
