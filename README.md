# Aura — Camera Filter App

A cinematic camera filter app for Android, built entirely in Java using the Android Camera2 API. Aura provides a dark, minimal camera interface with 10 unique hand-crafted filters designed for real-time preview and full-resolution photo capture.

---

## ✨ Features

### 🎨 10 Unique Filters

| Filter | Description |
|---|---|
| **Normal** | Unaltered camera passthrough |
| **Noir** | High-contrast cinematic B&W with S-curve tone mapping and vignette |
| **Duotone** | Deep violet shadows + golden amber highlights |
| **Glitch** | RGB channel shift + horizontal scanline tears for a digital artifact look |
| **Infrared** | Simulates IR photography with bright foliage, darkened skies, and warm cream tint |
| **Cyberpunk** | Neon teal highlights + magenta shadows with heavy S-curve contrast |
| **Dreamy** | Lifted shadows, soft pink-white bloom, and pastel desaturation |
| **Vintage** | Warm film grain, faded blacks, amber cross-process tones, and heavy vignette |
| **Thermal** | Heatmap-style color mapping from blue → cyan → green → orange → white |
| **Hologram** | Diagonal rainbow bands + scanlines + prismatic screen-blend effect |

All filters are applied at full resolution during capture. The filter strip provides live thumbnails generated from the camera feed.

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| **Java 8** | Core application development |
| **Android SDK 34** | Compile and target SDK |
| **Camera2 API** | Camera preview, camera control, and image capture |
| **AndroidX AppCompat 1.6.1** | Android compatibility and application components |
| **ConstraintLayout 2.1.4** | Responsive UI layout |
| **RecyclerView 1.3.2** | Horizontal filter selection strip |
| **Material Components 1.11.0** | Material UI components |
| **Gradle** | Project build and dependency management |
| **MediaStore API** | Saving captured images to the device gallery |
| **Bitmap / Pixel Processing** | Image manipulation and filter processing |
| **Canvas** | Visual effects such as vignettes and overlays |
| **LUT-based Processing** | Color mapping for effects such as Thermal |

### Platform Requirements

- **Minimum SDK:** Android 5.0 (API 21)
- **Target SDK:** Android 14 (API 34)
- **Compile SDK:** Android 14 (API 34)
- **Language:** Java
- **Build Tool:** Gradle
- **IDE:** Android Studio

---

## 📁 Project Structure

```text
AuraCameraFilter/
├── app/
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── java/
│           │   └── com/aura/camerafilter/
│           │       ├── filters/
│           │       │   ├── BaseFilter.java
│           │       │   ├── CyberpunkFilter.java
│           │       │   ├── DreamyFilter.java
│           │       │   ├── DuotoneFilter.java
│           │       │   ├── FilterUtils.java
│           │       │   ├── GlitchFilter.java
│           │       │   ├── HologramFilter.java
│           │       │   ├── InfraredFilter.java
│           │       │   ├── NoirFilter.java
│           │       │   ├── NormalFilter.java
│           │       │   ├── ThermalFilter.java
│           │       │   └── VintageFilter.java
│           │       │
│           │       ├── FilterAdapter.java
│           │       ├── FilterEngine.java
│           │       ├── FilterItem.java
│           │       └── MainActivity.java
│           │
│           └── res/
│               ├── drawable/
│               ├── layout/
│               └── values/
│
├── gradle/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

### Main Components

- **MainActivity.java** — Handles the camera UI, Camera2 lifecycle, preview, and capture flow.
- **FilterEngine.java** — Coordinates filter processing and applies the selected filter.
- **filters/** — Contains the individual filter implementations.
- **BaseFilter.java** — Base abstraction used by the filter implementations.
- **FilterUtils.java** — Shared image and pixel-processing utilities.
- **FilterAdapter.java** — Manages the RecyclerView filter strip.
- **FilterItem.java** — Data model representing a filter item.
- **AndroidManifest.xml** — Declares application configuration and required permissions.
- **res/** — Contains layouts, colors, themes, drawables, and other Android resources.

---

## ⚙️ Setup

### Requirements

- Android Studio Hedgehog or later
- Android SDK 34
- JDK compatible with the Android Gradle Plugin
- Physical Android device recommended
- USB debugging enabled on the device

> **Note:** Aura uses the Android Camera2 API. A physical Android device is recommended for testing camera functionality because emulator camera behavior can vary between devices and API images.

---

## 🚀 Installation & Running

### 1. Clone the Repository

```bash
git clone https://github.com/vishanth14/AuraCameraFilter.git
```

Then open the project in Android Studio.

### 2. Open the Project

In Android Studio:

```text
File → Open → AuraCameraFilter
```

### 3. Sync Gradle

Allow Android Studio to sync the Gradle project.

The project should compile successfully using:

```text
compileSdk 34
targetSdk 34
minSdk 21
```

### 4. Connect an Android Device

Enable:

```text
Developer Options
    ↓
USB Debugging
    ↓
ON
```

Connect your Android phone to the computer and accept the USB debugging authorization prompt.

### 5. Run the Application

Select your connected Android device in Android Studio and run:

```text
Run → Run 'app'
```

or press:

```text
Shift + F10
```

### 6. Grant Permissions

On first launch, grant the requested camera and image/storage permissions.

---

## 📱 APK Download

If you only want to test the application without opening the Android Studio project, download the APK from the project's GitHub Releases.

**[Download the latest APK](../../releases/latest)**

> The APK is provided for testing purposes. For the best Camera2 compatibility, install it on a physical Android device.

### Manual APK Installation

1. Download `AuraCameraFilter.apk`.
2. Transfer it to your Android phone if necessary.
3. Open the APK.
4. If Android asks for permission to install apps from that source, allow it.
5. Install the application.
6. Launch **Aura**.
7. Grant the required permissions.

---

## 📷 How It Works

### Camera2 Pipeline

Aura uses the Android Camera2 API for its camera pipeline.

```text
Camera
   │
   ▼
TextureView
   │
   ▼
Live Camera Preview
   │
   ├───────────────┐
   │               │
   ▼               ▼
Filter Strip    Shutter
   │               │
   ▼               ▼
Selected Filter  ImageReader
                   │
                   ▼
              Captured JPEG
                   │
                   ▼
             Filter Processing
                   │
                   ▼
              Save to Gallery
```

### Camera Components

- **TextureView** — Displays the live camera preview.
- **Camera2 API** — Controls the camera and capture session.
- **ImageReader** — Receives captured JPEG frames.
- **CameraCaptureSession** — Manages camera capture requests.
- **Auto-focus** — Used during image capture.

---

## 🎨 Filter Engine

The filtering system is implemented in pure Java without RenderScript.

Each filter is implemented as a separate class under:

```text
com.aura.camerafilter.filters
```

The filters operate on image pixel data and apply different color transformations and visual effects.

### Processing Techniques

- RGB pixel manipulation
- Contrast adjustment
- S-curve tone mapping
- Color channel manipulation
- Vignette generation
- Scanline effects
- RGB channel shifting
- Film grain simulation
- Bloom-style effects
- Color mapping / LUT processing
- Canvas-based visual overlays

### Filter Architecture

```text
FilterEngine
     │
     ▼
Selected Filter
     │
     ├── NormalFilter
     ├── NoirFilter
     ├── DuotoneFilter
     ├── GlitchFilter
     ├── InfraredFilter
     ├── CyberpunkFilter
     ├── DreamyFilter
     ├── VintageFilter
     ├── ThermalFilter
     └── HologramFilter
```

---

## 💾 Save to Gallery

Captured images are saved using the Android `MediaStore` API.

Photos are stored under:

```text
Pictures/Aura/
```

with filenames following the format:

```text
AURA_yyyyMMdd_HHmmss.jpg
```

JPEG images are saved at high quality.

The implementation is compatible with Android's scoped storage model on modern Android versions.

---

## 🔐 Permissions Used

| Permission | Purpose |
|---|---|
| `CAMERA` | Camera preview and image capture |
| `WRITE_EXTERNAL_STORAGE` | Saving photos on Android versions that require it |
| `READ_MEDIA_IMAGES` | Image access on Android 13+ |

The application requests only the permissions required for its camera and image functionality.

---

## 🖥️ User Interface

Aura follows a dark and minimal camera interface designed to keep the camera preview as the primary focus.

### Main UI Elements

- Full-screen camera preview
- Top camera controls
- Horizontal filter selection strip
- Live filter thumbnails
- Selected-filter indicator
- Animated shutter button
- Minimal dark theme

---

## ⚠️ Known Limitations

- Camera behavior may vary across Android devices because Camera2 capabilities differ between manufacturers.
- Image filtering is CPU-based and may require additional processing time on lower-end devices.
- Emulator camera support may not accurately represent physical-device Camera2 behavior.
- Some advanced camera controls may not be available on devices with limited Camera2 support.

---

## 🔮 Future Improvements

Potential improvements for future versions include:

- Front-camera support
- Camera flash control
- Zoom controls
- Exposure adjustment
- Focus control
- Video recording with filters
- GPU-accelerated filter processing
- More customizable filter parameters
- Filter intensity controls
- Improved performance on low-end devices
- Additional cinematic presets

---

## 📸 Screenshots

Add application screenshots here to showcase the camera interface and filters.

Example:

```markdown
![Aura Camera Preview](screenshots/camera-preview.png)
![Aura Filters](screenshots/filters.png)
```

Recommended screenshot folder:

```text
screenshots/
├── camera-preview.png
├── filters.png
└── captured-photo.png
```

---

## 🧪 Build

To build the debug APK from the command line:

### Windows

```bash
gradlew.bat assembleDebug
```

### macOS / Linux

```bash
./gradlew assembleDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 Release Build

For a release build, configure the appropriate signing credentials in Android Studio and generate a signed APK or App Bundle.

```text
Build
   ↓
Generate Signed Bundle / APK
```

Do not commit private signing keys or passwords to the repository.

---

## 🤝 Contributing

Contributions, suggestions, and improvements are welcome.

If you would like to contribute:

1. Fork the repository.
2. Create a new branch.
3. Make your changes.
4. Test the application on a physical Android device.
5. Create a pull request.

---

## 📄 License

Add your preferred open-source license here.

For example:

```text
MIT License
```

if the project is intended to be distributed under the MIT License.

---

## 👨‍💻 Author

**Vishanth K**

GitHub: [@vishanth14](https://github.com/vishanth14)

---

## ⭐ Support

If you find Aura useful or interesting, consider giving the repository a ⭐ on GitHub!
