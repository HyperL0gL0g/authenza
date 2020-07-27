package com.example.fingerprint_protection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;

public class oldSdkAuthActivity extends AppCompatActivity {

    oldSdkAuthActivity activity = this;

    fingerPrint_Auth fingerPrintAuth = new fingerPrint_Auth(activity);
    FingerprintManager fingerprintManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_sdk_auth);

        fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        fingerPrintAuth.lowerSDKFingerPrintAuth(fingerprintManager,activity);


    }
}
