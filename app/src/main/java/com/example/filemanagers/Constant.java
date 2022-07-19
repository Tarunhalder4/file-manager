package com.example.filemanagers;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Constant {
    public static String PATH ="path";

    public static final int PIE_CHAT=1;
    public static final int CONTAINS_INTERNAL_STORAGE=2;
    public static final int INTERNAL_STORAGE=3;
    public static final int BOOKMARK=4;

    public static final int SPLASH_SCREEN_TIME_OUT=2000;

    public static final int REQUEST_CODE = 111;

    public static final String PHOTO_FOLDER = "Photo";
    public static final String DOWNLOAD_FOLDER = "Download";
    public static final String SAFE_BOX_FOLDER = "SafeBox";
    public static final String MUSIC_FOLDER = "Music";
    public static final String RECENT_FILE = "RecentFile";
    public static final String DOCUMENTS_FOLDER = "Documents";
    public static final String APP_MANAGER_FOLDER = "AppManager";
    public static final String VIDEO_FOLDER = "Video";
    public static final String AAD_TO_QUICK_ACCESS = "Add to quick access";

    public static final String DCIM_FOLDER = "DCIM";
    public static final String MOVIES_FOLDER = "Video";
    public static final String PICTURES_FOLDER = "Picture";

    public static final String INTERNAL_STORAGE_PATH="internal_storage_path";
    public static final String FOLDER_PATH="folder_path";

    public static final String PHOTO_FILE ="photo";
    public static final String PDF_FILE ="pdf_file";
    public static final String AUDIO_FILE ="audio";
    public static final String VIDEO_FILE = "video";
    public static final String DOCUMENTS_FILE = "documents_file";
    public static final String ALL_FILE = "all_file";
    public static final String INTERNAL_STORAGE_FILE_FOLDER = "internal_storage_file_folder";

    public static final String MY_SHARE_PREF = "my_share_pref";

    public static boolean SHOW_FILE_SIZE = false;
    public static boolean SHOW_FOLDER_SIZE = false;
    public static boolean SHOW_FULL_NAME_OF_FILE = false;
    public static boolean SHOW_HIDDEN_FILE_AND_FOLDER = false;

    /////sorting Constant
    public static final String SORT_ID = "sort_id";
    public static final int ID_SORT_NAME = 2131231261;
    public static final int ID_SORT_DATE = 2131231260;
    public static final int ID_SORT_SIZE = 2131231263;
    public static final int ID_SORT_TYPE = 2131231264;

    public static boolean ASCENDING_ORDER =false;
    //public static final int DESCENDING_ORDER =1;

    public static final int NAME_ASCENDING_ORDER =0;
    public static final int NAME_DESCENDING_ORDER =1;
    public static final int DATE_ASCENDING_ORDER =2;
    public static final int DATE_DESCENDING_ORDER =3;
    public static final int SIZE_ASCENDING_ORDER =4;
    public static final int SIZE_DESCENDING_ORDER =5;
    public static final int TYPE_ASCENDING_ORDER =6;
    public static final int TYPE_DESCENDING_ORDER =7;

    public static int checkedId = 0;

    public static boolean LOG_CLICK_ACTIVATED = false;

    ///////////For drawable background
    public static final int WITH_BACK_GROUND_VIEW =0;
    public static final int WITHOUT_BACK_GROUND_VIEW =1;

    public static final String Home ="home";
    public static final String ADD_CLOUD_STORAGE ="add cloud storage";
    public static final String FTP_SERVER ="ftp server";
    public static final String LAN ="lan";
    public static final String TRASH  ="trash";
    public static final String CONNECT_WITH_US ="connect with us";
    public static final String COMPRESS_FOLDER ="compress folder";
    public static final String APK = "apk";
    public static final String INFORMATION ="information";


    public static boolean checkPermission(Context context) {
            int write = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 333);
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
