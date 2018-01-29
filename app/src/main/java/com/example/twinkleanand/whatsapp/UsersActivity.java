package com.example.twinkleanand.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
     private RecyclerView mRecyclerView;
     private Toolbar mToolBar;
     private DatabaseReference mDatabaseRef;
     private FirebaseAuth mAuth;

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolBar =(Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth       =FirebaseAuth.getInstance();
        mRecyclerView = (RecyclerView)findViewById(R.id.users_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> fireBaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.listusers,
                UsersViewHolder.class,
                mDatabaseRef


        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, final int position) {
                viewHolder.set_Name(model.getName());
                viewHolder.set_Status(model.getStatus());
                viewHolder.set_ThumbImage(model.getThumb_nail(),getApplicationContext());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String id = getRef(position).getKey();
                        Intent intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        intent.putExtra("userId",id);
                        startActivity(intent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(fireBaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void set_Name(String name){
            TextView mtextView = (TextView)mView.findViewById(R.id.usersSingle_displayName);
            mtextView.setText(name);
        }

        public void set_Status(String status){
            TextView mtextView =(TextView)mView.findViewById(R.id.usersSingle_status);
            mtextView.setText(status);
        }

        public void set_ThumbImage(String string, Context context){
            CircleImageView mImage = (CircleImageView)mView.findViewById(R.id.usersSingle_image);
            Picasso.with(context).load(string).placeholder(R.drawable.people).into(mImage);
        }
    }
}
