# Shiftservice

## Runtime configuration

`shiftservice` expects these environment variables in normal runtime:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Optional service-specific variables:

- `SHIFTSERVICE_REST_PATH_PREFIX`
  - Defaults to `/`, which keeps endpoints at `/api/v1/...` with no extra prefix.
  - Example: set `/shiftservice` to expose endpoints as `/shiftservice/api/v1/...`.
- `SPRING_LIQUIBASE_CONTEXTS`
  - Defaults to `dev`.
  - Controls which Liquibase context-tagged change sets are applied.

## Database and Liquibase

Main configuration lives in [application.yml](./src/main/resources/application.yml).

The service runs Hibernate with `ddl-auto=validate`, so schema creation and migration are handled by Liquibase, not Hibernate.

Liquibase is enabled by default and uses:

- changelog: `classpath:db/changelog-master.xml`
- default context: `dev`

## Environments

### Development

Use:

- `SPRING_LIQUIBASE_CONTEXTS=dev` or leave it unset

Effect:

- schema migrations run
- development seed data in `db/data/*` runs because those change sets are tagged with `context="dev"`

### Production

Use:

- `SPRING_LIQUIBASE_CONTEXTS=prod`

Effect:

- schema migrations still run
- `dev` seed data is skipped

This matters because the seed data files under `db/data/` are marked with the `dev` Liquibase context.

## OpenAPI profile

The `openapi` profile uses [application-openapi.yml](./src/main/resources/application-openapi.yml):

- in-memory H2 datasource
- Liquibase disabled
- Hibernate `ddl-auto=none`

That profile is intended only for OpenAPI generation, not normal app runtime.

### Generate `openapi.json`

From the `shiftservice` project directory run:

```bash
mvn verify -Popenapi
```

What this does:

- packages the service jar, then starts it on port `18080` with the `openapi` Spring profile
- waits until `http://127.0.0.1:18080/v3/api-docs` is reachable
- writes the generated file to `target/openapi.json`
- stops the temporary OpenAPI process again after generation

This flow does not use the Spring Boot Maven plugin's JMX `start`/`stop` support, so it avoids the default JMX port `9001`.

If you are running from the Java multimodule root instead of the `shiftservice` directory, use:

```bash
mvn verify -pl shiftservice -am -Popenapi
```
