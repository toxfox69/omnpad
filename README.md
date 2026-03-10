# OmniPad

Android notepad that opens everything.

## Features

- **100+ file types** — text, code, markdown, JSON, XML, YAML, CSV, PDF, DOCX, XLSX, PPTX, images, archives, hex view for binary
- **Syntax highlighting** — 30+ languages (Python, JS/TS, Java, Kotlin, C/C++, Rust, Go, SQL, HTML, CSS, Bash, and more)
- **Tabs** — multiple files open simultaneously
- **Search & replace** — find across content, replace all
- **Word wrap toggle** — code mode vs document mode
- **Line numbers** — toggleable
- **Dark theme** — cyberpunk aesthetic, easy on the eyes
- **Open from anywhere** — file picker, share intent, file manager integration
- **Edit & save** — full editing for text-based files with save and save-as
- **Office documents** — extracts text from DOCX, DOC, XLSX, XLS, PPTX (read-only)
- **PDF** — text extraction with PDFBox
- **Archives** — lists contents of ZIP, TAR, GZ, 7Z, RAR, JAR, APK
- **Images** — renders PNG, JPG, GIF, WebP, SVG, HEIC, AVIF
- **Hex viewer** — binary files shown as hex + ASCII dump
- **Auto-detect** — unknown extensions tested for text content before falling back to hex

## Build

1. Clone this repo
2. Open in Android Studio (Ladybug 2024.2+)
3. Sync Gradle
4. Run on device/emulator (API 26+)

```bash
git clone <repo>
cd omnpad
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

- **Kotlin + Jetpack Compose** — modern Android UI
- **Material 3** — dark color scheme
- **Apache POI** — Office document parsing
- **PDFBox Android** — PDF text extraction
- **Coil** — image loading (including SVG, GIF, HEIC)
- **Commons Compress** — archive inspection
- **No cloud, no accounts, no telemetry** — everything runs locally

## ENERGENAI LLC
