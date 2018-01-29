package com.example.twinkleanand.whatsapp;

import android.content.Context;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Twinkle Anand on 1/25/2018.
 */

public class ChatActivity extends AppCompatActivity {
    private String mUserId ;
    private String userName;
    private String mCurrentUser;
    private android.support.v7.widget.Toolbar mToolBar;
    private DatabaseReference mrootref;
    private FirebaseAuth mAuth;
    private CircleImageView mcircularImage;
    private TextView mDispalyName;
    private TextView mLastSeen;
    private ImageButton mSendBtn;
    private ImageButton mAddBtn;
    private EditText mMessage;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;
    private LinearLayoutManager mlinearLayoutManager;
    private List<Message> mMessageList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final int TOTAL_ITEMS_TO_LOAD=10;
    private int mCurrentPage=1;
    private Query msgQuery ;
    private ChildEventListener listener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        mUserId =getIntent().getStringExtra("userId");
        userName=getIntent().getStringExtra("userName");
        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);

        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        mrootref= FirebaseDatabase.getInstance().getReference();
        mAuth   = FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser().getUid().toString();


        /* INFLATE THE CUSTOM VIEW */
        LayoutInflater inflater = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_appbar, null);
        actionBar.setCustomView(v);


        /*GETTING REF TO CUSTOM VIEW's COMPONENTS*/
        mDispalyName = (TextView)findViewById(R.id.Display_Name);
        mLastSeen    = (TextView)findViewById(R.id.Last_Seen);
        mcircularImage=findViewById(R.id.circular_View);
        mDispalyName.setText(userName);


        mSendBtn    =(ImageButton)findViewById(R.id.sendBtn);
        mAddBtn     =(ImageButton)findViewById(R.id.addBtn);
        mMessage    =(EditText)findViewById(R.id.msgText);

        /*SETTING UP THE RECYCLER VIEW*/
        mMessageAdapter = new MessageAdapter(mMessageList,getApplicationContext());
        mRecyclerView   = (RecyclerView) findViewById(R.id.chat_RecyclerView);

        mlinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mlinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);
        mRecyclerView.setHasFixedSize(true);

        /*LOAD THE MESSAGES AND POPULATE RECYCLER VIEW WITH MESSAGES */
        loadMessage();

      /*=====================ONLINE FUNCTIONALITY====================*/
        mrootref.child("Users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                  String online = dataSnapshot.child("online").getValue().toString();
                  String image  = dataSnapshot.child("image").getValue().toString();
                  if(online.equals("true"))
                  {
                      mLastSeen.setText("Online");
                  }
                  else{
                      long time = Long.parseLong(online);
                      String lastseenTime = UtilClassTime.getTimeAgo(time,getApplicationContext());
                      mLastSeen.setText(lastseenTime);
                  }

                Picasso.with(ChatActivity.this).load(image).fit().placeholder(R.drawable.people).into(mcircularImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*==================CHAT FUNCTIONALITY=========================*/
        mrootref.child("Chat").child(mCurrentUser)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mUserId)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+ mCurrentUser+"/"+mUserId,chatAddMap);
                    chatUserMap.put("Chat/"+mUserId+"/"+mCurrentUser,chatAddMap);

                    mrootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

       /*====================SWIPE FEATURE========================*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                msgQuery.removeEventListener(listener);
                refreshItems();
            }
        });
    }



    //UTILITY METHOD FOR SENDING MESSAGE
     private void sendMessage(){
         String message = mMessage.getText().toString();
         if(!TextUtils.isEmpty(message)) {
             String pathcurrentUser = "Messages/" + mCurrentUser + "/" + mUserId;
             String pathUser = "Messages/" + mUserId + "/" + mCurrentUser;
             DatabaseReference userPushId = mrootref.child("Messages").child(mCurrentUser).child(mUserId).push();
             String push_id = userPushId.getKey();

             Map messageMap = new HashMap();
             messageMap.put("message",message);
             messageMap.put("seen",false);
             messageMap.put("type","text");
             messageMap.put("time",ServerValue.TIMESTAMP);
             messageMap.put("from",mCurrentUser);

             Map userMessageMap = new HashMap();
             userMessageMap.put(pathcurrentUser+"/"+push_id,messageMap);
             userMessageMap.put(pathUser+"/"+push_id,messageMap);

             mMessage.setText("");
             mrootref.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                 @Override
                 public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                     if(databaseError!=null)
                     {
                         Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                     }

                 }
             });


         }
     }

     public void loadMessage(){
          DatabaseReference msgRef = mrootref.child("Messages").child(mCurrentUser).child(mUserId);
          msgQuery = msgRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
          listener  =  msgQuery.addChildEventListener(new ChildEventListener() {
          @Override
          public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              Message msg = dataSnapshot.getValue(Message.class);
              mMessageList.add(msg);
              mMessageAdapter.notifyDataSetChanged();
              mRecyclerView.scrollToPosition(mMessageList.size()-1);

          }

          @Override
          public void onChildChanged(DataSnapshot dataSnapshot, String s) {

          }

          @Override
          public void onChildRemoved(DataSnapshot dataSnapshot) {

          }

          @Override
          public void onChildMoved(DataSnapshot dataSnapshot, String s) {

          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

     }

     private void refreshItems(){
      mCurrentPage++;
      mMessageList.clear();
      loadMessage();
      mSwipeRefreshLayout.setRefreshing(false);
     }
}
