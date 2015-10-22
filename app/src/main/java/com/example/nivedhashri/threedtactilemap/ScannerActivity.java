package com.example.nivedhashri.threedtactilemap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * Created by Nivedha Shri on 06-Oct-15.
 */
public class ScannerActivity extends ActionBarActivity {

    private android.hardware.Camera mCamera;
    private CameraActivity mPreview;
    private Handler autoFocusHandler;
     public static String scanResult;

    private Button scanButton;
    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        initControls();
    }

    private void initControls(){

        //to set the orientation of the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //method for auto focusing
        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraActivity(ScannerActivity.this, mCamera, previewCb, autoFocusCb);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanButton = (Button)findViewById(R.id.btnScanBarcode);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeScanned) {
                    //used to scan the barcode if it is not scanned when the button is clicked
                    barcodeScanned = false;
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCb);
                }
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // SCAdminTapToScanScreen.isFromAssetDetail = false;
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }

    /** A safe way to get an instance of the Camera object. */
    public static android.hardware.Camera getCameraInstance() {
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCb);
        }
    };

    android.hardware.Camera.PreviewCallback previewCb = new android.hardware.Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {

            android.hardware.Camera.Parameters parameters = camera.getParameters();
            android.hardware.Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {

                    Log.i("<<<<<<Asset Code>>>>> ","<<<<Bar Code>>> " + sym.getData());
                    scanResult = sym.getData().trim();
                    Toast.makeText(ScannerActivity.this, scanResult,Toast.LENGTH_SHORT).show();
                    barcodeScanned = true;
                    ProgressDialog progress = new ProgressDialog(ScannerActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Wait while loading...");
                    progress.show();
                    // To dismiss the dialog
                    Intent i = new Intent(ScannerActivity.this,MapsActivity.class);
                    startActivity(i);
                    progress.dismiss();
                    break;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    android.hardware.Camera.AutoFocusCallback autoFocusCb = new android.hardware.Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, android.hardware.Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
