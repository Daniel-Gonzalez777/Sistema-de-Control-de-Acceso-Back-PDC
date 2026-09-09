-- =====================================================================
-- migracion-usuarios.sql — agrega el login con 3 roles: Admin, Portería,
-- Concesionario. Ejecutar UNA vez contra la base de datos existente
-- (local y/o Aiven), igual que las demás migraciones del proyecto.
-- =====================================================================

CREATE TABLE usuario (
                         id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                         username        VARCHAR(50) NOT NULL UNIQUE,
                         password        VARCHAR(255) NOT NULL,   -- hash BCrypt, nunca texto plano
                         rol             VARCHAR(20) NOT NULL,    -- ADMIN | PORTERIA | CONCESIONARIO
                         nombre_mostrar  VARCHAR(100)
) ENGINE=InnoDB;

-- 3 usuarios iniciales de arranque.
-- Contraseñas de prueba (cámbialas cuando quieras, insertando un nuevo
-- hash BCrypt en su lugar): admin123 / porteria123 / concesionario123
INSERT INTO usuario (username, password, rol, nombre_mostrar) VALUES
                                                                  ('admin',        '$2b$10$kyJnzWf6jiJVxCRTcnmij.uUIGMrIxz8YKvD/wzQm/ITinIhX0tee', 'ADMIN',        'Administrador'),
                                                                  ('porteria',     '$2b$10$7Tk3MepxtJfqawBTah2t0ekXaqQi6R0IocUtygHHrq/Y4TwHxPQwK', 'PORTERIA',     'Portería'),
                                                                  ('concesionario','$2b$10$BGWAug46kXcqWf5KuFNXUuBQ0OZohc5ermY8ayOeq9IaFMi3P3AkW', 'CONCESIONARIO','Concesionario');