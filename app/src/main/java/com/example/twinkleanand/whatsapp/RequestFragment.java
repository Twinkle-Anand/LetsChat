package com.example.twinkleanand.whatsapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mReqList;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_UserId;

    private View mMainView;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mReqList = (RecyclerView) mMainView.findViewById(R.id.request_recyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_UserId = mAuth.getCurrentUser().getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(mCurrent_UserId);
        mDatabaseRef.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();
//        DatabaseReference databaseReference = mDatabaseRef.equalTo("received","request_type").getRef();
        FirebaseRecyclerAdapter<Friend_Request, RequestFragment.RequestViewHolder> firebaseRequestAdapter = new FirebaseRecyclerAdapter<Friend_Request, RequestFragment.RequestViewHolder>(
                Friend_Request.class,
                R.layout.listusers,
                RequestFragment.RequestViewHolder.class,
                mDatabaseRef

        ) {
            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Friend_Request model, int position) {
                String request_type = model.getRequest_type();
                final String key = getRef(position).getKey();
               // Log.i("KEY+++++++",request_type+" "+key);
                if (request_type.equals("received")) {
                    mUsersDatabase.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumbNail = dataSnapshot.child("thumbnail").getValue().toString();


                            if (dataSnapshot.hasChild("online")) {
                                String val = dataSnapshot.child("online").getValue().toString();
                                if (val == "true")
                                    viewHolder.set_onlineStatus(true);
                            }

                            viewHolder.set_Name(userName);
                            viewHolder.set_ThumbImage(userThumbNail, getContext());
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("userId", key);
                                    startActivity(profileIntent);

                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        };
        mReqList.setAdapter(firebaseRequestAdapter);
    }
    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void set_Name(String name){
            TextView mtextView = (TextView)mView.findViewById(R.id.usersSingle_displayName);
            mtextView.setText(name);
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
