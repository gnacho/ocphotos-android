# ocphotos-android — audit (parity with the ocphotos web app)

Reference: the `ocphotos` web extension (`gnacho/ocphotos`, Vue 3) and its
backend `gnacho/ocapps` (Go). This client targets the same REST API.

## Parity

| Feature | Web (Vue) | Android | Notes |
|---|---|---|---|
| Timeline (day/month) | yes | yes | grouped by local day, lazy grid |
| Viewer + EXIF | yes | yes | original + metadata panel |
| Favorite | yes | yes | direct-to-server + local mirror |
| Archive | yes | yes | |
| Tags (list + per asset) | yes | yes | |
| Albums (CRUD + assets) | yes | yes | |
| Folders | yes | yes | breadcrumb navigation |
| Memories (on-this-day) | yes | yes | hero + strip |
| Explore (search) | yes | yes | `/assets?q=` |
| Map (places/geo) | yes | yes | osmdroid + OSM, cluster pins |
| Duplicates | yes | yes | groups + archive duplicates |
| Video | yes | yes | signed URL opened in system player |
| Rewind (year/month scrubber) | yes | partial | calendar data loaded; scrubber UI pending |
| Face recognition / AI tags | no | no | out of scope (no ML) |

## Key differences

- Auth: Android sends Basic auth on every request through a shared OkHttp
  interceptor, so images never carry tokens in the URL (the web extension had
  to use blob URLs for the same reason).
- Videos: Android opens the signed stream URL in the system player instead of an
  in-app HLS player (the host CSP forbids `blob:`; in-app MSE is out of scope).

## Backend gotchas (verified against ocapps source)

- `takenAt` is Unix seconds, not millis.
- `DELETE /assets/{id}/tags/{tag}` returns 204 on success.
- Favorites/archive/tags write an XMP sidecar next to the photo (best effort);
  a failed sidecar write does not fail the API call.
- `/assets` keyset is `(before_taken, before_id)` with order `takenAt DESC,
  id DESC`; passing the last element of each page is correct.
- The empty-index reconciliation must `clear()` rather than `DELETE ... WHERE
  id NOT IN ()` (SQLite `NOT IN ()` matches every row).
