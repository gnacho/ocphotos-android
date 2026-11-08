# Changelog

## 0.1.4 (2026-11-08)

- In-app video playback with Media3 (signed stream URL).
- Year Rewind scrubber on the timeline.
- Encrypted app-password storage (EncryptedSharedPreferences).
- Clear image cache on logout (no cross-account images).
- Serialize sync with a Mutex (no overlapping runs).
- GitHub Actions CI (build + unit tests).
- Harden date formatting (runtime locale) and skip sync work when signed out.

## 0.1.2 (2026-11-08)

- Initial release of the native ocphotos Android client.
- Timeline grouped by day with authenticated thumbnails.
- Full-screen viewer with EXIF details and quick actions (favorite, archive, tag, add to album, download, play video).
- Memories (on this day + highlights), Explore (search), Map (OSM clusters).
- Favorites, Archive, Tags, Albums, Folders, Duplicates.
- Room local cache and WorkManager periodic sync.
- Basic app-password auth on every request (images included).
- Light/dark theme and configurable sync interval.
