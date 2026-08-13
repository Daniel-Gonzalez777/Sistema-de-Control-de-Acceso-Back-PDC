# acceso-api — Backend Spring Boot

API REST del sistema de control de acceso del Parque del Café (Fase 1-4 del roadmap del documento de requerimientos).

## Requisitos previos
- Java 17
- Maven (o usa el `mvnw` incluido si lo agregas desde tu IDE — IntelliJ/Eclipse lo generan automáticamente al abrir el proyecto)
- MySQL corriendo localmente (o cambia el driver a PostgreSQL en `pom.xml` y `application.properties`)

## Pasos para correrlo

1. Crea la base de datos:
   ```sql
   CREATE DATABASE parque_acceso;
   ```
2. Edita `src/main/resources/application.properties` con tu usuario/contraseña de MySQL.
3. Desde la raíz del proyecto:
   ```bash
   mvn spring-boot:run
   ```
4. La API queda disponible en `http://localhost:8080`. Al iniciar, Hibernate crea automáticamente las tablas.

## Endpoints disponibles

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/concesionarios` | Listar concesionarios |
| POST | `/api/concesionarios` | Crear concesionario |
| PUT | `/api/concesionarios/{id}` | Actualizar concesionario |
| DELETE | `/api/concesionarios/{id}` | Eliminar concesionario |
| GET | `/api/empleados` | Listar empleados |
| POST | `/api/empleados` | Crear empleado |
| **GET** | **`/api/ingreso/validar/{cedula}`** | **Endpoint clave: valida si la cédula puede ingresar** |
| POST | `/api/visitas/ingreso` | Registrar ingreso de visitante |
| POST | `/api/visitas/{id}/salida` | Registrar salida de visitante |
| GET | `/api/visitas/activas` | Visitantes que aún están dentro del Parque |

## Cómo probarlo sin frontend (con curl o Postman)

```bash
# 1. Crear un concesionario
curl -X POST http://localhost:8080/api/concesionarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Aseo Total S.A.S","nit":"900123456","activo":true}'

# 2. Crear un empleado (usa el id que te devolvió el paso anterior en concesionario.id)
curl -X POST http://localhost:8080/api/empleados \
  -H "Content-Type: application/json" \
  -d '{"cedula":"1002345678","nombre":"Juan Pérez","cargo":"Operario","area":"Zona Norte","concesionario":{"id":1}}'

# 3. Intentar validar el ingreso (en este punto va a decir NO AUTORIZADO,
#    porque todavía no existe afiliación cargada para el mes actual —
#    eso es justamente la regla RN-03 funcionando correctamente)
curl http://localhost:8080/api/ingreso/validar/1002345678
```

Para que quede AUTORIZADO, falta el endpoint de carga de afiliación (fase 2 del roadmap — carga de plantilla mensual). Por ahora, mientras se construye esa fase, puedes insertar un registro directo en la tabla `afiliacion` para probar el flujo completo del `IngresoService`.

## Siguiente paso pendiente (Fase 2)
Crear `AfiliacionController` + un endpoint `POST /api/afiliaciones/cargar-plantilla` que reciba un Excel (con Apache POI, ya incluido en el `pom.xml`) y cree/actualice registros de `Afiliacion` en lote. Dímelo cuando quieras que lo construyamos.
