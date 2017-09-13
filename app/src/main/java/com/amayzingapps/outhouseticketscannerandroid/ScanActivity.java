package com.amayzingapps.outhouseticketscannerandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collection;

public class ScanActivity extends AppCompatActivity {
    Collection<String> scanTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Toast toast;
        if(scanningResult != null) {
            if(scanningResult.getContents() == null) {
                toast = Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG);
            } else {
                toast = Toast.makeText(this, "Scanned: " + scanningResult.getContents(), Toast.LENGTH_LONG);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
        finish();
    }
}

