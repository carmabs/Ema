# Contributing

## Project structure

| Module                | Contents                                                                 |
|-----------------------|--------------------------------------------------------------------------|
| `ema-core`            | Kotlin Multiplatform. All the code lives in `commonMain`.                |
| `ema-android`         | Android library shared by both UI technologies.                          |
| `ema-android-compose` | Compose support, published as `ema-compose`.                             |
| `ema-android-view`    | Android View system support.                                             |
| `sample`              | A separate Gradle build with the sample app. It includes the library modules from the repository. |

## Building

```bash
./gradlew assembleDebug
```

### The targets of `ema-core`

`ema-core` compiles every multiplatform target by default, which is needed to publish it but slow for daily work.
Choose the targets with the `ema.targets` property, in your `local.properties` (not versioned) or with
`-Pema.targets=...`:

```properties
# local.properties
ema.targets=jvm,js
```

The valid values are `all`, `jvm`, `js`, `wasm`, `apple`, `linux`, `windows` and `androidNative`, separated by commas.
The JVM target is always compiled, because the Android modules use it.

## Tests

| Module                | Tests                                   | Command                                        |
|-----------------------|-----------------------------------------|------------------------------------------------|
| `ema-core`            | `commonTest`, run on every enabled target | `./gradlew :ema-core:allTests`                |
| `ema-android`         | Robolectric                             | `./gradlew :ema-android:testDebugUnitTest`     |
| `ema-android-view`    | Robolectric                             | `./gradlew :ema-android-view:testDebugUnitTest`|
| `ema-android-compose` | Compose UI tests on Robolectric         | `./gradlew :ema-android-compose:testDebugUnitTest` |

The resources that only the tests need live in `src/test/res`, so they are not published.

### Coverage

[Kover](https://github.com/Kotlin/kotlinx-kover) aggregates the coverage of the four modules:

```bash
./gradlew koverHtmlReport
```

The report is written to `build/reports/kover/html`. `koverXmlReport` writes the XML version.

## Code format

The code is formatted with [Spotless](https://github.com/diffplug/spotless) and ktlint. The rules are in the
`.editorconfig` of the repository: Android Studio code style and lines of up to 120 characters.

```bash
./gradlew spotlessApply   # format
./gradlew spotlessCheck   # verify
```

Run them from the `sample` folder too, for the sample app. The commits that only format the code are listed in
`.git-blame-ignore-revs`; enable it once with `git config blame.ignoreRevsFile .git-blame-ignore-revs`.

## Documentation

The documentation is the `docs/` folder, plain Markdown that renders on GitHub. Every page is in English (`page.md`)
and in Spanish (`page.es.md`): when you change one, update the other too. To preview it as a website with
[MkDocs](https://www.mkdocs.org/), the Material theme and the language selector:

```bash
python3 -m venv .venv
.venv/bin/pip install mkdocs-material mkdocs-static-i18n
.venv/bin/mkdocs serve
```
