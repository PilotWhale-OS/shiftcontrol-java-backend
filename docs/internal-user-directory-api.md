# Internal User Directory API

`shiftservice` is the internal source of truth for application user and contact data.

Endpoints:

- `GET /api/internal/users/{userId}` returns one known local user.
- `POST /api/internal/users/batch` returns known local users for a list of ids.
- `POST /api/internal/users/contacts` returns contact data for a list of known local users.
- `GET /api/internal/users?page={page}&size={size}&name={name}` searches known local users.

Auth expectations:

- These endpoints are for trusted internal service consumers only.
- Use either:
  - OAuth2 bearer tokens with the service-to-service authorization model already configured for internal calls, or
  - the shared `X-ShiftControl-Internal-Api-Key` header when internal API key auth is configured
- Consumers must treat admin role decisions as `shiftservice` output, not re-derive them from the IdP.

Behavior expectations:

- Returned users are only app-relevant local users known to `shiftservice`.
- Authenticated but otherwise irrelevant IdP users are intentionally not exposed here.
- Pending invitees are managed through the admin invite APIs, not through the internal user directory endpoints.

Caching guidance:

- Safe to use short-lived client-side caching for point lookups and contact reads.
- Do not assume search results are stable across invite claims, role changes, or recent logins.
- Prefer cache TTLs in the low-minutes range for read-heavy consumers.

Retry and timeout guidance:

- Use bounded timeouts for every call.
- Retry only idempotent reads.
- Use small retry counts with backoff for transient transport failures.

Pagination guidance:

- Search pagination is page/size based.
- Consumers must not assume a stable total ordering across writes happening between requests.
- Re-query from page 0 when exact freshness matters after user or invite changes.
