package com.chatty.chat5;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhance this complicated code following instructions on video 25
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private String mCurrent_state;
    private FirebaseUser mCurrent_user;

    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Gets the user ID from the intent originated in UsersActivity
        String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Gets the user thanks to the last variable
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        // Initialize a reference to another part of the database, the friends request section of it
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        // To the friend part of the database
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        // Gets current userId
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_user_image);
        mProfileName = findViewById(R.id.profile_display_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = findViewById(R.id.profile_fRequest_btn);
        mDeclineBtn = findViewById(R.id.profile_declineBtn);

        mCurrent_state = "no_friends";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        // Does different things on data change or cancellation
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Creates new variables to store the snapshots
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                // Sets the strings to the views
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.image).into(mProfileImage);

                /***************Friends list / Request Feature****************/
                // This checks if friends requests have been received by specific users
                // functionality is similar to the buttons at the bottom of this activity
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Request");

                                // Show 2nd btn
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Request");

                                // Hide 2nd btn
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                        } else {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // They are already friends
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this Person");

                                        // Hide 2nd btn
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        // What happens when friendRequest button is pressed
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // User can't press the button twice or more
                mProfileSendReqBtn.setEnabled(false);

                /***************No Friends****************/
                // What happens if the variable is equal to "no_friends" this means that these two users aren't friends yet
                if(mCurrent_state.equals("no_friends")){
                    // Second layer in db is current user Id, the next is the other user's Id, the next is the request type. The value for it is received
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                // From the friend request portion of the db, go to other user's id, then a child which is the current user's id, then the request type is set to "received'
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // current state string changes and the text button changes too
                                        mCurrent_state = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Request");

                                        // Hide 2nd btn
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                        Toast.makeText(ProfileActivity.this,"Request Sent", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this,"Fail sending request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }

                /***************Cancel request state****************/
                if (mCurrent_state.equals("req_sent")){
                    // Removing information from database
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    // current state string changes and the text button changes too
                                    mCurrent_state = "no_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    // Hide 2nd btn
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                /***************Req received state****************/
                if (mCurrent_state.equals("req_received")){
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap<>();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /***********************Unfriends************************/
                if (mCurrent_state.equals("friends")){
                    Map unfriendMap = new HashMap<>();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "no_friends";
                                mProfileSendReqBtn.setText("Send friend request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

            }
        });

    }
}