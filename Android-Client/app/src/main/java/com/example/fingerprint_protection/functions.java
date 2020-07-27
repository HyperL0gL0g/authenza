package com.example.fingerprint_protection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class functions {

    Activity activity;
    Context context;

    public functions(Activity activity, Context context) {

        this.activity = activity;
        this.context = context;
    }

    public JSONObject DecodeJWT(String jwtToken) throws JSONException,IllegalArgumentException,ArrayIndexOutOfBoundsException{
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];
        String body = new String(Base64.decode(base64EncodedBody,Base64.DEFAULT));
        JSONObject reader = new JSONObject(body);
        return reader;
    }


    public void save_pref_data(String key, String value){
        Log.d("setting","value");
        SharedPreferences.Editor editor = activity.getSharedPreferences("fb_token", activity.MODE_PRIVATE).edit();
        editor.putString(key,value);
        editor.commit();
    }

    public String get_pref_data(String key){
        SharedPreferences prefs = activity.getSharedPreferences("fb_token", activity.MODE_PRIVATE);
        return prefs.getString(key,"No data");
    }

    public boolean key_exist_pref_data(String key){
        SharedPreferences prefs = activity.getSharedPreferences("fb_token", activity.MODE_PRIVATE);
        return prefs.contains(key);
    }


    public static void set_processDialog(ProgressDialog dialog, String Message){
        dialog.setMessage(Message);
        dialog.show();
       }

    public static void reset_progressDialog(ProgressDialog dialog){
        dialog.dismiss();
    }

    public boolean checkFingerPrintAvailability() {

        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);

        return fingerprintManager.hasEnrolledFingerprints();
    }


    public void lowerSDKSettings() {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        activity.startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    public void androidPieSettings() {
        Intent intent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
        activity.startActivity(intent);
    }


    public List<ArrayList<String>> cursorHelper(Cursor res, List<ArrayList<String>> data){
        String token = "TOKEN";
        if(res.getCount()==0){
            Toast.makeText(activity, "No data present in cursor helper", Toast.LENGTH_SHORT).show();;
        }else{
            while(res.moveToNext()){
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(res.getString(res.getColumnIndex("ID")));
                arrayList.add(res.getString(res.getColumnIndexOrThrow("TOKEN")));
                arrayList.add(res.getString(res.getColumnIndexOrThrow("ORG_NAME")));
                data.add(arrayList);
            }
        }

        return data;
    }

    public String getFBToken(){
        final String[] token = new String[1];
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("adad", "getInstanceId failed", task.getException());
                            return;
                        }
                        token[0] = task.getResult().getToken();
                        Log.d("key", token[0]);
                    }
                });

        return token[0];
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void saveImage(Bitmap data,String filename) {

//        if(data!=null){
//            data =  BitmapFactory.decodeResource(context.getResources(),R.drawable.defaultimage);
//        }
        File createFolder = new File("/sdcard/Pictures/Authenza");
        if(!createFolder.exists())
            createFolder.mkdir();
        File createFolder1 = new File("/sdcard/Pictures/Authenza/organization_logs");
        if(!createFolder1.exists())
            createFolder1.mkdir();

        File saveImage = new File(createFolder1,filename+".png");
        if(!saveImage.exists()) {
            try {
                saveImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("urlfile",saveImage.toString());

        try {
            if(data!=null){
                OutputStream outputStream = new FileOutputStream(saveImage);
                data.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            }else{
                //Bitmap bp = R.drawable.defaultimage
                //data = ((BitmapDrawable) R.drawable.defaultimage.getDrawable()).getBitmap();
                data =  BitmapFactory.decodeResource(context.getResources(),R.drawable.default123);
                if(data!=null){
                    Log.d("not null","not null");
                }
                else{
                    Log.d("nulllll","nullllllllll");
                }
                OutputStream outputStream = new FileOutputStream(saveImage);
                data.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showOKMessage(String message) {
        new android.app.AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }




}
