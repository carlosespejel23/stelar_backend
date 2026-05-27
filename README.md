# STELAR BACKEND
<img src="src/main/resources/assets/logo.png" alt="Stelar Logo" width="100"/>

Backend de **Stelar**, una plataforma SaaS edtech para docentes de educacion basica, media superior y superior en Mexico. Monolito modular construido con Spring Boot y Clean Architecture.

## Tech Stack

| Componente | Tecnologia | Version |
|------------|-----------|---------|
| Lenguaje | Java | 25 |
| Framework | Spring Boot | 3.5.x |
| Build | Maven | 3.9+ |
| Base de datos | PostgreSQL | 16+ |
| Migraciones | Flyway | Versionadas en `src/main/resources/db/migration/` |
| Cache | Redis | 7+ |
| Seguridad | Spring Security + JWT | jjwt 0.12.6 |
| Email | AWS SES v2 | Via AWS SDK 2.x |
| Docs API | SpringDoc OpenAPI | 2.8.x |
| Testing | JUnit 5, Mockito, Testcontainers | -- |

## Requisitos previos

- **Java 25** (JDK)
- **PostgreSQL 16+** corriendo en `localhost:5432`
- **Redis 7+** corriendo en `localhost:6379`
- **Maven 3.9+** (o usar el wrapper incluido `./mvnw`)

## Configuración

1. Clona el repositorio:

   ```bash
   git clone
    cd stellar-backend
    ``` 
2. Configura las variables de entorno (puedes usar un `.env` o exportarlas directamente):
    ```bash
    export DB_URL=jdbc:postgresql://localhost:5432/stellar
    export DB_USERNAME=tu_usuario
    export DB_PASSWORD=tu_contraseña
    
    export REDIS_HOST=localhost
    export REDIS_PORT=6379
    
    export JWT_SECRET=tu_clave_secreta_para_jwt
    ```
3. Crea la base de datos en PostgreSQL:
    ```sql  
    CREATE DATABASE stelar;
    ```
4. Ejecuta las migraciones de Flyway (opcional, se ejecutan automáticamente al iniciar la app):
    ```bash
    mvn flyway:migrate
    ```

## Ejecución

Puedes ejecutar la aplicación usando Maven:

```bash
mvn spring-boot:run
```

O usando el wrapper:

```bash
./mvnw spring-boot:run
```
La aplicación estará disponible en `http://localhost:8080`.

## Documentación API

La documentación de la API se genera automáticamente con SpringDoc OpenAPI y está disponible en `http://localhost:8080/swagger-ui.html` o `http://localhost:8080/api-docs`.

## Testing

Para ejecutar los tests unitarios e integrales, usa:

```bash
mvn test
```
Esto ejecutará todos los tests definidos en el proyecto, incluyendo aquellos que usan Testcontainers para pruebas de integración con PostgreSQL y Redis.

## Contribuciones

¡Las contribuciones son bienvenidas! Si deseas contribuir, por favor sigue estos pasos:
1. Fork el repositorio.
2. Crea una nueva rama para tu feature o bugfix.
3. Realiza tus cambios y asegúrate de que los tests pasen.
4. Haz un pull request describiendo tus cambios.
