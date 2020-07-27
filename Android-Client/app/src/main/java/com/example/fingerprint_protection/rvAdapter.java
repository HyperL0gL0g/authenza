package com.example.fingerprint_protection;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class rvAdapter extends RecyclerView.Adapter<rvAdapter.productViewHolder> {


    BiometricPrompt biometricPrompt;
    fingerPrint_Auth fingerPrintAuth;
    dbHelper dbHelper;
    SQLiteDatabase database;

    String token;


    private Context mctx;
    private List<detailsAdapter> productList;
    private  Activity activity;

    public rvAdapter(Context mCtx, List<detailsAdapter> productList,Activity activity) {
        this.mctx = mCtx;
        this.productList = productList;
        this.activity = activity;
        this.fingerPrintAuth = new fingerPrint_Auth(activity);
        dbHelper = new dbHelper(mctx);
        database= dbHelper.getWritableDatabase();

    }

    @NonNull
    @Override
    public productViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mctx);
        View view = layoutInflater.inflate(R.layout.list_layout, null);
        productViewHolder holder = new productViewHolder(view);

        return holder;


    }

    @Override
    public void onBindViewHolder(@NonNull productViewHolder productViewHolder, int i) {
        detailsAdapter product = productList.get(i);
        productViewHolder.title.setText(product.getTitle());
        productViewHolder.description.setText(product.getShortdesc());
        productViewHolder.imageView.setImageBitmap(product.getImage());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


    @Override
    public void onBindViewHolder(@NonNull final productViewHolder holder, final int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Cursor res = dbHelper.get_dataById(database,position+1);

                if(!(res.getCount()==0)) {
                    while (res.moveToNext()) {
                        token = res.getString(0);
                    }
                }
                final Executor executor = Executors.newSingleThreadExecutor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && token !=null) {
                    biometricPrompt = fingerPrintAuth.biometricInit(biometricPrompt, activity, executor,token);
                }

                if (Build.VERSION.SDK_INT >= 28) {
                    fingerPrintAuth.biometricListner(biometricPrompt, executor, activity);
                } else {
                    Intent a = new Intent(activity, oldSdkAuthActivity.class);
                    activity.startActivity(a);

                }
            }
        });


    }

    class productViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title, description;

        public productViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView2);
            title = itemView.findViewById(R.id.textView6);
            description = itemView.findViewById(R.id.textView8);

        }
    }


}




