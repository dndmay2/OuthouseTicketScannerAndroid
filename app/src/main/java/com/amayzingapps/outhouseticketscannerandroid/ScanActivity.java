package com.amayzingapps.outhouseticketscannerandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScanActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        List<String> barcodeFormats = Collections.singletonList("CODE_128");
        integrator.setDesiredBarcodeFormats(barcodeFormats);
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
        Intent returnIntent = new Intent();
        String ticketCode;
        if(scanningResult != null) {
            if(scanningResult.getContents() == null) {
                ticketCode = "Press Scan Ticket";
                toast = Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG);
            } else {
                ticketCode = scanningResult.getContents();
                toast = Toast.makeText(this, "Scanned: " + ticketCode, Toast.LENGTH_LONG);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            ticketCode = null;
            super.onActivityResult(requestCode, resultCode, intent);
        }
        returnIntent.putExtra("TICKET_CODE", ticketCode);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}

