package com.example.filemanagers;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity{

    public  Context context;
    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        context = this;

        button = findViewById(R.id.bottom_sheet);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  BottomSheet bottomSheet = new BottomSheet();
              //  bottomSheet.show(getSupportFragmentManager(),"bottom sheet");

            }
        });



    }


}
