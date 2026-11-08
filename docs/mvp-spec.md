# ocphotos-android — MVP spec

Native Android client for the ocphotos photos API. Scope: full parity with the
ocphotos web extension.

## API contract (ocapps, served under `/ocphotos-api/api/`)

Auth: HTTP Basic `username:app-password` on every request (images included,
via a shared OkHttp client). The OpenCloud app password is a six-word
passphrase.

Base URL: `<server>/ocphotos-api/api/`.

### Endpoints

| Method | Path | Body | Response |
|---|---|---|---|
| GET | `/stats` | - | `{assets, favorites, geo, exifPending, videos}` (long) |
| GET | `/assets` | query `before_taken`, `before_id`, `limit`, `favorites=1`, `archived=1`, `q` | `{assets: [Asset]}` keyset paginated (takenAt DESC, id DESC) |
| GET | `/assets/{id}/thumb?w=` | - | image (w 64..2048, default 400); 302 to original on failure |
| GET | `/assets/{id}/original` | - | binary |
| POST | `/assets/{id}/video-url` | - | `{url: "/ocphotos-api/api/video/{id}?exp=&sig="}` (signed, no auth) |
| POST | `/assets/{id}/favorite` | `{favorite: bool}` | 204 |
| POST | `/assets/{id}/archive` | `{archived: bool}` | 204 |
| GET | `/assets/{id}/tags` | - | `{tags: [string]}` |
| POST | `/assets/{id}/tags` | `{tag: string}` | 204 |
| DELETE | `/assets/{id}/tags/{tag}` | - | 204 |
| GET | `/tags` | - | `{tags: [{name, count}]}` |
| GET | `/tags/{tag}/assets` | - | `{assets}` |
| GET | `/albums` | - | `{albums: [{id, name, count, coverId?, createdAt}]}` |
| POST | `/albums` | `{name}` | `{id}` |
| PATCH | `/albums/{id}` | `{name}` | 204 |
| DELETE | `/albums/{id}` | - | 204 |
| GET | `/albums/{id}/assets` | - | `{assets}` |
| POST | `/albums/{id}/assets` | `{assetIds: [long]}` | 204 |
| DELETE | `/albums/{id}/assets/{assetId}` | - | 204 |
| GET | `/folders?path=` | - | `{path, folders: [{name, path, count}], assets}` |
| GET | `/places` | - | `{places: [{lat, lon, count, coverId, name?}]}` |
| GET | `/geo` | - | `{assets}` (geotagged) |
| GET | `/duplicates` | - | `{groups: [[Asset]], computed, pending}` |
| GET | `/memories/on-this-day?days=` | - | `{assets, days}` |
| GET | `/memories/highlights` | - | `{scope: "day"|"month"|"oldest", assets}` |
| GET | `/timeline/calendar` | - | `{years: [{year, count, months: [{month, count}]}]}` |
| POST | `/admin/rescan` | - | `{status}` |

### Asset

```json
{ "id": 1, "path": "/dav/spaces/...", "filename": "x.jpg", "mediaType": "image",
  "takenAt": 1794137465, "width": 1200, "height": 800, "camera": "...",
  "iso": 100, "aperture": "f/1.8", "shutter": "1/100", "focal": "26mm",
  "lat": 40.1, "lon": -3.2, "size": 100000, "favorite": false, "archived": false }
```

`takenAt` is Unix SECONDS. `camera`/`lens`/`aperture`/`shutter`/`focal`/`lat`/`lon`
are omitted when absent.

## Room model

- `assets` (PK `id`): full Asset mirror.
- `albums` (PK `id`): id, name, count, coverId, createdAt.

No schema export yet (`exportSchema = false`); migrations come when the schema
changes.

## Sync engine

`GET /assets` keyset-paginated (`before_taken`/`before_id`, `limit` 500) until
exhausted, then `upsertAll` + `deleteMissing(ids)`. Empty index → `clear()` (a
`DELETE ... WHERE id NOT IN ()` would wipe everything). Mutations (favorite,
archive, tags, albums) are direct-to-server and update the local mirror.

## Screens

timeline, memories, explore, map, favorites, archive, tags, tag detail, albums,
album detail, folders, duplicates, settings, viewer.

## Toolchain (known-good, mirror of ocnews/ocnotes-android)

AGP 9.4.1 (built-in Kotlin), Kotlin 2.4.21, KSP 2.3.9, Compose BOM 2026.09.00,
Room 2.8.5, Retrofit 3.0.0, OkHttp 5.5.1, Coil 3.6.3, osmdroid 6.1.20,
WorkManager 2.12.0. `compileSdk 37` / `minSdk 29` / `targetSdk 37`, Java 17.

## Gotchas

- `takenAt` is seconds (backend `time.Unix()`), format with `* 1000`.
- OpenCloud app password = six-word passphrase (with spaces), not the hex.
- `android:usesCleartextTraffic="true"` for self-hosted over HTTP on LAN.
- Images carry auth via a shared OkHttp + AuthInterceptor (no tokens in URLs).
- osmdroid needs a user agent and a cache dir under app storage.
