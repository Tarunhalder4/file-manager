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

    public void setSortAscending(boolean ascending){
        editor.putBoolean(Constant.ASCENDING_ORDER_STRING, ascending);
        editor.commit();
    }

    public boolean getSortAscending(){
        return sharedPreferences.getBoolean(Constant.ASCENDING_ORDER_STRING, false);
    }

    public void setSortId(int id){
        editor.putInt(Constant.SORT_ID,id);
        editor.commit();
    }

    public int getSortId(){
        return sharedPreferences.getInt(Constant.SORT_ID,0);
    }

    public void setShowFileSize(boolean booleans){
        editor.putBoolean(Constant.SHOW_FILE_SIZE,booleans);
        editor.commit();
    }

    public boolean getShowFileSize(){
        return sharedPreferences.getBoolean(Constant.SHOW_FILE_SIZE,false);
    }

    public void setShowFolderSize(boolean booleans){
        editor.putBoolean(Constant.SHOW_FOLDER_SIZE,booleans);
        editor.commit();
    }

    public boolean getShowFolderSize(){
        return sharedPreferences.getBoolean(Constant.SHOW_FOLDER_SIZE,false);
    }

    public void setShowHiddenFileAndFolder(boolean booleans){
        editor.putBoolean(Constant.SHOW_HIDDEN_FILE_AND_FOLDER,booleans);
        editor.commit();
    }

    public boolean getShowHiddenFileAndFolder(){
        return sharedPreferences.getBoolean(Constant.SHOW_HIDDEN_FILE_AND_FOLDER,false);
    }

    public void setShowFullNameOfFile(boolean booleans){
        editor.putBoolean(Constant.SHOW_FULL_NAME_OF_FILE,booleans);
        editor.commit();
    }

    public boolean getShowFullNameOfFile(){
        return sharedPreferences.getBoolean(Constant.SHOW_FULL_NAME_OF_FILE,false);
    }

    public void setCompareType(String compareType){
        editor.putString(Constant.COMPARE_TYPE,compareType);
        editor.commit();
    }

    public String getCompareType(){
        return sharedPreferences.getString(Constant.COMPARE_TYPE,Constant.NAME_ASCENDING_ORDER);
    }
}
