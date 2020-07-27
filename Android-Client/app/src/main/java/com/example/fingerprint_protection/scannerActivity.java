package com.example.fingerprint_protection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Response;

import static android.Manifest.permission.CAMERA;

public class scannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    ZXingScannerView scannerView;

    Context context = this;
    Activity activity = this;
    functions functions;

    dbHelper dbHelper;
    SQLiteDatabase database;

    HttpManager httpManager;
    ProgressDialog progressDialog;

    String client_id = null;
    String org_name = null;

    static String scanner_result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        functions = new functions(activity, context);
        progressDialog = new ProgressDialog(this);

        dbHelper = new dbHelper(this);
        database = dbHelper.getWritableDatabase();
        httpManager = new HttpManager();

        setContentView(R.layout.scannerview);
        scannerView = (ZXingScannerView) findViewById(R.id.scannerview);
        if (!checkPermission())
            requestPermission();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if ((progressDialog != null) && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            if (scannerView == null) {
                scannerView = (ZXingScannerView) findViewById(R.id.scannerview);
                setContentView(R.layout.scannerview);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);

                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(scannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }




    @Override
    public void handleResult(Result result) {
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        try {
            JSONObject reader = functions.DecodeJWT(result.getText());

            client_id = reader.getString("client_id");


            Log.d("urlclientid", client_id);


            if (!dbHelper.dataExistsById(database, result.getText())) {

//                File file = new File("/sdcard/Pictures/Authenza/organization_logs/" + client_id + ".png");
//                if (!file.exists())
//                    new donwload_image().execute(client_id);

                scanner_result = result.getText();
                client_id = reader.getString("client_id");
                Log.d("urlclientid", client_id);
                new getOrgName().execute();
                new donwload_image().execute(client_id);
                new confirm_reg().execute("user/two_factor/confirm_reg", result.getText());

            } else {
                Toast.makeText(context, "Already Registered", Toast.LENGTH_SHORT).show();
                Intent a = new Intent(getApplicationContext(), recylerViewActivity.class);
                a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(a);
            }
        } catch (JSONException | ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Toast.makeText(activity, "Not Valid QRCode", Toast.LENGTH_SHORT).show();
            Intent a = new Intent(getApplicationContext(), recylerViewActivity.class);
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(a);
            e.printStackTrace();
        }

    }

    class confirm_reg extends AsyncTask<String, Void, Response> {
        Response response1;
        String[][] data;

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Do not exit");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Response doInBackground(String... strings) {

            data = new String[][]{{"token", strings[1]}};
            try {
                response1 = httpManager.send_postRequest("http://localhost/" + strings[0], data);
                Log.d("respnse1", "wordking");
            } catch (IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Failed to connect, check Internet", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
            return response1;
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if ((progressDialog != null) && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            });

            String a;
            if (response != null) {
                if (response.code() == 200) {
                    dbHelper.insert_data(scanner_result, org_name, database);
                    try {
                        a = response.body().string();
                        JSONObject reader = new JSONObject(a);
                        Toast.makeText(scannerActivity.this, String.valueOf(reader.get("message")), Toast.LENGTH_SHORT).show();
                        Log.d("response_body", a);
                        Intent intent = new Intent(getApplicationContext(), recylerViewActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String resp = response.body().string();
                        JSONObject reader = new JSONObject(resp);
                        Toast.makeText(activity, String.valueOf(reader.get("error")), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), recylerViewActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d("is_Response_null", "response_is_null");
            }

        }
    }


    class donwload_image extends AsyncTask<String, Void, Bitmap> {

        Bitmap bitmap;
        InputStream in = null;

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL("http://localhost/static/logo/" + strings[0] + ".png");
                Log.d("url", url.toString());
                URLConnection urlConn = url.openConnection();
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConn;
                httpURLConnection.connect();
                Log.d("urlcode", String.valueOf(httpURLConnection.getResponseCode()));
                if (httpURLConnection.getResponseCode() == 200) {
                    in = httpURLConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    return bitmap;
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Cant find image", Toast.LENGTH_SHORT).show();
                            //bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultimage);

                        }
                    });
                    bitmap = null;
                }

            } catch (IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Failed to connect, check Internet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if(bitmap!=null){
                Log.d("bitmap not null","bitmap not null");
            }else{
                Log.d("bitmap null","bitmap null");
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap!=null){
                Log.d("bitmap not null","bitmap not null");
            }else{
                Log.d("bitmap null","bitmap null");
            }
            functions.saveImage(bitmap, client_id);
        }
    }


    class getOrgName extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            Response r;
            String result = null;
            try {
                r = httpManager.send_postRequest("http://localhost/org/listing", null);
                result = r.body().string();

            } catch (IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Failed to connect, check Internet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null){
               try {
                   Log.d("orgValue", s);
                   JSONObject reader = new JSONObject(s);
                   String getData = reader.getString(client_id).replace("\"", "").replace("[", "").replace("[", "");
                   String[] data = getData.split(",");
                   org_name = data[0];
                   Log.d("orgname", org_name);
               }catch (JSONException e){
                   e.printStackTrace();
               }
            }
        }
    }


}


