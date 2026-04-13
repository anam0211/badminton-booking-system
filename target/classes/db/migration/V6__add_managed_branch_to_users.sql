SET @schema_name = DATABASE();

SET @has_managed_branch_col = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'users'
      AND column_name = 'managed_branch_id'
);

SET @add_col_sql = IF(
    @has_managed_branch_col = 0,
    'ALTER TABLE users ADD COLUMN managed_branch_id BIGINT NULL AFTER role_id',
    'SELECT 1'
);
PREPARE stmt_add_col FROM @add_col_sql;
EXECUTE stmt_add_col;
DEALLOCATE PREPARE stmt_add_col;

SET @has_managed_branch_fk = (
    SELECT COUNT(*)
    FROM information_schema.key_column_usage
    WHERE table_schema = @schema_name
      AND table_name = 'users'
      AND column_name = 'managed_branch_id'
      AND referenced_table_name = 'branches'
);

SET @add_fk_sql = IF(
    @has_managed_branch_fk = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_managed_branch FOREIGN KEY (managed_branch_id) REFERENCES branches(id) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt_add_fk FROM @add_fk_sql;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;
