# ocphotos-android

Native Android client for [ocphotos](https://github.com/gnacho/ocphotos), a
Memories-style photo app for [OpenCloud](https://opencloud.eu). It talks the
ocphotos REST API served by [ocapps](https://github.com/gnacho/ocapps) under
`/ocphotos-api/api/`.

Kotlin + Jetpack Compose + MVVM. Not a fork of any Nextcloud client: it is a
clean implementation against the ocphotos API (the `nextcloud/memories` app is
only a functional reference).

## Features

- Timeline grouped by day/month with authenticated thumbnails
- Full-screen viewer with EXIF details and quick actions (favorite, archive,
  tag, add to album, download, play video)
- Memories ("on this day" + highlights)
- Explore (search)
- Map with geotagged clusters (OpenStreetMap via osmdroid)
- Favorites, Archive, Tags, Albums, Folders, Duplicates
- Local cache (Room) and periodic sync (WorkManager)
- Light / dark theme, configurable sync interval

## Build

- Android SDK: `commandline-tools` + `platforms;android-37.0` +
  `build-tools;37.0.0` + `platform-tools` (see `docs/mvp-spec.md`).
- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest` (JVM unit tests)
- `./gradlew :app:assembleRelease` (signed when `keystore.properties` exists,
  unsigned otherwise)

## Usage

Sign in with your OpenCloud server URL, username and an app password (the
OpenCloud app passwords are a six-word passphrase, not the hex token).

## Stack

AGP 9.4.1, Kotlin 2.4.21, Compose BOM 2026.09.00, KSP 2.3.9, Room 2.8.5,
Retrofit 3 + OkHttp 5 + kotlinx.serialization, Coil 3, osmdroid 6.1.20,
WorkManager. `compileSdk 37`, `minSdk 29`.

## License

AGPL-3.0, see `LICENSE`.
