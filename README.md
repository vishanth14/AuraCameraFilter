# Aura — Camera Filter App

A cinematic camera filter app for Android, built entirely in Java with Camera2 API.
Dark, minimal UI with 9 unique hand-crafted filters.

---

## Features

### 9 Unique Filters

| Filter | Description |
|--------|-------------|
| **Normal** | Unaltered passthrough |
| **Noir** | High-contrast cinematic B&W with S-curve tone mapping and vignette |
| **Duotone** | Deep violet shadows + golden amber highlights (Spotify-inspired) |
| **Glitch** | RGB channel shift + horizontal scanline tears for digital artifact look |
| **Infrared** | Simulates IR photography — bright foliage, darkened skies, warm cream tint |
| **Cyberpunk** | Neon teal highlights + magenta shadows with heavy S-curve contrast |
| **Dreamy** | Lifted shadows, soft pink-white bloom, pastel desaturation |
| **Vintage** | Warm film grain, faded blacks, amber cross-process tones + heavy vignette |
| **Thermal** | Full heatmap LUT (blue → cyan → green → orange → white) |
| **Hologram** | Diagonal rainbow bands + scanlines + prismatic screen-blend sheen |

All filters are applied at full resolution on capture (JPEG, 95% quality).
The filter strip shows live thumbnails from the camera feed in real time.

---

## Project Structure

```
CameraFilterApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── java/com/aura/camerafilter/
│       ├── MainActivity.java      ← Camera2 + capture logic
│       ├── FilterEngine.java      ← All 9 filter algorithms (CPU, no RenderScript)
│       ├── FilterAdapter.java     ← RecyclerView for the filter strip
│       └── FilterItem.java        ← Data model
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml  ← Full-screen camera UI
│       │   └── item_filter.xml    ← Filter strip item
│       ├── values/
│       │   ├── colors.xml
│       │   ├── strings.xml
│       │   └── themes.xml
│       └── drawable/
│           ├── gradient_top/bottom.xml
│           ├── shutter_bg.xml     ← Animated capture button
│           ├── ring_selected.xml  ← Filter selection indicator
│           └── (other shapes)
```

---

## Setup

### Requirements
- Android Studio Hedgehog or later
- Android SDK 34 (compile), minSdk 21 (Android 5.0+)
- A physical device (Camera2 doesn't work in emulator)

### Steps

1. **Open in Android Studio**
   ```
   File → Open → select the CameraFilterApp folder
   ```

2. **Sync Gradle**
   Android Studio will prompt you. Click *Sync Now*.

3. **Connect a device** and enable USB debugging.

4. **Run** (`Shift + F10`).

5. Grant Camera + Storage permissions on first launch.

---

## How It Works

### Camera2 Pipeline
- `TextureView` for live preview
- `ImageReader` (JPEG) for still capture
- `CameraCaptureSession` with auto-focus

### Filter Engine (`FilterEngine.java`)
All filters operate on raw pixel arrays (`int[]`) for speed:
- **No RenderScript** — pure Java, works on all API 21+ devices
- **LUT-based** where beneficial (Thermal heatmap)
- **S-curve contrast** helper for Noir and Cyberpunk
- **Radial gradient vignettes** drawn via `Canvas`

### Save to Gallery
Uses `MediaStore` API (Android 10+ scoped storage compatible).
Photos are saved to `Pictures/Aura/` with filename `AURA_yyyyMMdd_HHmmss.jpg`.

---

## Permissions Used

| Permission | Purpose |
|-----------|---------|
| `CAMERA` | Camera preview and capture |
| `WRITE_EXTERNAL_STORAGE` | Save photos (Android ≤ 9) |
| `READ_MEDIA_IMAGES` | Photo access (Android 13+) |
