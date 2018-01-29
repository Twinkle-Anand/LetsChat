package com.example.twinkleanand.whatsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.squareup.picasso.Picasso.*;

public class AccountSettings extends AppCompatActivity {
    private FirebaseUser mUser;
    private DatabaseReference mdatabaseRef;
    private CircleImageView mcircleImageView;
    private TextView mdispayname;
    private TextView mstatus;
    private Button mChangeStatusBtn;
    private Button mChangeImageBtn;
    private final static int REQUEST_IMAGE_PICK = 1;
    private StorageReference mStorageReference;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
         mcircleImageView =(CircleImageView) findViewById(R.id.setting_circleImageView);
         mdispayname      =(TextView)findViewById(R.id.setting_displayname);
         mstatus          =(TextView)findViewById(R.id.settings_status);
         mChangeStatusBtn =(Button)findViewById(R.id.settings_changestatusButton);
         mChangeImageBtn  =(Button)findViewById(R.id.setting_changeimageButton);
         mProgressBar     =(ProgressBar)findViewById(R.id.accountsetting_progress);

         mUser = FirebaseAuth.getInstance().getCurrentUser();
         mStorageReference = FirebaseStorage.getInstance().getReference();

         String uid = mUser.getUid();
         mdatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
         mdatabaseRef.keepSynced(true);

         mdatabaseRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
               String name = dataSnapshot.child("name").getValue().toString();
               final String image = dataSnapshot.child("image").getValue().toString();
               String thumbnail =dataSnapshot.child("thumbnail").getValue().toString();
               String status = dataSnapshot.child("status").getValue().toString();

                  //Picasso third party library ==========>>

                 if(!image.equals("default")) {
                     with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.people)
                             .into(mcircleImageView, new Callback() {
                                 @Override
                                 public void onSuccess() {

                                 }

                                 @Override
                                 public void onError() {
                                     with(getApplicationContext()).load(image).placeholder(R.drawable.people).into(mcircleImageView);
                                 }
                             });
                 }
                  mdispayname.setText(name);
                  mstatus.setText(status);
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

         mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String status = mstatus.getText().toString();
                 Intent statusActivity = new Intent(AccountSettings.this,StatusActivity.class);
                 statusActivity.putExtra("status",status);
                 startActivity(statusActivity);
             }
         });

         mChangeImageBtn.setOnClickListener(new View.OnClickListener() {


             @Override
             public void onClick(View view) {
                 Intent intent = new Intent();
                 intent.setType("image/*");
                 intent.setAction(Intent.ACTION_GET_CONTENT);
                 //intent.addCategory(Intent.CATEGORY_OPENABLE);
                 startActivityForResult(Intent.createChooser(intent,"SELECT IMAGE"), REQUEST_IMAGE_PICK);
             }
         });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {


            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    Uri resultUri = result.getUri();
                    File path = new File(resultUri.getPath());
                    Bitmap thumbnail_image = null;

                    try {
                        thumbnail_image = new Compressor(this)
                                .setMaxHeight(200)
                                .setMaxWidth(200)
                                .setQuality(75)
                                .compressToBitmap(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbnail_image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] bytes = baos.toByteArray();


                    StorageReference localRef = mStorageReference.child("profile-images").child(mUser.getUid()+".jpeg");
                    final StorageReference thumbRef = mStorageReference.child("profile-images").child("thumbnail").child(mUser.getUid()+".jpeg");

                    localRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                final String download_url = task.getResult().getDownloadUrl().toString();
                                UploadTask thumbTask = thumbRef.putBytes(bytes);

                                thumbTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {
                                        String thumbdownload_url = thumbTask.getResult().getDownloadUrl().toString();
                                        Map updateMap = new HashMap();
                                        updateMap.put("image",thumbdownload_url);
                                        updateMap.put("thumbnail",download_url);
                                        if (thumbTask.isSuccessful()) {

                                            mdatabaseRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mProgressBar.setVisibility(View.GONE);
                                                        Toast.makeText(AccountSettings.this, "Success Uploading", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            mProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(AccountSettings.this, "Error Occurred in Uploading", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                        }
                    });
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(AccountSettings.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
            }

    }



    @Override
    protected void onPause() {
        super.onPause();
        mdatabaseRef.child("online").setValue("false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mdatabaseRef.child("online").setValue("true");

    }

}

