# दूध हिसाब · Milk Hisab

An offline-first Android app that replaces the daily paper milk notebook.

Parents record how much milk came in and at what price; the app keeps a
per-day history, works out the monthly total, and keeps everything on the
phone. No account, no cloud, no internet permission.

- **Bilingual** — नेपाली / English, switchable instantly from Settings
- **Light & dark** — with a "follow the system" option
- **Offline** — data lives in a local Room database

---

## Features

| Area | What it does |
|---|---|
| Daily record | Quantity (L) and rate per litre; the amount is always **calculated**, never typed |
| One record per day | Saving a date twice asks before editing the existing record instead of duplicating |
| History | Every day, newest first, with edit and delete |
| Monthly summary | Total milk, total amount, days recorded, daily average, per-day list, month navigation |
| Backup | JSON backup / restore through the system file picker, validated and applied in one transaction |
| Export | Monthly CSV report, shared anywhere without a storage permission |

Calculations keep exact money arithmetic (`BigDecimal`) and South-Asian
number grouping (`6,125` / `1,00,000`). Nepali digits (३.५) are understood
when typing.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Room (SQLite) + DataStore (UI preferences)
- MVVM: ViewModel → Repository → DAO, with Coroutines/Flow
- minSdk 26, targetSdk 34, Java 17, Gradle 8.9 / AGP 8.2.2

The `MilkRecord` schema is deliberately minimal and stable:

```
milk_records(id, date TEXT UNIQUE, quantity REAL, rate TEXT, amount TEXT)
```

Dates are stored as ISO-8601 strings; language and theme live in
DataStore and never touch the milk records.

## Project layout

```
app/src/main/java/com/milkhisab/app/
├── data/          Room entities, DAO, database, repository, DataStore settings
├── domain/        MilkCalculator (BigDecimal) and Validators
├── ui/            theme, strings, components, screens (home, addedit, records, summary, settings)
├── viewmodel/     one ViewModel per screen + factory
├── navigation/    routes and the three-tab bottom bar
└── utils/         Formatters, DateProvider (Asia/Kathmandu)
```

## Build

Requires JDK 17 and an Android SDK (compileSdk 34).

```bash
# Windows
set JAVA_HOME=C:\path\to\jdk-17
gradlew.bat :app:assembleDebug --no-daemon

# macOS / Linux
export JAVA_HOME=/path/to/jdk-17
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

> Build from a folder whose path contains only ASCII characters. Windows
> passes non-ASCII path arguments in the active code page, which breaks
> Kotlin/Java classpath resolution.

## Tests

```bash
gradlew.bat :app:testDebugUnitTest
```

75 unit tests cover the money calculations, input validation (including
Nepali digits), the repository and backup/restore round trip, settings
persistence, and the bilingual string catalogue.

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or copy the APK to the phone and open it (allow "install from unknown
sources"). The debug build is unsigned for release; create a keystore
before distributing through Google Play.

## Privacy

The app requests **no permissions** — in particular no `INTERNET`. Records,
settings and backups never leave the device unless the user explicitly
shares a file.
