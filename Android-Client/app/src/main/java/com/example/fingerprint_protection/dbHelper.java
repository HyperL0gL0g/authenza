package com.example.fingerprint_protection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class dbHelper extends SQLiteOpenHelper {

    private static final String DB_name = "auth.db";
    private static final String table_name = "token_table";
    private static final int version=1;


    public dbHelper(@Nullable Context context) {
        super(context, DB_name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql = "create table "+table_name+" ( ID INTEGER PRIMARY KEY AUTOINCREMENT , TOKEN TEXT,ORG_NAME TEXT)";
        sqLiteDatabase.execSQL(sql);
    }


    public boolean insert_data(String token,String org_name,SQLiteDatabase sqLiteDatabase){

        ContentValues contentValues = new ContentValues();
        contentValues.put("TOKEN",token);
        contentValues.put("ORG_NAME",org_name);
        long resutl = sqLiteDatabase.insert(table_name,null,contentValues);

        return resutl >= 1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists "+table_name);
        onCreate(sqLiteDatabase);
    }

    public Cursor get_allData(SQLiteDatabase sqLiteDatabase){
        Cursor res;
        res = sqLiteDatabase.rawQuery("select * from "+table_name,new String[]{});
        Log.d("dberror", String.valueOf(res.getCount()));
        return res;
    }

    public Cursor get_dataById(SQLiteDatabase sqLiteDatabase,int id){
        Cursor res = sqLiteDatabase.rawQuery("select TOKEN from "+table_name+" where ID="+id,new String[]{});
        return res;
    }

    public boolean dataExistsById(SQLiteDatabase sqLiteDatabase,String data){
        Cursor res;
         res = sqLiteDatabase.rawQuery("select * from "+table_name +" where TOKEN = '"+data+"'",new String[]{});
        if(res.getCount()<=0) {
            res.close();
            return false;
        }else {
            res.close();
            return true;
        }
    }


}
