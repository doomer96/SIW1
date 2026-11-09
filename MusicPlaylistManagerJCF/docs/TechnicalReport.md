# Independent Work 1 — Extending Project Architecture using Java Collection Framework

**Project:** Music Playlist Manager  
**Student:** Davlet Beishenbekov  
**Group:** COMCIF-25

## 1. Domain and continuity with the previous work

The project continues the previous Music Playlist Manager. The original domain entities and behavior are retained: abstract `Track`, concrete `Song`, `Podcast` and `Audiobook`, the `Playable` and `Downloadable` interfaces, `Playlist`, `PlaybackHistory`, `PlaylistManager` and `PlaylistExporter`. Track validation, formatted duration, royalty calculations, playback, downloading and type-specific methods remain available.

The model is extended with UUID primary keys, role-based users and playback requests. The original PlaylistManager lookup and playlist-entry count methods remain available for continuity. A playlist contains a set of tracks, while the same track can belong to several playlists. Playback history belongs to a playlist and is managed as a composition component.

## 2. Collection selection

- **List / ArrayList:** repositories preserve registries of tracks, playlists and users. Playback history is stored as a list because repeated plays and their order matter.
- **Set / LinkedHashSet:** each playlist stores unique tracks and preserves insertion order. Category and role indexes use sets to avoid duplicate entities.
- **Map / HashMap:** UUID-to-entity maps provide average O(1) primary-key lookup. Secondary maps group tracks by genre/type and users by role.
- **Queue / PriorityQueue:** playback requests are processed by priority. A higher priority value is processed first; requests with equal priority are processed in submission order.

## 3. Object contracts and sorting

Every entity has an immutable UUID business key. `Track.equals()` and `Track.hashCode()` are final and inherited by `Song`, `Podcast` and `Audiobook`, so the three track types follow one identity contract. `Playlist`, `User`, `PlaybackRequest` and `PlaybackHistory` also implement equality and hash code using their UUIDs.

`Track` implements `Comparable<Track>` with natural order by title, artist and UUID. `TrackComparators` provides four reusable dynamic comparators: title, artist then title, duration descending, and play count descending.

## 4. Service layer, CRUD and synchronization

`PlaylistManager` coordinates three repositories and the playback request queue. Its public CRUD and workflow methods are synchronized. Track update replaces the entity in the track registry, updates its category index and replaces the corresponding reference in every playlist while preserving playlist order and the track play-count statistic. Track deletion removes it from the registry, category index, all playlists and pending requests. User deletion also cancels requests submitted by that user.

Playlist CRUD includes creation, renaming and deletion. The manager supports adding/removing tracks in a playlist and searching entities by key, artist, category or name. Collections returned to callers are defensive copies, so a caller cannot directly modify the repository's internal lists or sets.

## 5. User-role workflow

A registered `LISTENER` can submit a playback request for a registered playable track. An `OPERATOR` processes a request with `PriorityQueue.poll()`. The service plays the track, changes the request status to `PROCESSED` and records playback in every playlist containing that track. A listener can cancel their own pending request; an operator can cancel any pending request. Role validation and unknown-ID cases are handled with clear exceptions or empty Optional results.

## 6. Analytics and reporting

The service provides the number of tracks in the library, total playlist entries, total library duration, estimated royalties, category counts and track counts by playlist. It also returns sorted snapshots without exposing repository storage.

## 7. Complexity notes

| Operation | Expected complexity |
|---|---:|
| Find track/user/playlist by UUID in HashMap | O(1) average |
| Add/remove in HashMap or HashSet | O(1) average |
| PriorityQueue offer/poll | O(log n) |
| Sort n tracks | O(n log n) |
| Search tracks by artist without a dedicated index | O(n) |
| Search tracks by indexed category | O(1) to find the bucket, plus O(k) to copy k matches |

## 8. Execution

The project uses only the Java standard library and is compatible with JDK 8 or newer. Run `musicplaylist.Main` after compiling the files under `src/main/java`. The demonstration covers primary-key lookup, category indexing, natural and custom ordering, priority queue processing, role validation, synchronized update/deletion, playback history, export and analytics. The captured console output is included in the DOCX report and in `docs/demo-output.txt`.
