# AGENTS.md — ocphotos-android

Native Android client of [ocphotos](https://github.com/gnacho/ocphotos) (a
Memories-style photo app for OpenCloud). Kotlin + Jetpack Compose + MVVM, no
fork of a Nextcloud client: own implementation against the ocphotos REST API
served by ocapps under `/ocphotos-api/api/`.

## What it is
- Android app consuming the photos API of `gnacho/ocapps`.
- Functional reference: `pulsejet/memories` (AGPL-3.0). It is NOT copied: it is
  only used as a reference for functionality and UX.

## Structure
- `docs/audit.md` — feature parity + API contract + gotchas.
- `docs/mvp-spec.md` — full spec (scope, Room model, sync engine).
- `app/` — Android module (`app.ocphotos`).

## How we work
- Idiomatic Kotlin, Compose, MVVM (UI + ViewModel + Repository).
- Room + DataStore, Retrofit/OkHttp + kotlinx.serialization, Coroutines/Flow,
  WorkManager, Coil 3, osmdroid.
- Conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`), branches
  `<tipo>/<desc>`.
- Spanish for communication, English in code and repo docs.
