package net.marcarni.easycheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import net.marcarni.easycheck.settings.SettingsActivity;

import java.io.IOException;

public class QRScanner extends AppCompatActivity {

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;
    private BarcodeDetector mBarcodeDetector;
    private TextView mCameraStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mCameraView = (SurfaceView) findViewById(R.id.camera);

        mBarcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE | Barcode.EAN_13 | Barcode.EAN_8).build();

        if (!mBarcodeDetector.isOperational()) {
            mCameraStatusTextView = (TextView) findViewById(R.id.camera_status);
            mCameraStatusTextView.setText(getString(R.string.detector_no_operational));
            mCameraStatusTextView.setVisibility(View.VISIBLE);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoFocus = sharedPreferences.getBoolean(getString(R.string.pref_autofocus_key), getResources().getBoolean(R.bool.pref_autofocus_default));

        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector).setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1600, 1024).setAutoFocusEnabled(autoFocus).build();

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCameraSource.start(mCameraView.getHolder());
                } catch (SecurityException se) {
                    Log.e("CAMERA PERMISSION", se.getMessage());
                } catch (IOException ioe) {
                    Log.e("CAMERA SOURCE", ioe.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    Intent intent = new Intent(QRScanner.this, DetailActivity.class);
                    intent.putExtra(getString(R.string.scanner_result), barcodes.valueAt(0).displayValue);
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraSource.release();
        mBarcodeDetector.release();
    }

}
