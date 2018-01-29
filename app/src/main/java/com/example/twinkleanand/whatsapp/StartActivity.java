package com.example.twinkleanand.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Twinkle Anand on 11/21/2017.
 */

public class StartActivity extends AppCompatActivity {

    private Button mregButton;
    private Button mloginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        mregButton = findViewById(R.id.signup);
        mregButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
        mloginButton=findViewById(R.id.login);
        mloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartActivity.this,LoginAcivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}
