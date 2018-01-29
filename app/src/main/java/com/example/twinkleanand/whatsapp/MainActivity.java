package com.example.twinkleanand.whatsapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mtoolBar;
    private ViewPager mviewPager;
    private SectionPagerAdapter mviewAdapter;
    private TabLayout mtabLayout;
    private DatabaseReference mDatabaseref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseref = FirebaseDatabase.getInstance().getReference();

        //Support Toolbar
        mtoolBar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("LetsChat");

       mviewPager = (ViewPager) findViewById(R.id.main_viewpager);
       mviewAdapter = new SectionPagerAdapter(getSupportFragmentManager());
       mviewPager.setAdapter(mviewAdapter);
       mtabLayout = (TabLayout)findViewById(R.id.main_tablayout);
       mtabLayout.setupWithViewPager(mviewPager);

    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
           return_toStart();
        }

    }

    private void return_toStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout)
        {
            mDatabaseref.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            return_toStart();
        }
        else if(item.getItemId()==R.id.main_accountsetting)
        {
            Intent accountIntent = new Intent(MainActivity.this,AccountSettings.class);
            startActivity(accountIntent);
        }
        else if(item.getItemId() == R.id.main_users){
            Intent userIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(userIntent);
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null)
        mDatabaseref.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseref.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }
}
