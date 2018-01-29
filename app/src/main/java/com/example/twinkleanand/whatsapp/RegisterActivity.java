package com.example.twinkleanand.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mbutton;
    private Toolbar mtoolbar;
    private ProgressBar mprogressbar;
    //Email Validation====================>>
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    //Authentication Database Setup============>
    private DatabaseReference mdatabase;



    //FireBase Auth======================>>
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mDisplayName =(TextInputLayout)findViewById(R.id.reg_displayName);
        mEmail       =(TextInputLayout)findViewById(R.id.reg_email);
        mPassword    =(TextInputLayout)findViewById(R.id.reg_password);
        mbutton      =(Button)findViewById(R.id.reg_button);
        mprogressbar= (ProgressBar)findViewById(R.id.progressbar);
        //ToolBar
        mtoolbar = (Toolbar)findViewById(R.id.reg_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("WhatsApp");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //FireBase Auth
        mAuth = FirebaseAuth.getInstance();



        mbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                hideKeyboard();
                String display_name = mDisplayName.getEditText().getText().toString();
                String email        = mEmail.getEditText().getText().toString();
                String password     = mPassword.getEditText().getText().toString();
                if(display_name.length()== 0)
                {
                    mDisplayName.setError("Enter valid UserName");
                }
                else if (!validateEmail(email)) {
                    mEmail.setError("Not a valid email address!");
                }
                else if (!validatePassword(password)) {
                    mPassword.setError("Not a valid password!");
                }
                else {
                    mEmail.setErrorEnabled(false);
                    mDisplayName.setErrorEnabled(false);
                    mPassword.setErrorEnabled(false);
                    mprogressbar.setVisibility(View.VISIBLE);
                    register_User(display_name, email, password);

                }


            }
        });
    }
    public void register_User(final String name, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uId         = mUser.getUid();

                            mdatabase          = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
                            String token       = FirebaseInstanceId.getInstance().getToken();
                            HashMap<String,String> mhashmap = new HashMap<>();
                            mhashmap.put("name",name);
                            mhashmap.put("status","Hi there I am using letsChat");
                            mhashmap.put("image","default");
                            mhashmap.put("thumbnail","default");
                            mhashmap.put("device_token",token);
                            mdatabase.setValue(mhashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mprogressbar.setVisibility(View.GONE);
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                            // Sign in success, update UI with the signed-in user's information
//

                        } else {
                            // If sign in fails, display a message to the user
                            //Better to use dismiss otherwise it may cause window leak error if the activity is finish()
                            mprogressbar.setVisibility(View.GONE);
                            String TAG = "FIREBASE_EXCEPTION";
                            FirebaseException e = (FirebaseException)task.getException();
                            Log.d(TAG, "Reason: " +  e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Authentication failed" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            // ...
                        }
                    }
                });
    }

    //Hiding Virtual keyboard as android doesn't hide keyboard own its own================>>
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    //Email Validation ===============================>>
    private boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    //Password Validation ============================>>
    private boolean validatePassword(String password) {
        return password.length() > 5;
    }



}
