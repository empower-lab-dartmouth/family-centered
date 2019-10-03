package com.example.myfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class HardcodedExample extends BasicFunctionality{

    ImageView picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hardcoded_example);

        picture = (ImageView)findViewById(R.id.hardcodedImage);

        picture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(HardcodedExample.this, "You clicked on ImageView", Toast.LENGTH_LONG).show();

            }
        });
    }
}
