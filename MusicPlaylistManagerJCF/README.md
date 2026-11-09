# Music Playlist Manager — Java Collection Framework

Расширение первой работы **Music Playlist Manager** для Independent Work 1.
Проект использует исходную предметную область и основные классы: `Track`, `Song`, `Podcast`,
`Audiobook`, `Playable`, `Downloadable`, `Playlist`, `PlaybackHistory`, `PlaylistManager`
и `PlaylistExporter`.

## Что реализовано по требованиям

| Требование | Реализация |
|---|---|
| List / Set | `ArrayList` хранит реестры объектов; `LinkedHashSet` хранит треки плейлиста и категории |
| equals() / hashCode() | Сущности сравниваются по неизменяемому UUID; наследники `Track` используют общий контракт `Track` |
| Map: primary key | `HashMap<UUID, Track>`, `HashMap<UUID, Playlist>`, `HashMap<UUID, User>` |
| Map: categorization | Индекс `Map<String, Set<Track>>` по жанру/типу и `Map<UserRole, Set<User>>` по роли |
| Comparable | `Track.compareTo()` — название, исполнитель, UUID |
| 3+ Comparator | `TrackComparators`: по названию, исполнителю/названию, длительности, числу прослушиваний |
| Queue / PriorityQueue | Очередь `PlaybackRequest`: приоритет 1–10, сначала высокий приоритет, при равном — FIFO по монотонному sequence number |
| CRUD | Добавление, обновление и удаление треков, плейлистов и пользователей |
| Связи | Playlist хранит `Set<Track>`; Track может быть в нескольких Playlist; история композиционно принадлежит Playlist |
| Роли | `LISTENER` подаёт запрос; `OPERATOR` извлекает и обрабатывает запрос через `poll()` |
| Аналитика | Количество треков/записей, длительность библиотеки, роялти, размеры плейлистов, категории |

## Структура

```text
src/main/java/musicplaylist/
├── Main.java
├── model/
│   ├── Track.java
│   ├── Song.java
│   ├── Podcast.java
│   ├── Audiobook.java
│   ├── Playable.java
│   ├── Downloadable.java
│   ├── Playlist.java
│   ├── PlaybackHistory.java
│   ├── User.java
│   ├── UserRole.java
│   ├── PlaybackRequest.java
│   └── RequestStatus.java
├── repository/
│   ├── TrackRepository.java
│   ├── PlaylistRepository.java
│   └── UserRepository.java
└── service/
    ├── PlaylistManager.java
    ├── TrackComparators.java
    └── PlaylistExporter.java
```

Дополнительные материалы: `uml/music-playlist-manager.puml` and `uml/music-playlist-manager.png`, `docs/TechnicalReport.docx`,
`docs/demo-output.txt`. Самопроверки без внешних библиотек находятся в `src/test/java/musicplaylist/ProjectSelfTest.java`.

## Запуск

### IntelliJ IDEA

1. Распакуйте архив и откройте папку проекта.
2. Откройте `src/main/java/musicplaylist/Main.java`.
3. Запустите метод `main()`.

### Терминал (JDK 8 или новее)

Из корневой папки проекта:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out musicplaylist.Main
```

Для Windows PowerShell можно использовать:

```powershell
New-Item -ItemType Directory -Force out
$files = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
java -cp out musicplaylist.Main
```

Также доступен `pom.xml` для импорта как Maven-проекта.

### Самопроверки

После компиляции основного и тестового исходного кода запустите:

```bash
javac -encoding UTF-8 -cp out -d out $(find src/test/java -name "*.java")
java -cp out musicplaylist.ProjectSelfTest
```

Тесты проверяют UUID-контракт `equals/hashCode`, синхронизацию индексов при обновлении/удалении,
приоритет и FIFO очереди, проверку ролей и сортировку Comparator.

Важные методы из первой версии сохранены: `PlaylistManager.findByName()` и
`PlaylistManager.getTotalTracks()` (последний считает элементы во всех плейлистах). Для подсчёта
уникальных треков библиотеки добавлен `getLibraryTrackCount()`.

## Важные правила обновления

У каждой сущности постоянный UUID. При замене трека передавайте в `PlaylistManager.updateTrack()`
новый объект с тем же UUID. Метод атомарно обновляет реестр, индекс категории и ссылки на объект
во всех плейлистах, сохраняя playCount существующего трека. Удаление трека через `removeTrack()` удаляет его из всех плейлистов и отменяет
ожидающие запросы на этот трек. Не меняйте категорию зарегистрированного трека напрямую через
setter: заменяйте объект через `updateTrack()`, чтобы сохранить согласованность индексов.

## Сложность основных операций

- Поиск по UUID в `HashMap`: O(1) в среднем.
- Добавление/удаление в `HashMap` и `HashSet`: O(1) в среднем.
- Добавление и извлечение из `PriorityQueue`: O(log n).
- Сортировка списка из n треков: O(n log n).
- Фильтрация по исполнителю: O(n); фильтрация по категории использует индекс Map.
