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
                                                                  ('admin',        '$2b$12$weo7G4U6ryzd7Q39cD/fqe.qehiYEZyPL1W30KsfGqn2gl5QOlQee', 'ADMIN',        'Administrador'),
                                                                  ('porteria',     '$2b$12$hRHClD1PmnKCIUc0SuTjQuU8pdC7l7K7EkuUGtEotBlxONCQTd7hC', 'PORTERIA',     'Portería'),
                                                                  ('concesionario','$2b$12$hfSZ4HsXuPwzWKamNnUPUui7805fVsc6maYGD87uWiDwx6ZYm1.Ce', 'CONCESIONARIO','Concesionario');
