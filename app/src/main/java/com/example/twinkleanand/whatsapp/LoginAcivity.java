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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginAcivity extends AppCompatActivity {
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private ProgressBar mprogressbar;
    //Email Validation====================>>
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private DatabaseReference mDataBaseRef;
    private Button mbutton;

    //FireBase Auth======================>>
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataBaseRef = FirebaseDatabase.getInstance().getReference();
        mEmail =(TextInputLayout)findViewById(R.id.login_displayName);
        mPassword    =(TextInputLayout)findViewById(R.id.login_password);
        mbutton = (Button) findViewById(R.id.login_button);
        mprogressbar= (ProgressBar)findViewById(R.id.progressbar);
        //ToolBar
        Toolbar mtoolbar = (Toolbar) findViewById(R.id.login_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("LetsChat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        mbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                hideKeyboard();
                mbutton.setEnabled(false);
                String email= mEmail.getEditText().getText().toString();
                String password     = mPassword.getEditText().getText().toString();
                if(!validateEmail(email))
                {
                    mEmail.setError("Enter valid Email");
                }

                else if (!validatePassword(password)) {
                    mPassword.setError("Not a valid password!");
                }
                else {
                    mEmail.setErrorEnabled(false);
                    mPassword.setErrorEnabled(false);
                    mprogressbar.setVisibility(View.VISIBLE);
                    login_User(email,password);

                }


            }
        });
    }
    public void login_User(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)

                .addOnCompleteListener(LoginAcivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mprogressbar.setVisibility(View.GONE);

                            FirebaseUser user = mAuth.getCurrentUser();
                            String token      = FirebaseInstanceId.getInstance().getToken();
                            mDataBaseRef.child("Users").child(user.getUid()).child("device_token")
                                    .setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainIntent = new Intent(LoginAcivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(mainIntent);
                                    finish();
                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user
                            //Better to use dismiss otherwise it may cause window leak error if the activity is finish()
                            mprogressbar.setVisibility(View.GONE);
                            String TAG = "FIREBASE_EXCEPTION";
                            FirebaseException e = (FirebaseException)task.getException();
                            Log.d(TAG, "Reason: " +  e.getMessage());
                            Toast.makeText(LoginAcivity.this, "Authentication failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            mbutton.setEnabled(true);
                            // ...
                        }
                    }
                });
        return;
    }

    //Hiding Virtual keyboard as android doesn't hide keyboard own its own================>>
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //Password Validation ============================>>
    private boolean validatePassword(String password) {
        return password.length() > 5;
    }

    //Email Validation ===============================>>
    private boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

}

