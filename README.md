# TimeView - File System Version Viewer

A desktop Java application that lets users "travel back in time" to see how files or folders changed over time. Think of it like Apple’s Time Machine, but just for selected directories/files, and built with Java.

## Features (Planned)
- Directory/Folder Watcher
- Snapshot Capture
- Diff Viewer
- Time Travel Mode
- Git Backend (optional)
- Logs/History Timeline

## Tech Stack
- Java 17
- JavaFX (UI)
- Gradle (build)
- WatchService API
- java-diff-utils, Apache Commons IO, SQLite (future)

## Setup
1. Install Java 17+
2. Install Gradle (or use the Gradle wrapper)
3. Clone this repo
4. Run: `gradle run`

## Project Structure
- `src/main/java/timeview/` - Main codebase
- `src/main/resources/` - FXML, icons, etc.

## Development Plan
Features will be added incrementally. See comments in code for stubs and next steps. 