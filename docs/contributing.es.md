# Contribuir

## Estructura del proyecto

| Módulo                | Contenido                                                                |
|-----------------------|--------------------------------------------------------------------------|
| `ema-core`            | Kotlin Multiplatform. Todo el código está en `commonMain`.               |
| `ema-android`         | Librería Android compartida por las dos tecnologías de UI.               |
| `ema-android-compose` | Soporte de Compose, publicado como `ema-compose`.                        |
| `ema-android-view`    | Soporte del sistema de vistas de Android.                                |
| `sample`              | Un build de Gradle independiente con la app de ejemplo. Incluye los módulos de la librería del repositorio. |

## Compilar

```bash
./gradlew assembleDebug
```

### Los targets de `ema-core`

`ema-core` compila por defecto todos los targets multiplataforma, algo necesario para publicarlo pero lento para el día
a día. Elige los targets con la propiedad `ema.targets`, en tu `local.properties` (no versionado) o con
`-Pema.targets=...`:

```properties
# local.properties
ema.targets=jvm,js
```

Los valores válidos son `all`, `jvm`, `js`, `wasm`, `apple`, `linux`, `windows` y `androidNative`, separados por comas.
El target JVM se compila siempre, porque lo usan los módulos de Android.

## Tests

| Módulo                | Tests                                   | Comando                                        |
|-----------------------|-----------------------------------------|------------------------------------------------|
| `ema-core`            | `commonTest`, en todos los targets activos | `./gradlew :ema-core:allTests`              |
| `ema-android`         | Robolectric                             | `./gradlew :ema-android:testDebugUnitTest`     |
| `ema-android-view`    | Robolectric                             | `./gradlew :ema-android-view:testDebugUnitTest`|
| `ema-android-compose` | Tests de UI de Compose sobre Robolectric | `./gradlew :ema-android-compose:testDebugUnitTest` |

Los recursos que solo necesitan los tests están en `src/test/res`, así que no se publican.

### Cobertura

[Kover](https://github.com/Kotlin/kotlinx-kover) agrega la cobertura de los cuatro módulos:

```bash
./gradlew koverHtmlReport
```

El informe se genera en `build/reports/kover/html`. `koverXmlReport` genera la versión XML.

## Formato del código

El código se formatea con [Spotless](https://github.com/diffplug/spotless) y ktlint. Las reglas están en el
`.editorconfig` del repositorio: estilo de código de Android Studio y líneas de hasta 120 caracteres.

```bash
./gradlew spotlessApply   # formatear
./gradlew spotlessCheck   # comprobar
```

Ejecútalos también desde la carpeta `sample`, para la app de ejemplo. Los commits que solo formatean el código están en
`.git-blame-ignore-revs`; actívalo una vez con `git config blame.ignoreRevsFile .git-blame-ignore-revs`.

## Documentación

La documentación es la carpeta `docs/`, Markdown normal que se ve bien en GitHub. Cada página está en inglés
(`page.md`) y en español (`page.es.md`): cuando cambies una, actualiza también la otra. Para verla como una web con
[MkDocs](https://www.mkdocs.org/), el tema Material y el selector de idioma:

```bash
python3 -m venv .venv
.venv/bin/pip install mkdocs-material mkdocs-static-i18n
.venv/bin/mkdocs serve
```
