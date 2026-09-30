-- =====================================================================
-- migracion-quitar-roles.sql — ya no se usan los roles PORTERIA ni
-- CONCESIONARIO como cuentas de login; solo queda ADMIN.
-- Ejecutar UNA vez contra local y Aiven.
-- =====================================================================

DELETE FROM usuario WHERE username IN ('porteria', 'concesionario');
