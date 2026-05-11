package com.aura.camerafilter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AuraCam";
    private static final int REQ_PERMISSIONS = 101;

    // ── Views ──────────────────────────────────────────────────────────────────
    private TextureView   textureView;
    private RecyclerView  rvFilters;
    private ImageButton   btnCapture;
    private ImageButton   btnFlip;
    private TextView      tvFilterName;
    private View          flashOverlay;

    // ── Camera2 ────────────────────────────────────────────────────────────────
    private CameraDevice         cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageReader          imageReader;
    private HandlerThread        backgroundThread;
    private Handler              backgroundHandler;
    private String               cameraId;
    private boolean              usingFront = false;
    private Size                 previewSize;

    // ── Filters ────────────────────────────────────────────────────────────────
    private int selectedFilter = FilterEngine.FILTER_NONE;
    private FilterAdapter filterAdapter;
    private Bitmap        previewThumb;

    // ──────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        textureView  = findViewById(R.id.texture_view);
        rvFilters    = findViewById(R.id.rv_filters);
        btnCapture   = findViewById(R.id.btn_capture);
        btnFlip      = findViewById(R.id.btn_flip);
        tvFilterName = findViewById(R.id.tv_filter_name);
        flashOverlay = findViewById(R.id.flash_overlay);

        setupFilterStrip();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  UI Setup
    // ──────────────────────────────────────────────────────────────────────────
    private void setupFilterStrip() {
        List<FilterItem> filters = FilterEngine.getAllFilterItems();

        filterAdapter = new FilterAdapter(filters, filterId -> {
            selectedFilter = filterId;
            String name = filters.stream()
                    .filter(f -> f.filterId == filterId)
                    .findFirst().map(f -> f.name).orElse("");
            tvFilterName.setText(name);
            tvFilterName.animate().alpha(1f).setDuration(200)
                    .withEndAction(() -> tvFilterName.animate().alpha(0f)
                            .setStartDelay(900).setDuration(400).start()).start();
        });

        rvFilters.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        rvFilters.setAdapter(filterAdapter);
    }

    private void setupButtons() {
        btnCapture.setOnClickListener(v -> takePicture());
        btnFlip.setOnClickListener(v -> {
            usingFront = !usingFront;
            closeCamera();
            openCamera();
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Permissions
    // ──────────────────────────────────────────────────────────────────────────
    private boolean hasRequiredPermissions() {
        boolean camera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            storage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return camera && storage;
    }

    private void requestPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms = new String[]{Manifest.permission.CAMERA,
                                 Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            perms = new String[]{Manifest.permission.CAMERA,
                                 Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(this, perms, REQ_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int req,
            @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == REQ_PERMISSIONS && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Camera2
    // ──────────────────────────────────────────────────────────────────────────
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(@NonNull SurfaceTexture s, int w, int h) { openCamera(); }
        @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture s, int w, int h) {}
        @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture s) { return true; }
        @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture s) {
            // Grab a low-res thumbnail for filter strip previews (every ~60 frames)
            if (System.currentTimeMillis() % 2000 < 50) {
                Bitmap bmp = textureView.getBitmap(80, 80);
                if (bmp != null) {
                    previewThumb = bmp;
                    runOnUiThread(() -> filterAdapter.setPreviewBitmap(previewThumb));
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (!hasRequiredPermissions()) { requestPermissions(); return; }
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = pickCamera(manager, usingFront);
            CameraCharacteristics cc = manager.getCameraCharacteristics(cameraId);
            android.hardware.camera2.params.StreamConfigurationMap map =
                    cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
            previewSize = chooseBestSize(sizes, 1920, 1080);

            imageReader = ImageReader.newInstance(previewSize.getWidth(),
                    previewSize.getHeight(), ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);

            configureTransform(textureView.getWidth(), textureView.getHeight());
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera failed", e);
        }
    }

    private String pickCamera(CameraManager manager, boolean front)
            throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics cc = manager.getCameraCharacteristics(id);
            Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
            int want = front ? CameraCharacteristics.LENS_FACING_FRONT
                             : CameraCharacteristics.LENS_FACING_BACK;
            if (facing != null && facing == want) return id;
        }
        return manager.getCameraIdList()[0];
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice cam) {
            cameraDevice = cam;
            createPreviewSession();
        }
        @Override public void onDisconnected(@NonNull CameraDevice cam) { cam.close(); cameraDevice = null; }
        @Override public void onError(@NonNull CameraDevice cam, int err) { cam.close(); cameraDevice = null; }
    };

    private void createPreviewSession() {
        try {
            SurfaceTexture tex = textureView.getSurfaceTexture();
            tex.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(tex);

            previewRequestBuilder = cameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                captureSession.setRepeatingRequest(
                                        previewRequestBuilder.build(), null, backgroundHandler);
                            } catch (CameraAccessException e) { Log.e(TAG, "preview failed", e); }
                        }
                        @Override public void onConfigureFailed(@NonNull CameraCaptureSession s) {
                            Toast.makeText(MainActivity.this,
                                    "Camera session failed.", Toast.LENGTH_SHORT).show();
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "createPreviewSession", e);
        }
    }

    private void takePicture() {
        if (cameraDevice == null) return;

        // Flash animation
        flashOverlay.animate().alpha(0.85f).setDuration(60)
                .withEndAction(() -> flashOverlay.animate().alpha(0f).setDuration(200).start())
                .start();

        try {
            CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    getJpegOrientation(cameraId, rotation));

            captureSession.capture(captureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override public void onCaptureCompleted(@NonNull CameraCaptureSession s,
                                @NonNull CaptureRequest r, @NonNull TotalCaptureResult res) {
                            Log.d(TAG, "Capture complete");
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "takePicture", e);
        }
    }

    private final ImageReader.OnImageAvailableListener imageAvailableListener = reader -> {
        Image image = reader.acquireLatestImage();
        if (image == null) return;
        try {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (raw != null) {
                Bitmap filtered = FilterEngine.applyFilter(raw, selectedFilter);
                saveToGallery(filtered);
                raw.recycle();
            }
        } finally {
            image.close();
        }
    };

    private void closeCamera() {
        if (captureSession != null) { captureSession.close(); captureSession = null; }
        if (cameraDevice  != null) { cameraDevice.close();  cameraDevice  = null; }
        if (imageReader   != null) { imageReader.close();   imageReader   = null; }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Save to gallery via MediaStore
    // ──────────────────────────────────────────────────────────────────────────
    private void saveToGallery(Bitmap bitmap) {
        String filename = "AURA_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.US).format(new Date()) + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Aura");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri == null) {
            runOnUiThread(() -> Toast.makeText(this, "Failed to save photo.", Toast.LENGTH_SHORT).show());
            return;
        }

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (IOException e) {
            Log.e(TAG, "saveToGallery", e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
        }

        runOnUiThread(() -> Toast.makeText(this,
                "✓ Saved to gallery", Toast.LENGTH_SHORT).show());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Background thread
    // ──────────────────────────────────────────────────────────────────────────
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("AuraCam");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try { backgroundThread.join(); backgroundThread = null; backgroundHandler = null; }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private Size chooseBestSize(Size[] sizes, int maxW, int maxH) {
        Size best = sizes[0];
        for (Size s : sizes) {
            if (s.getWidth() <= maxW && s.getHeight() <= maxH) {
                if (s.getWidth() * s.getHeight() > best.getWidth() * best.getHeight()) {
                    best = s;
                }
            }
        }
        return best;
    }

    private void configureTransform(int viewW, int viewH) {
        if (previewSize == null) return;
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), viewW / 2f, viewH / 2f);
        }
        textureView.setTransform(matrix);
    }

    private int getJpegOrientation(String camId, int displayRotation) {
        try {
            CameraManager mgr = (CameraManager) getSystemService(CAMERA_SERVICE);
            CameraCharacteristics cc = mgr.getCameraCharacteristics(camId);
            int sensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int[] rotDegrees = {0, 90, 180, 270};
            int displayDeg = rotDegrees[displayRotation];
            Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return (sensorOrientation + displayDeg) % 360;
            } else {
                return (sensorOrientation - displayDeg + 360) % 360;
            }
        } catch (CameraAccessException e) { return 0; }
    }
}
