package com.example.fingerprint_protection;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Response;

public class fingerPrint_Auth  {

    Activity activity;
    HttpManager httpManager = new HttpManager();
    functions functions;

    JSONObject reader;

    String token=null;
    ProgressDialog progressDialog;

    public fingerPrint_Auth(Activity activity) {
        this.activity = activity;
        this.functions = new functions(activity,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BiometricPrompt biometricInit(BiometricPrompt biometricPrompt,Activity a, Executor executor,String token){

        try {
           reader =  functions.DecodeJWT(token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.token = token;
        progressDialog = new ProgressDialog(activity);


        try {
            biometricPrompt = new BiometricPrompt.Builder(a)
                    .setTitle(reader.getString("name"))
                    .setDescription(reader.getString("email"))
                    .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return biometricPrompt;
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    public void biometricListner(BiometricPrompt biometricPrompt, Executor executor, final Activity activity){

        biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "AuthError", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                activity.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {

                               new confirm_login().execute("user/two_factor/confirm_login",token);
                               Log.d("tokenimp",token);

                           }
                       });
            }

        });
    }

    public void lowerSDKFingerPrintAuth(FingerprintManager fingerprintManager, final Activity activity){

        fingerprintManager.authenticate(null, new CancellationSignal(), 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "AuthError", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "AuthSuccess", Toast.LENGTH_SHORT).show();
                        }
                    });
                    TextView textView = (TextView) ((Activity)activity).findViewById(R.id.textView2);
                    ImageView imageView = (ImageView) ((Activity)activity).findViewById(R.id.imageView);
                    imageView.setImageResource(R.mipmap.check);
                    textView.setText("Successfull");
                    new confirm_login().execute("user/two_factor/confirm_login",token);
                }

            },null);
    }


    class confirm_login extends AsyncTask<String,Void, Response> {
        Response response1;
        String[][] data;
        @Override
        protected Response doInBackground(String... strings) {

           functions.set_processDialog(progressDialog,"Loading");
            data = new String[][] {{"token",strings[1]}};
            try {
                response1 = httpManager.send_postRequest("http://localhost/"+strings[0],data);
                Log.d("respnse1","wordking");
            } catch (IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Failed to connect, check Internet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return response1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Verifying");
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            if(response!=null){
                if(response.code()==200){
                    Toast.makeText(activity, "done", Toast.LENGTH_SHORT).show();
                    try {
                        a = response.body().string();
                        Log.d("response_body",a);
                        JSONObject reader = new JSONObject(a);
                        Toast.makeText(activity, String.valueOf(reader.get("message")), Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        String resp = response.body().string();
                        JSONObject reader = new JSONObject(resp);
                        Toast.makeText(activity, String.valueOf(reader.get("error")), Toast.LENGTH_SHORT).show();
                       // Snackbar.make(activity.findViewById(android.R.id.content), String.valueOf(reader.get("error")), Snackbar.LENGTH_LONG)
                         //       .setAction("Action", null).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                Log.d("respnse_code","error");
            }


        }
    }






}
