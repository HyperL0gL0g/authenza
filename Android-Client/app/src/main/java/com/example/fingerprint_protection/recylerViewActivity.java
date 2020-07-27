package com.example.fingerprint_protection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class recylerViewActivity extends AppCompatActivity {
    Cursor res;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    static List<detailsAdapter> productList;
    Activity activity = this;
    TextView null_data;
    Context context = this;
    functions functions;
    dbHelper dbHelper;
    List<ArrayList<String>> data;

    BiometricPrompt biometricPrompt;
    fingerPrint_Auth fingerPrintAuth;

    String notificationData = null;
    static String fb_token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyler_view);

        productList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        data = new ArrayList<ArrayList<String>>();

        fab = findViewById(R.id.floatingActionButton);
        null_data = findViewById(R.id.null_data);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        functions = new functions(activity,context);

        if(!functions.isNetworkAvailable()) {
            Toast.makeText(activity, "Internet Not Available", Toast.LENGTH_SHORT).show();
            functions.showOKMessage("Internet Not Available");
        }


        if(functions.get_pref_data("fb_token").equals(functions.getFBToken())){
          fb_token = functions.get_pref_data("fb_token");
        }else{
            functions.save_pref_data("fb_token",functions.getFBToken());
            fb_token = functions.getFBToken();
        }


        notificationData = getIntent().getStringExtra("Nick");
        if(notificationData != null){
            final Executor executor = Executors.newSingleThreadExecutor();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && notificationData !=null) {
                fingerPrintAuth = new fingerPrint_Auth(activity);
                biometricPrompt = fingerPrintAuth.biometricInit(biometricPrompt, activity, executor,notificationData);
                fingerPrintAuth.biometricListner(biometricPrompt, executor, activity);

            }
        }

        if(!isStoragePermissionGranted())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        dbHelper = new dbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        res = dbHelper.get_allData(database);

        if(res.getCount()!=0){
            data = functions.cursorHelper(res,data);
            res.close();
            Log.d("arraylust",data.toString());
            for(int i=0;i<data.size();i++){
                try {
                    JSONObject reader = functions.DecodeJWT(data.get(i).get(1));
                    File imgFile = new File("/sdcard/Pictures/Authenza/organization_logs/"+reader.getString("client_id")+".png");
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    productList.add(new detailsAdapter(Integer.parseInt(data.get(i).get(0)), StringUtils.capitalize(data.get(i).get(2).toLowerCase().trim()),reader.getString("email"),myBitmap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else{
         //   Snackbar.make(activity.findViewById(android.R.id.content), "No Registration Found", Snackbar.LENGTH_LONG)
           //         .setAction("Action", null).show();
        }


        if(productList.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            null_data.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            null_data.setVisibility(View.GONE);
        }

        rvAdapter adapter = new rvAdapter(this, productList,activity);

        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(),scannerActivity.class);
                startActivity(a);
            }
        });

        if(!functions.checkFingerPrintAvailability()){
            if (Build.VERSION.SDK_INT >= 28) {
                new android.app.AlertDialog.Builder(recylerViewActivity.this)
                        .setTitle("No Fingerpint Available")
                        .setMessage("Add Fingerpint in settings")
                        .setCancelable(false)
                        .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                functions.androidPieSettings();
                            }
                        })
                        .create()
                        .show();

            } else {
                new android.app.AlertDialog.Builder(recylerViewActivity.this)
                        .setTitle("No Fingerpint Available")
                        .setMessage("Add Fingerpint in settings")
                        .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                functions.lowerSDKSettings();
                            }
                        })
                        .create()
                        .show();
            }
        }

        try {
            File directory = new File("/sdcard/Pictures/Authenza/organization_logs");
            File[] files = directory.listFiles();
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

    }
}

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }
}