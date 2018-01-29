package com.example.twinkleanand.whatsapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple Fragment subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserDatabaseRef;
    String mCurrent_UserId;
    private FirebaseAuth mAuth;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View MainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mAuth= FirebaseAuth.getInstance();
        mCurrent_UserId = mAuth.getCurrentUser().getUid();


        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_UserId);
        mDatabaseRef.keepSynced(true);
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabaseRef.keepSynced(true);

        mRecyclerView = (RecyclerView) MainView.findViewById(R.id.friends_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return MainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
             Friends.class,
              R.layout.listusers,
                FriendsViewHolder.class,
                mDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                  viewHolder.set_Date(model.getDate());
                  final String list_user_id = getRef(position).getKey();
                  Log.i("INFORMATION",list_user_id);
                  mUserDatabaseRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                          final String userName = dataSnapshot.child("name").getValue().toString();
                          String userThumbNail = dataSnapshot.child("thumbnail").getValue().toString();


                          if(dataSnapshot.hasChild("online"))
                          {
                              String val = dataSnapshot.child("online").getValue().toString();
                          }

                          viewHolder.set_Name(userName);
                          viewHolder.set_ThumbImage(userThumbNail,getContext());
                          viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View view) {

                                  CharSequence[] charSequences = new CharSequence[]{"Open Profile","Send Message"};
                                  AlertDialog.Builder  builder = new AlertDialog.Builder(getActivity());
                                  builder.setTitle("Choose among two");
                                  builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialogInterface, int i) {
                                                 if(i==0)
                                                 {
                                                     Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                     profileIntent.putExtra("userId",list_user_id);
                                                     startActivity(profileIntent);
                                                 }
                                                 if(i==1)
                                                 {
                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    chatIntent.putExtra("userId",list_user_id);
                                                    chatIntent.putExtra("userName",userName);
                                                    startActivity(chatIntent);
                                                 }
                                              }
                                          });
                                builder.show();
                              }
                          });


                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });

            }
        };
        mRecyclerView.setAdapter(friendsRecyclerViewAdapter);
    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void set_Name(String name){
            TextView mtextView = (TextView)mView.findViewById(R.id.usersSingle_displayName);
            mtextView.setText(name);
        }

        public void set_Date(String date ){
            TextView mtextView =(TextView)mView.findViewById(R.id.usersSingle_status);
            mtextView.setText(date);
        }

        public void set_ThumbImage(String string, Context context){
            CircleImageView mImage = (CircleImageView)mView.findViewById(R.id.usersSingle_image);
            Picasso.with(context).load(string).placeholder(R.drawable.people).into(mImage);
        }

        public void set_onlineStatus(Boolean val){
            ImageView mImageView =(ImageView)mView.findViewById(R.id.onlineIcon);
            if(val.equals(true))
            {

                mImageView.setVisibility(View.VISIBLE);
            }
            else{
                mImageView.setVisibility(View.INVISIBLE);

            }
        }
    }
}



