package com.newgen.clover;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Accessory;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FacialHair;
import com.microsoft.projectoxford.face.contract.Hair;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperation;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperationResult;
import com.microsoft.projectoxford.vision.contract.HandwritingTextLine;
import com.microsoft.projectoxford.vision.contract.HandwritingTextWord;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class blind_services extends AppCompatActivity implements SensorEventListener{

    boolean isrealtime;

    private static final int REQUEST_SELECT_IMAGE = 0;
    Button mButtonSelectImage;
    private Uri mImageUri;
    boolean isprocessing = false;
    private Bitmap mBitmap;
    int state = 0;
    ImageButton scenedetectbtn, ocrbtn, handbtn, facebtn, colorbtn;
    private int retryCountThreshold = 30;
    private VisionServiceClient client;
    private Synthesizer m_syn;
    public static Uri oneaboveall;
    DetectionTask mDetectionTask;
    doRequest mDoRequest;

    private String mResultFinal;

    AudioManager audioManager;
    int originalVolume;

    boolean isFlashOn;

    Sensor mySensor;
    SensorManager mySensorManager;
    float accel;
    boolean stabler;
    float accelCurrent;
    float accelLast;
    int temp;
    int state2;
    long time1;
    int state3;

    private boolean calledForResult;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private  boolean flash_on = false;
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String TAG = "Camera2BasicFragment";
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }
    };
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private File mFile;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
    };
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mState = STATE_PREVIEW;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private boolean mFlashSupported;
    private int mSensorOrientation;
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                this.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                                Log.e("TAGGY", "Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mySensorManager.unregisterListener(this);
        closeCamera();
        if(m_syn!=null)
        {
            m_syn.stopSound();
        }
        finish();
    }

    private void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
                mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                        mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.e("Tag","Saved: " + mFile);
                    mImageUri = Uri.fromFile(mFile);
                    doDescribe();
                    Log.d(TAG, mFile.toString());
                    if(mCaptureSession!=null)
                    {
                        unlockFocus();
                    }
                }
            };
            /*
            if(mCameraId.equals("0")&&isFlashOn==false)
            {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                    isFlashOn = true;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            */
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        // ACCELEROMETER LAST READ EQUAL TO THE CURRENT ONE
        accelLast = accelCurrent;
        // QUICK MAFS TO CALCULATE THE ACCELERATION
        accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
        // DELTA BETWEEN THE CURRENT AND THE LAST READ OF THE ACCELEROMETER
        float delta = accelCurrent - accelLast;
        // QUICK MAFS TO CALCULATE THE ACCEL THAT WILL DECLARE IF IT SHAKED OR NOT
        accel = accel * 0.9f + delta;
        // DID IT SHAKE??
        if(state2==1)
        {
            time1 = System.currentTimeMillis();
            state2 = 0;
        }
        else{
            if(System.currentTimeMillis()>=(time1+1250))
            {
                stabler = true;
                if(state3==0)
                {
                    if(mCaptureSession!=null) {
                        takePicture();
                    }
                    state3 = 1;
                }
            }
        }
        if (accel > 0.7) {
            /*try {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
            }catch (Exception e)
            {
                Toast.makeText(blind_services.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            */
            temp = 0;
            stabler = false;
            state2 = 1;
            state3=0;
            cancelAsyncTasks();
        }
        else {
            state2 = 0;
        }
        long time = System.currentTimeMillis();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void manualtakepic(View view) {
        if(isrealtime==false&&calledForResult==true)
        {
            isprocessing=true;
            takePicture();
        }
        else if(isrealtime==false)
        {
            if(mCaptureSession!=null&&isprocessing==false) {
                isprocessing=true;
                takePicture();
            }
        }
    }


    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
                oneaboveall = Uri.fromFile(mFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.blind_services_layout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.visual_toolbar);
        setSupportActionBar(myToolbar);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
        scenedetectbtn = (ImageButton)findViewById(R.id.descscenebtn);
        ocrbtn = (ImageButton)findViewById(R.id.ocrbtn);
        handbtn = (ImageButton)findViewById(R.id.handwritingrecogbtn);
        facebtn = (ImageButton)findViewById(R.id.faceapibtn);
        colorbtn = (ImageButton)findViewById(R.id.colorrecogbtn);
        //mButtonSelectImage = (Button)findViewById(R.id.buttonSelectImage);
       // mTextView = (TextView) findViewById(R.id.textViewResult);
        /*mButtonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do your stuff here

                //Intent intent;
                //intent = new Intent(blind_services.this, SelectImageActivity.class);
                //startActivityForResult(intent, REQUEST_SELECT_IMAGE);

                takePicture();
            }
        });
        */
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        mFile = new File(this.getExternalFilesDir(null), "pic.jpg");

        state2 = 1;
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accel = 0.00f;
        mySensorManager.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_UI);
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
        stabler=false;
        time1 = 0;
        temp = 0;
        if(blind_services.this.getCallingActivity()!=null){
            calledForResult = true;
            isrealtime = false;
            mySensorManager.unregisterListener(blind_services.this);
        }
        else {
            calledForResult = false;
            isrealtime = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.visual, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // Toast.makeText(blind_services.this, "HELLA", Toast.LENGTH_LONG).show();
        cancelAsyncTasks();
        if(calledForResult==false) {
            isrealtime = isrealtime ? false : true;

            if (isrealtime == true) {
                mySensorManager.registerListener(blind_services.this, mySensor, SensorManager.SENSOR_DELAY_UI);
                item.setIcon(R.drawable.ic_timelapse_black_24dp);
                item.setIcon(R.drawable.ic_timelapse_black_24dp);
                if (m_syn == null) {
                    // Create Text To Speech Synthesizer.
                    m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                }
                m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                m_syn.SetVoice(v, null);
                m_syn.SpeakToAudio("Realtime activated");
            } else {
                mySensorManager.unregisterListener(blind_services.this);
                item.setIcon(R.drawable.ic_timer_off_black_24dp);
                if (m_syn == null) {
                    // Create Text To Speech Synthesizer.
                    m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                }
                m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                m_syn.SetVoice(v, null);
                m_syn.SpeakToAudio("Realtime deactivated");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
            super.onResume();
        startBackgroundThread();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        if(calledForResult==false)
        mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_UI);
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        mySensorManager.unregisterListener(this);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        if(m_syn!=null)
        {
            m_syn.stopSound();
        }
        closeCamera();
        if(mBackgroundHandler!=null){
            stopBackgroundThread();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        mySensorManager.unregisterListener(this);
        if(m_syn!=null)
        {
            m_syn.stopSound();
        }
        closeCamera();
    }

    public void doDescribe() {
        mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());
       // mButtonSelectImage.setEnabled(false);
        if(state!=3)
        {
            try {
               // new doRequest(blind_services.this).execute();
                mDoRequest = new doRequest(blind_services.this);
                mDoRequest.execute();
            } catch (Exception e) {
                Log.e("dodescrerror", e.getMessage());
            }
        }
        else{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            //new DetectionTask().execute(inputStream);
            mDetectionTask = new DetectionTask();
            mDetectionTask.execute(inputStream);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DescribeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen
                        // Add detection log.
                        Log.d("DescribeActivity", "Image: " /*+ mImageUri*/ + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doDescribe();
                    }
                }
                break;
            default:
                break;
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        String result = "";
        switch (state) {
            case 0: {
                //desc scene
                AnalysisResult v = blind_services.this.client.describe(inputStream, 1);
                result = gson.toJson(v);
            }
            break;
            case 1:{
                //ocr
                OCR ocr = blind_services.this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);
                result = gson.toJson(ocr);
            }
            break;
            case 2:{
                //handwriting
                HandwritingRecognitionOperation operation = this.client.createHandwritingRecognitionOperationAsync(inputStream);

                HandwritingRecognitionOperationResult operationResult;
                //try to get recognition result until it finished.

                int retryCount = 0;
                do {
                    try {
                        if (retryCount > retryCountThreshold) {
                            throw new InterruptedException("Can't get result after retry in time.");
                        }
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e("Handwriting Recognition", e.getMessage());
                    }
                    operationResult = this.client.getHandwritingRecognitionOperationResultAsync(operation.Url());
                }
                while (operationResult.getStatus().equals("NotStarted") || operationResult.getStatus().equals("Running"));

                result = gson.toJson(operationResult);
                Log.d("result", result);
            }
            break;
            case 3:{
                //person detection
                mDetectionTask = new DetectionTask();
                //new DetectionTask().execute(inputStream);
                mDetectionTask.execute(inputStream);
            }
            break;
            case 4:{
                //color detection
                String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories"};
                String[] details = {};
                AnalysisResult v = this.client.analyzeImage(inputStream, features, details);
                result = gson.toJson(v);
            }
            break;
        }
       // Log.d("result", result);

        return result;
    }

    public void setSelectedMargins(View v)
    {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            Resources r = blind_services.this.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    0,
                    r.getDisplayMetrics()
            );
            p.setMargins(px, px, px, px);
            v.requestLayout();
        }
    }

    public void setDeselectedMargins(View v)
    {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            Resources r = blind_services.this.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6,
                    r.getDisplayMetrics()
            );
            p.setMargins(px, px, px, px);
            v.requestLayout();
        }
    }

    public void cancelAsyncTasks(){
        if(mDetectionTask!=null) {
            mDetectionTask.cancel(true);
        }
        if(mDoRequest!=null) {
            mDoRequest.cancel(true);
        }
    };

    public void desc(View view) {
        cancelAsyncTasks();
        state = 0;
        setSelectedMargins(scenedetectbtn);
        setDeselectedMargins(ocrbtn);
        setDeselectedMargins(handbtn);
        setDeselectedMargins(colorbtn);
        setDeselectedMargins(facebtn);
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        m_syn.SpeakToAudio("Describe scene");
    }

    public void ocr(View view) {
        cancelAsyncTasks();
        state = 1;
        setSelectedMargins(ocrbtn);
        setDeselectedMargins(scenedetectbtn);
        setDeselectedMargins(handbtn);
        setDeselectedMargins(colorbtn);
        setDeselectedMargins(facebtn);
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        m_syn.SpeakToAudio("OCR Mode");
    }

    public void handwritingrecognition(View view) {
        cancelAsyncTasks();
        state = 2;
        setSelectedMargins(handbtn);
        setDeselectedMargins(ocrbtn);
        setDeselectedMargins(scenedetectbtn);
        setDeselectedMargins(colorbtn);
        setDeselectedMargins(facebtn);
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        m_syn.SpeakToAudio("Recognize Handwritten Text");
    }

    public void facedetection(View view) {
        cancelAsyncTasks();
        state = 3;
        setSelectedMargins(facebtn);
        setDeselectedMargins(ocrbtn);
        setDeselectedMargins(handbtn);
        setDeselectedMargins(colorbtn);
        setDeselectedMargins(scenedetectbtn);
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        m_syn.SpeakToAudio("Describe faces");
    }

    public void colordetection(View view) {
        cancelAsyncTasks();
        state = 4;
        setSelectedMargins(colorbtn);
        setDeselectedMargins(ocrbtn);
        setDeselectedMargins(handbtn);
        setDeselectedMargins(scenedetectbtn);
        setDeselectedMargins(facebtn);
        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);
        m_syn.SpeakToAudio("Detect colors");
    }

    private class DetectionTask extends AsyncTask<InputStream, String, com.microsoft.projectoxford.face.contract.Face[]> {
        private boolean mSucceed = true;

        @Override
        protected com.microsoft.projectoxford.face.contract.Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westeurope.api.cognitive.microsoft.com/face/v1.0", "7505e0e2ce1e47bdac5ac3999f4768d4");
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.FacialHair,
                                FaceServiceClient.FaceAttributeType.Emotion,
                                FaceServiceClient.FaceAttributeType.HeadPose,
                                FaceServiceClient.FaceAttributeType.Accessories,
                                FaceServiceClient.FaceAttributeType.Blur,
                                FaceServiceClient.FaceAttributeType.Exposure,
                                FaceServiceClient.FaceAttributeType.Hair,
                                FaceServiceClient.FaceAttributeType.Makeup,
                                FaceServiceClient.FaceAttributeType.Noise,
                                FaceServiceClient.FaceAttributeType.Occlusion
                        });
            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                Log.e("faceapi", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(Face[] result) {
            if (mSucceed) {
                //addLog("Response: Success. Detected " + (result == null ? 0 : result.length)
                //        + " face(s) in " + mImageUri);
            }
           // List<Face> faces = new ArrayList<>();
            //faces = Arrays.asList(result);

            StringBuilder res = new StringBuilder();
            if(result==null)
            {
                res.append("I detected no faces in the given image. ");
            }
            else {
                 if (result.length == 1) {
                    res.append("I detected one person in the picture. ");
                } else {
                    res.append("I detected " + result.length + " people in the picture. ");
                }
                for(int i=0;i<result.length;i++)
                {
                    res.append("Number ");
                    res.append(i+1);
                    res.append(": A "+ Long.toString(Math.round(result[i].faceAttributes.age))+" year old ");
                    if(getHair(result[i].faceAttributes.hair).toLowerCase().equals("bald"))
                    {
                        res.append("bald "+result[i].faceAttributes.gender);
                        if(getFacialHair(result[i].faceAttributes.facialHair).equals("Yes"))
                        {
                            res.append(" with a beard ");
                        }
                        else if(getFacialHair(result[i].faceAttributes.facialHair).equals("No")&&result[i].faceAttributes.gender.equals("Male")){
                            //res.append(" with no facial hair ");
                        }
                    }
                    else{
                        res.append(result[i].faceAttributes.gender+" with ");
                        if(getHair(result[i].faceAttributes.hair).toLowerCase().equals("unknown")||getHair(result[i].faceAttributes.hair).toLowerCase().equals("other"))
                        {
                            res.append("some cool hair ");
                        }
                        else {
                            res.append(getHair(result[i].faceAttributes.hair)+" hair ");
                        }
                        if(getFacialHair(result[i].faceAttributes.facialHair).equals("Yes"))
                        {
                            res.append("and a beard ");
                        }
                        else if(getFacialHair(result[i].faceAttributes.facialHair).equals("No")&&result[i].faceAttributes.gender.equals("Male")){
                            //res.append("but no facial hair ");
                        }
                    }
                    res.append((result[i].faceAttributes.gender.toLowerCase().equals("male"))?". He":". She");
                    res.append(" is wearing ");
                    if(getAccessories(result[i].faceAttributes.accessories).equals("NoAccessories"))
                    {
                        res.append("no accessories ");
                    }
                    else {
                        String temp = getAccessories(result[i].faceAttributes.accessories);

                            temp = temp.replace("code9564", result[i].faceAttributes.glasses.toString());
                            res.append(temp+" ");

                    }
                    if(getEmotion(result[i].faceAttributes.emotion).equals("contempt")) {
                        res.append("and appears to be feeling contempt");
                    }
                    else {
                        res.append("and appears to be " + getEmotion(result[i].faceAttributes.emotion));
                    }
                    res.append("\n\n");
                }
            }
            if(calledForResult==true)
            {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", res.toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return;
            }
            if (m_syn == null) {
                // Create Text To Speech Synthesizer.
                m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
            }
            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
            Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
            m_syn.SetVoice(v, null);
            m_syn.SpeakToAudio(res.toString());
            // Show the result on screen when detection is done.
            //setUiAfterDetection(result, mSucceed);
            state3 = 0;
            isprocessing = false;
        }

        public String getHair(Hair hair) {
            if (hair.hairColor.length == 0)
            {
                if (hair.invisible)
                    return "Invisible";
                else
                    return "Bald";
            }
            else
            {
                int maxConfidenceIndex = 0;
                double maxConfidence = 0.0;

                for (int i = 0; i < hair.hairColor.length; ++i)
                {
                    if (hair.hairColor[i].confidence > maxConfidence)
                    {
                        maxConfidence = hair.hairColor[i].confidence;
                        maxConfidenceIndex = i;
                    }
                }

                return hair.hairColor[maxConfidenceIndex].color.toString();
            }
        }
        private String getAccessories(Accessory[] accessories) {
            StringBuilder sb = new StringBuilder();
            if (accessories.length == 0)
            {
                return "NoAccessories";
            }
            else
            {
                String[] accessoriesList = new String[accessories.length];
                for (int i = 0; i < accessories.length; ++i)
                {
                    accessoriesList[i] = accessories[i].type.toString();
                    if(i!=0&&i!=accessories.length-1)
                    {
                        sb.append(", ");
                    }
                    else if(i!=0&&i==accessories.length-1)
                    {
                        sb.append(" and ");
                    }
                    if(accessoriesList[i].equals("Glasses")) {
                        sb.append("code9564");
                    }
                    else {
                        sb.append(accessoriesList[i]);
                    }
                }

                return sb.toString();
            }
        }
        private String getFacialHair(FacialHair facialHair) {
            return (facialHair.moustache + facialHair.beard + facialHair.sideburns > 0) ? "Yes" : "No";
        }
        private String getEmotion(Emotion emotion)
        {
            String emotionType = "";
            double emotionValue = 0.0;
            if (emotion.anger > emotionValue)
            {
                emotionValue = emotion.anger;
                emotionType = "angry";
            }
            if (emotion.contempt > emotionValue)
            {
                emotionValue = emotion.contempt;
                emotionType = "contempt";
            }
            if (emotion.disgust > emotionValue)
            {
                emotionValue = emotion.disgust;
                emotionType = "disgusted";
            }
            if (emotion.fear > emotionValue)
            {
                emotionValue = emotion.fear;
                emotionType = "afraid";
            }
            if (emotion.happiness > emotionValue)
            {
                emotionValue = emotion.happiness;
                emotionType = "happy";
            }
            if (emotion.neutral > emotionValue)
            {
                emotionValue = emotion.neutral;
                emotionType = "neutral";
            }
            if (emotion.sadness > emotionValue)
            {
                emotionValue = emotion.sadness;
                emotionType = "sad";
            }
            if (emotion.surprise > emotionValue)
            {
                emotionValue = emotion.surprise;
                emotionType = "surprised";
            }
            return emotionType;
        }
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;
        private WeakReference<blind_services> recognitionActivity;
        public doRequest(blind_services activity) {
            recognitionActivity = new WeakReference<blind_services>(activity);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                if(state==2) {
                    if (recognitionActivity.get() != null) {
                        return recognitionActivity.get().process();
                    }
                }
                else
                {
                    return process();
                }
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            Log.e("TAG", "onPostExecute was called");
            // Display based on error existence

           // mTextView.setText("");
            if(recognitionActivity.get()==null)
            {
                return;
            }
            if (e != null) {
                //mTextView.setText("Error: " + e.getMessage());
                Log.e("TAG", e.getMessage());
                Gson gson = new Gson();


                this.e = null;
            } else {
                Gson gson = new Gson();
                Log.e("TAG", "This much worked");
                switch (state) {

                    case 0: {
                        AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                        Log.e("TAG", "THIS ALSO WORKED");
                        for (Caption caption : result.description.captions) {
                            if(calledForResult==true)
                            {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("result", caption.text);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                                return;
                            }
                            if (m_syn == null) {
                                // Create Text To Speech Synthesizer.
                                m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                            }
                            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                            Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                            m_syn.SetVoice(v, null);
                            m_syn.SpeakToAudio(caption.text);
                        }
                        for (String tag : result.description.tags) {
                            //mTextView.append("Tag: " + tag + "\n");
                        }
                    }
                    break;
                    case 1: {
                        String result = "";
                        if (e != null) {
                            result = e.getMessage();
                            this.e = null;
                        }
                        else {
                            OCR r = gson.fromJson(data, OCR.class);
                            for (Region reg : r.regions) {
                                for (Line line : reg.lines) {
                                    for (Word word : line.words) {
                                        result += word.text + " ";
                                    }
                                }
                                result += "\n\n";
                            }
                            if(calledForResult==true)
                            {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("result", result);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                                return;
                            }
                            if (m_syn == null) {
                                // Create Text To Speech Synthesizer.
                                m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                            }
                            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                            Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                            m_syn.SetVoice(v, null);
                            m_syn.SpeakToAudio(result);
                        }
                    }
                    break;
                    case 2:{
                        HandwritingRecognitionOperationResult r = gson.fromJson(data, HandwritingRecognitionOperationResult.class);

                        StringBuilder resultBuilder = new StringBuilder();
                        //if recognition result status is failed. display failed
                        if (r.getStatus().equals("Failed")) {
                                resultBuilder.append("Error: Recognition Failed");
                        } else {
                            for (HandwritingTextLine line : r.getRecognitionResult().getLines()) {
                                for (HandwritingTextWord word : line.getWords()) {
                                    resultBuilder.append(word.getText() + " ");
                                }
                                //resultBuilder.append("\n");
                            }
                            resultBuilder.append("\n");
                        }
                        if(calledForResult==true)
                        {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("result", resultBuilder.toString());
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                            return;
                        }
                        if (m_syn == null) {
                            // Create Text To Speech Synthesizer.
                            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                        }
                        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                        m_syn.SetVoice(v, null);
                        m_syn.SpeakToAudio(resultBuilder.toString());
                    }
                    break;
                    case 4:{
                        AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                        String res = "The primary color appears to be " + result.color.dominantColorForeground;
                        if(calledForResult==true)
                        {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("result", res);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                            return;
                        }
                        if (m_syn == null) {
                            // Create Text To Speech Synthesizer.
                            m_syn = new Synthesizer("be588fee9cf045b58e72ff8727efde04");
                        }
                        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
                        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, Guy24kRUS)", Voice.Gender.Female, true);
                        m_syn.SetVoice(v, null);
                        m_syn.SpeakToAudio(res);
                    }
                }
            }

           // mButtonSelectImage.setEnabled(true);
            state3=0;
            isprocessing = false;
        }
    }
}
