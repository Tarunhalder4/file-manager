package com.example.filemanagers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.PublicKey;

public class Constant {
    public static String PATH ="path";

    public static int PIE_CHAT=1;
    public static int CONTAINS_INTERNAL_STORAGE=2;
    public static int INTERNAL_STORAGE=3;
    public static int BOOKMARK=4;

    public static String PHOTO_FOLDER = "Photo";
    public static String DOWNLOAD_FOLDER = "Download";
    public static String SAFE_BOX_FOLDER = "SafeBox";
    public static String MUSIC_FOLDER = "Music";
    public static String RECENT_FILE = "RecentFile";
    public static String DOCUMENTS_FOLDER = "Documents";
    public static String APP_MANAGER_FOLDER = "AppManager";
    public static String VIDEO_FOLDER = "Video";
    public static String AAD_TO_QUICK_ACCESS = "Add to quick access";

    public static String DCIM_FOLDER = "DCIM";
    public static String MOVIES_FOLDER = "Video";
    public static String PICTURES_FOLDER = "Picture";

    public static String INTERNAL_STORAGE_PATH="internal_storage_path";
    public static String FOLDER_PATH="folder_path";

    public static String PHOTO_FILE ="photo";
    public static String PDF_FILE ="file";
    public static String AUDIO_FILE ="audio";
    public static String VIDEO_FILE = "video";

    public static void requestPermission(Activity activity){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(activity, "Storage permission are required", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions((Activity) activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},111);
    }

    public static boolean checkPermission(Context context){
        int writePermission = ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE);
        if (writePermission== PackageManager.PERMISSION_GRANTED && readPermission==PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }

    public static String memory(long memory){
        if (memory<=100000){
            memory = memory/1000;
            return String.valueOf(memory)+" KB";
        }else if(memory<=100000000){
            memory = memory/1000000;
            return String.valueOf(memory)+" MB";
        }else if(memory<=1000000000){
            memory = memory/1000000000;
            return String.valueOf(memory)+" GB";
        }
        return "";
    }


}
