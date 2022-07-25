package com.example.filemanagers;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class AsyncTaskRunner extends AsyncTask<String, String, String> {

    public AsyncTaskRunner() {
        super();
        Log.e(Constant.TAG, "AsyncTaskRunner: " );
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(Constant.TAG, "onPreExecute: " );
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

//        getSupportActionBar().setTitle(getResources().getString(R.string.file_copy));
//        Log.e(TAG, "then: file copy" );
//        showFileAndFolder(destinationFile, true);
//        binding.copyToolBarBottom.setVisibility(View.GONE);
//        Log.e(Constant.TAG, "onPostExecute: " );
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        Log.e(Constant.TAG, "onProgressUpdate: "+ Arrays.toString(values));
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        Log.e(Constant.TAG, "onCancelled: "+s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.e(Constant.TAG, "onCancelled: " );
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.e(Constant.TAG, "doInBackground1: " );
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            try {
                Log.e(Constant.TAG, "doInBackground2: " );
                Files.copy(Paths.get(strings[0]), Paths.get(strings[1]), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Log.e(Constant.TAG, "doInBackground: ",e);
                //e.printStackTrace();
            }
        }

        return null;
    }
}
