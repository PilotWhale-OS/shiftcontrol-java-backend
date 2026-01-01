# pretalx-client

This module provides a **generated Java client** for the pretalx REST API, based on the official OpenAPI specification.

The client is generated during the Maven build using **OpenAPI Generator** and is intended to be consumed by other backend services as a normal dependency.

---

## Structure

```
pretalx-client
├─ src/main/openapi/
│  ├─ pretalx.yaml              # Official pretalx OpenAPI specification (vendor file)
├─ target/
│  └─ pretalx.patched.yaml      # Generated, fixed OpenAPI spec (build output)
└─ pom.xml                      # This also contains build configuration for patching & generation
```

---

## Why this module exists
The pretalx OpenAPI specification currently contains several issues that prevent
reliable Java client generation:

- Invalid OpenAPI types
- Broken enum definitions
- Empty enums
- Schemas that reference non-existent or invalid types

Rather than manually editing generated code (which is fragile and not reproducible),
this module **patches the OpenAPI spec automatically during the build** and then
generates a clean, compilable client from the patched spec.

This approach ensures:
- Reproducible builds
- No manual post-processing
- Easy updates when pretalx publishes a new spec version

---

## OpenAPI source

The **official pretalx OpenAPI specification** must be placed here: `src/main/openapi/pretalx.yaml`

This file is treated as a **vendor file**:
- It should not be manually edited
- All fixes are applied programmatically during the build

---

## Spec patching (important)

Before running OpenAPI Generator, the build executes a Groovy-based patch step
that fixes known issues in the pretalx specification.

The patching happens automatically in the Maven `generate-sources` phase.

### Patches applied

#### 1. Fix invalid `type: str`
The pretalx spec uses `type: str` in several places, which is **not valid OpenAPI**.

Example (invalid):
```yaml
answer_file:
  type: str
````

Patch:
```yaml
answer_file:
  type: string
```

This prevents the generator from creating phantom types such as `Str`.

---

#### 2. Remove `NullEnum`
The spec defines an enum containing only `null`:
```yaml
NullEnum:
  enum: [null]
```

This produces invalid Java enums with no constants.

Patch behavior:
* All `oneOf` references that include `NullEnum` are replaced with `nullable: true`
* The `NullEnum` schema is removed entirely

---

#### 3. Fix `TimezoneEnum`
`TimezoneEnum` is defined with an empty enum list:

```yaml
TimezoneEnum:
  enum: []
```

This results in a Java enum with no values, which does not compile.

Patch behavior:

* `TimezoneEnum` is rewritten as a plain string schema
* `Event.timezone` and `EventList.timezone` continue to reference it safely

Result:

```yaml
TimezoneEnum:
  type: string
  description: IANA timezone name (e.g. Europe/Vienna)
```

The generated client now uses `String` for timezones.

---

#### 4. Inline simple string aliases

Schemas that are nothing more than:

```yaml
type: string
```

and provide no additional constraints are **inlined**.

This prevents the generator from creating meaningless wrapper model classes
and ensures Java uses `java.lang.String` directly.

---

## Code generation
After patching, the client is generated using **OpenAPI Generator**.

* The generator consumes `target/pretalx.patched.yaml`
* Generated sources are compiled as part of this module
* No generated code is committed to Git

The resulting artifact is a normal JAR dependency that can be used by other modules.

---

## Tooling & dependencies

* Maven only (no external tools required)
* Groovy execution via `gmavenplus-plugin`
* YAML parsing via SnakeYAML
* OpenAPI Generator for client code generation

No Python, Node.js, or other system dependencies are required.

---

## Updating the pretalx spec
To update the client when pretalx publishes a new OpenAPI version:

1. Replace `src/main/openapi/pretalx.yaml` with the new official spec
2. Run `mvn clean package`
3. If the build fails, adjust the patch script to handle new spec issues

This keeps all fixes explicit, versioned, and reviewable.
