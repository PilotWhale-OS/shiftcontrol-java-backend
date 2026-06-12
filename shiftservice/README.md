# Shiftservice

## Runtime configuration

`shiftservice` expects these environment variables in normal runtime:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Optional service-specific variables:

- `SHIFTSERVICE_REST_PATH_PREFIX`
  - Empty by default.
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
