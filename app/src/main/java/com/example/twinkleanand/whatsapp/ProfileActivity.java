package com.example.twinkleanand.whatsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileStatus,profileDisplayName,profileFriendsList;
    private ImageView mProfileImage;
    private Button mFriendsRequestBtn;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mRootRef;
    private DatabaseReference mFriendsReqDatabaseRef;
    private DatabaseReference mFriendsDatabaseRef;
    private DatabaseReference mNotificationRef;
    private ProgressBar mProgressBar;
    private FirebaseUser mCurrentUser;
    private String mRequestStatus;
    private Button mDeclineReq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
       //ID OF THE CURRENT USER
        final String id = getIntent().getStringExtra("userId");
        //Getting the reference to all views
        mRootRef    = FirebaseDatabase.getInstance().getReference();
        mNotificationRef  =FirebaseDatabase.getInstance().getReference().child("Notifications");
        mFriendsReqDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        mDatabaseRef.keepSynced(true);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRequestStatus="not_friends";

        mProgressBar =(ProgressBar)findViewById(R.id.profile_progress);
        profileStatus =(TextView)findViewById(R.id.profile_Status);
        profileDisplayName=(TextView)findViewById(R.id.profile_displayName);
        profileFriendsList=(TextView)findViewById(R.id.profile_Friends);
        mFriendsRequestBtn   =(Button)findViewById(R.id.profile_RequestFriendsButton);
        mProfileImage        =(ImageView)findViewById(R.id.profile_ImageView);
        mDeclineReq          =(Button)findViewById(R.id.profile_declineReq);




       mDatabaseRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String name = dataSnapshot.child("name").getValue().toString();
               String status =dataSnapshot.child("status").getValue().toString();
               String image =dataSnapshot.child("image").getValue().toString();
               //==================>REQUEST FEATURE

               mFriendsReqDatabaseRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       mProgressBar.setVisibility(View.VISIBLE);
                       if(dataSnapshot.hasChild(id)){

                        String requestType = dataSnapshot.child(id).child("request_type").getValue().toString();
                        if(requestType.equals("received")){
                            mRequestStatus="req_received";
                            mFriendsRequestBtn.setText("ACCEPT FRIEND REQUEST");

                            mDeclineReq.setVisibility(View.VISIBLE);
                            mDeclineReq.setEnabled(true);
                        }
                        else if(requestType.equals("sent")){
                            mRequestStatus="req_sent";
                            mFriendsRequestBtn.setText("CANCEL FRIEND REQUEST");
                            mDeclineReq.setVisibility(View.INVISIBLE);
                            mDeclineReq.setEnabled(false);

                        }
                        mProgressBar.setVisibility(View.GONE);

                       }
                      else{
                           mFriendsDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Friends");
                                   mFriendsDatabaseRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if(dataSnapshot.hasChild(id)){
                                       mRequestStatus="friends";
                                       mFriendsRequestBtn.setEnabled(true);
                                       mFriendsRequestBtn.setText("UNFRIEND");
                                       mProgressBar.setVisibility(View.GONE);
                                       mDeclineReq.setVisibility(View.INVISIBLE);
                                       mDeclineReq.setEnabled(false);
                                   }
                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {
                                   mProgressBar.setVisibility(View.GONE);
                               }
                           });
                                   mProgressBar.setVisibility(View.GONE);
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {
                       mProgressBar.setVisibility(View.GONE);
                   }
               });

               profileStatus.setText(status);
               profileDisplayName.setText(name);
               Picasso.with(ProfileActivity.this).load(image).fit().placeholder(R.drawable.people).into(mProfileImage);
               mProgressBar.setVisibility(View.GONE);

           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
               mProgressBar.setVisibility(View.GONE);
           }
       });

       mFriendsRequestBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {


               mFriendsRequestBtn.setEnabled(false);
               //==============================>SEND FRIEND REQUEST
               if (mRequestStatus == "not_friends") {

                   mFriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Request");

                   String newNotificationId = mNotificationRef.child(id).push().getKey();
                   HashMap<String,String> notificationData = new HashMap<>();
                   notificationData.put("from",mCurrentUser.getUid());
                   notificationData.put("type","request");

                   Map requestMap = new HashMap();
                   requestMap.put("Friend_Request/"+mCurrentUser.getUid()+"/"+id+"/request_type","sent");
                   requestMap.put("Friend_Request/"+id+"/"+mCurrentUser.getUid()+"/request_type","received");
                   requestMap.put("Notifications/"+id+"/"+newNotificationId,notificationData);

                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                           if(databaseError!=null)
                           {
                               Toast.makeText(ProfileActivity.this,"There is some error while sending request",Toast.LENGTH_SHORT).show();
                           }
                           else {
                               mRequestStatus = "req_sent";
                               mFriendsRequestBtn.setText("CANCEL FRIEND REQUEST");
                               mDeclineReq.setVisibility(View.INVISIBLE);
                               mDeclineReq.setEnabled(false);
                           }
                           mFriendsRequestBtn.setEnabled(true);
                       }
                   });
               }



               //=====================================>CANCEL FRIEND REQUEST
               else if(mRequestStatus=="req_sent"){
                   mFriendsReqDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Friend_Request");
                   mFriendsReqDatabaseRef.child(mCurrentUser.getUid()).child(id)
                           .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           mFriendsReqDatabaseRef.child(id).child(mCurrentUser.getUid())
                                   .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   mFriendsRequestBtn.setEnabled(true);
                                   mRequestStatus="not_friends";
                                   mFriendsRequestBtn.setText("SEND FRIEND REQUEST");
                                   mDeclineReq.setVisibility(View.INVISIBLE);
                                   mDeclineReq.setEnabled(false);
                               }
                           });
                       }
                   });

               }

               //=================================================>>ACCEPT FRIEND REQUEST
              else if(mRequestStatus.equals("req_received")){
                   final String date = DateFormat.getDateTimeInstance().format(new Date());
                   Map acceptRequest = new HashMap();
                   acceptRequest.put("Friends/" + id + "/" + mCurrentUser.getUid() + "/date" ,date);
                   acceptRequest.put("Friends/"+ mCurrentUser.getUid() + "/" + id + "/date",date);
                   acceptRequest.put("Friend_Request/" + mCurrentUser.getUid() + "/" + id,null);
                   acceptRequest.put("Friend_Request/" + id + "/" + mCurrentUser.getUid(),null);


                   mRootRef.updateChildren(acceptRequest, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){

                                mRequestStatus ="friends";
                                mFriendsRequestBtn.setText("UNFRIEND");
                                mFriendsRequestBtn.setEnabled(true);
                                mDeclineReq.setVisibility(View.INVISIBLE);
                                mDeclineReq.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                       }
                   });


               }

               //===============================UNFRIEND
              else if(mRequestStatus.equals("friends")){


                   mProgressBar.setVisibility(View.VISIBLE);

                   Map unfriendmap = new HashMap();
                   unfriendmap.put("Friends/" + mCurrentUser.getUid() + "/" + id ,null);
                   unfriendmap.put("Friends/" + id + "/" + mCurrentUser.getUid() ,null );

                   mRootRef.updateChildren(unfriendmap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if(databaseError == null) {
                               mRequestStatus = "not_friends";
                               mFriendsRequestBtn.setText("SEND FRIEND REQUEST");
                           }

                           mProgressBar.setVisibility(View.GONE);
                           mFriendsRequestBtn.setEnabled(true);
                       }
                   });



               }
           }
       });


       mDeclineReq.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               mDeclineReq.setEnabled(false);
               mFriendsRequestBtn.setEnabled(false);
               mFriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
               mFriendsReqDatabaseRef.child(mCurrentUser.getUid()).child(id).removeValue()
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       mFriendsReqDatabaseRef.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               mFriendsRequestBtn.setEnabled(true);
                               mRequestStatus="not_friends";
                               mFriendsRequestBtn.setText("SEND FRIEND REQUEST");
                               mDeclineReq.setVisibility(View.INVISIBLE);
                               mDeclineReq.setEnabled(false);


                           }
                       });
                   }
               });
           }
       });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue("true");
    }


}
