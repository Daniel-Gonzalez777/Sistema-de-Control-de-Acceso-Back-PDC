-- =====================================================================
-- Migracion: registrar tipo de movimiento (ENTRADA / SALIDA)
-- =====================================================================
-- Corre este script UNA sola vez sobre tu base de datos ya existente.
-- =====================================================================
USE parque_acceso;

ALTER TABLE registro_ingreso_empleado
    ADD COLUMN tipo_movimiento VARCHAR(20) NOT NULL DEFAULT 'ENTRADA' AFTER resultado;

-- Los registros que ya tenías antes de este cambio fueron todos intentos
-- de ENTRADA (la salida no existía todavía), así que el DEFAULT de arriba
-- ya los deja correctos -- no hace falta un UPDATE adicional.
