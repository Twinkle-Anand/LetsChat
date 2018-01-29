package com.example.twinkleanand.whatsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {

    private TextInputLayout mInputLayout;
    private Button mChangeBtn;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar mtoolbar = (Toolbar)findViewById(R.id.statusactivity_toolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInputLayout = (TextInputLayout) findViewById(R.id.statusactivity_status);
        mChangeBtn     =(Button)findViewById(R.id.statusactivity_button);


        mUser          = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef   = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());

        String status = getIntent().getStringExtra("status");
        mInputLayout.getEditText().setText(status);

        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                final ProgressBar mprogress = (ProgressBar)findViewById(R.id.statusactivity_progress);
                mprogress.setVisibility(View.VISIBLE);

                String new_status = mInputLayout.getEditText().getText().toString();

                mDatabaseRef.child("status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mprogress.setVisibility(View.GONE);
                            Toast.makeText(StatusActivity.this,"Status Updated",Toast.LENGTH_SHORT).show();

                        }
                        else{
                            mprogress.setVisibility(View.GONE);
                            Toast.makeText(StatusActivity.this,"Error while updating the status",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }



    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseRef.child("online").setValue("true");
    }


    //Hiding Virtual keyboard as android doesn't hide keyboard own its own================>>
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
