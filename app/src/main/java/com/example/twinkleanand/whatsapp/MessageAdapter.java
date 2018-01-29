package com.example.twinkleanand.whatsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Twinkle Anand on 1/27/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> mMessageList;
    private FirebaseAuth mAuth ;
    private Context mContext;



    public MessageAdapter(List<Message> mMessageList,Context context){
        this.mMessageList=mMessageList;
        this.mContext = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatlist_item,parent,false);

        return new MessageViewHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
             if(mMessageList.size() > 0) {
                 Message c = mMessageList.get(position);
                 String from_user = c.getFrom();
                 mAuth = FirebaseAuth.getInstance();
                 String mCurrentUser = mAuth.getCurrentUser().getUid();
                 Drawable background = holder.mMessageView.getBackground();
                 GradientDrawable gradientDrawable = (GradientDrawable) background;

                 if (from_user.equals(mCurrentUser)) {

                     gradientDrawable.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                     holder.mMessageView.setTextColor(Color.WHITE);
                     holder.profileView.setVisibility(View.GONE);
                     holder.mlayout.setGravity(Gravity.RIGHT);

                 } else {
                     gradientDrawable.setColor(Color.WHITE);
                     holder.mMessageView.setTextColor(R.color.colorPrimary);
                     holder.profileView.setVisibility(View.GONE);
                     holder.mlayout.setGravity(Gravity.LEFT);
                 }
                 holder.mMessageView.setText(c.getMessage());
             }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
    public TextView mMessageView;
    public CircleImageView profileView;
    public RelativeLayout mlayout;

    public MessageViewHolder(View itemView) {
        super(itemView);

        mMessageView = (TextView)itemView.findViewById(R.id.Chatlist_messageView);
        profileView  = (CircleImageView)itemView.findViewById(R.id.Chatlist_profileView);
        mlayout      = (RelativeLayout)itemView.findViewById(R.id.Chatlist_layout);
       }
   }
}
