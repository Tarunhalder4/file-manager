package com.example.filemanagers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePref {
    @SuppressLint("StaticFieldLeak")
    private static SharePref ourInstance = null;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static SharePref getInstance(Context  context){
        if (ourInstance==null){
            ourInstance = new SharePref(context);
        }
        return ourInstance;
    }

    private SharePref(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constant.MY_SHARE_PREF,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setSortId(int id){
        editor.putInt(Constant.SORT_ID,id);
        editor.commit();
    }

    public int getSortId(){
        return sharedPreferences.getInt(Constant.SORT_ID,0);
    }

}
