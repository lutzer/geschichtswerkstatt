package com.drl.brandis.geschichtswerkstatt.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.drl.brandis.geschichtswerkstatt.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends BaseActivity implements ZXingScannerView.ResultHandler {

    private static final String LOG_TAG = "ScannerActivity";

    private ZXingScannerView scannerView;

    private ScannerState state = ScannerState.SCANNING;

    TextView textView;
    String linkUrl;

    public enum ScannerState {
        SCANNING, FOUND
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
        }

        textView = (TextView) findViewById(R.id.text_view);

        initScannerView();

        updateUi();
    }

    public void updateUi() {
        updateUi("");
    }

    public void updateUi(String text) {
        if (state == ScannerState.FOUND) {
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            findViewById(R.id.button_layout).setVisibility(View.GONE);
            textView.setText("Zeige mit der Camera auf den QR Code");
        }
    }

    public void initScannerView() {
        if (scannerView != null)
            scannerView.stopCamera();

        // scan formats
        BarcodeFormat formats[] = { BarcodeFormat.QR_CODE };

        scannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        scannerView.setFormats(Arrays.asList(formats));

        // place in layout
        FrameLayout scannerFrame = (FrameLayout) findViewById(R.id.scanner_frame);
        scannerFrame.addView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();

        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        Log.v(LOG_TAG, result.getText()); // Prints scan results
        Log.v(LOG_TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        state = ScannerState.FOUND;

        linkUrl = result.getText();

        updateUi("QR-Code: "+linkUrl);
    }

    public void onScanButtonClicked(View view) {

        state = ScannerState.SCANNING;

        scannerView.startCamera();

        updateUi();

    }

    public void onLinkButtonClicked(View view) {


        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
            showAlert("QR Code","Der QR Code beinhaltet keine Url");
            return;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
        startActivity(browserIntent);

        updateUi();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initScannerView();
                } else {
                    showAlert("Error","This app requires to take camera pictures", true);
                }
                return;
            }
        }
    }
}