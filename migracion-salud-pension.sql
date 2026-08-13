-- =====================================================================
-- Migracion: separar Seguridad Social (SS) en Salud y Pension
-- =====================================================================
-- Corre este script UNA sola vez sobre tu base de datos ya existente
-- (la que ya tenias con afiliado_ss / fecha_afiliacion_ss).
-- Si vas a crear la base de datos desde cero, mejor usa el schema.sql
-- actualizado y no necesitas este archivo.
-- =====================================================================
USE parque_acceso;

-- 1) Agregar las columnas nuevas
ALTER TABLE afiliacion
    ADD COLUMN afiliado_salud   BOOLEAN NOT NULL DEFAULT FALSE AFTER mes,
    ADD COLUMN eps              VARCHAR(255) AFTER afiliado_salud,
    ADD COLUMN fecha_afiliacion_salud DATE AFTER eps,
    ADD COLUMN afiliado_pension BOOLEAN NOT NULL DEFAULT FALSE AFTER fecha_afiliacion_salud,
    ADD COLUMN afp              VARCHAR(255) AFTER afiliado_pension,
    ADD COLUMN fecha_afiliacion_pension DATE AFTER afp,
    ADD COLUMN arl              VARCHAR(255) AFTER afiliado_arl;

-- 2) Migrar los datos que ya tenias: si afiliado_ss era TRUE, se asume
--    que cubria salud Y pension (no habia forma de saberlo separado antes).
UPDATE afiliacion
SET afiliado_salud = afiliado_ss,
    fecha_afiliacion_salud = fecha_afiliacion_ss,
    afiliado_pension = afiliado_ss,
    fecha_afiliacion_pension = fecha_afiliacion_ss;

-- 3) Eliminar las columnas viejas, ya migradas
ALTER TABLE afiliacion
    DROP COLUMN afiliado_ss,
    DROP COLUMN fecha_afiliacion_ss;
