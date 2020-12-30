package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private Toolbar PersonProfileToolbar;
    private TextView Username, UserProfName, Userdob, UserCountry, UserRelation, UserStatus, UserGender;
    private CircleImageView userProfileImage;
    private Button SendFriendRequest, DeclineFriendRequest;
    private ScrollView fullScroll;
    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef, NotificationRef, NotificationDeletionRef,NotificationDeletionRef2;
    private DatabaseReference NotificationRef3;
    private FirebaseAuth mAuth;
    private int ShowActionBar;
    private String SenderUserId, ReceiverUserId, CURRENT_STATE, saveCurrentDate, saveCurrentTime, TheNotificationId;
    private boolean Allow = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        SenderUserId = mAuth.getCurrentUser().getUid();
        PersonProfileToolbar = (Toolbar) findViewById(R.id.special_420_layout);

        try {
    ReceiverUserId = getIntent().getExtras().get("visit_user_id").toString();

    ShowActionBar = Integer.parseInt(getIntent().getExtras().get("actionbar").toString());

    TheNotificationId = getIntent().getExtras().get("notificationid").toString();
}
catch (NullPointerException e){e.printStackTrace();}

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
try {
    NotificationDeletionRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(TheNotificationId);
}  catch (NullPointerException e){e.printStackTrace();}

       // Toast.makeText(this, ""+ShowActionBar, Toast.LENGTH_SHORT).show();

        if (ShowActionBar == 420) {
            setSupportActionBar(PersonProfileToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Person Profile");
        } else {
            PersonProfileToolbar.setVisibility(View.GONE);
        }

        InitializeFields();

    try {
        UsersRef.child(ReceiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("User_Name").getValue().toString();
                    String myProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String myStatus = dataSnapshot.child("Status").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("RelationshipStatus").getValue().toString();
                    String myCountry = dataSnapshot.child("Country_Name").getValue().toString();
                    String myDob = dataSnapshot.child("Date_Of_Birth").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();
                    fullScroll.fullScroll(View.FOCUS_DOWN);

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    Username.setText("@ " + myUserName);
                    UserProfName.setText(myProfileName);
                    UserStatus.setText(myStatus);
                    UserRelation.setText("Relationship: " + myRelationStatus);
                    UserCountry.setText("Country: " + myCountry);
                    Userdob.setText("Date Of Birth: " + myDob);
                    UserGender.setText("Gender " + myGender);

                    MaintainaceOfButton();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    } catch (NullPointerException e){e.printStackTrace();}

         DeclineFriendRequest.setVisibility(View.INVISIBLE);
         DeclineFriendRequest.setEnabled(false);



        if (!SenderUserId.equals(ReceiverUserId)) {
            SendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequest.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToPerson();
                    }

                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest( );
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest( );
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnfriendExistingFriend ( );
                    }
                }
            });
        } else {
            SendFriendRequest.setVisibility(View.INVISIBLE);
            DeclineFriendRequest.setVisibility(View.INVISIBLE);
        }

    }



    private void UnfriendExistingFriend()
    {
        FriendsRef.child(SenderUserId).child(ReceiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                           FriendsRef.child(ReceiverUserId).child(SenderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequest.setText("Send Friend Request");

                                                DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        //Here when we come from notification activity we have the "TheNotificationId" and Allow is already false so both conditions becoms false.
        if (null == TheNotificationId || Allow)
        {
//            SendFriendRequest.setVisibility(View.GONE);
//            DeclineFriendRequest.setVisibility(View.GONE);
//            DeclineFriendRequest.setEnabled(false);
//            SendFriendRequest.setEnabled(false);

            final View mview = LayoutInflater.from(PersonProfileActivity.this).inflate(R.layout.dialog_layout, null);
            TextView Message = mview.findViewById(R.id.dialog_text);
            Button OkBtn = mview.findViewById(R.id.dialog_btn);

            AlertDialog.Builder mbuilder = new AlertDialog.Builder(PersonProfileActivity.this, R.style.mydialog);
            mbuilder.setView(mview);
            mbuilder.setCancelable(false);
            String message = "You can't accept from here, only from notifications can be accepted!";
            Message.setText(message);

            final Dialog dialog = mbuilder.create();
            dialog.show();

            OkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    ((ViewGroup) mview.getParent()).removeView(mview);
                }
            });
        }
        else
            {

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                FriendsRef.child(SenderUserId).child(ReceiverUserId).child("date").setValue(saveCurrentDate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    FriendsRef.child(ReceiverUserId).child(SenderUserId).child("date").setValue(saveCurrentDate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        FriendRequestRef.child(SenderUserId).child(ReceiverUserId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            FriendRequestRef.child(ReceiverUserId).child(SenderUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                SendFriendRequest.setEnabled(true);
                                                                                                CURRENT_STATE = "friends";
                                                                                                SendFriendRequest.setText("Unfriend");
                                                                                                try {
                                                                                                    NotificationDeletionRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(Void aVoid) {
                                                                                                            Toast.makeText(PersonProfileActivity.this, "Request Accepted!", Toast.LENGTH_SHORT).show();
                                                                                                            Allow = true;
                                                                                                        }
                                                                                                    });
                                                                                                }    catch (NullPointerException e){e.printStackTrace();}
                                                                                                DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                                DeclineFriendRequest.setEnabled(false);

                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });

            }

    }

    private void CancelFriendRequest()
    {
//Here when we come from notification activity we have the "TheNotificationId" and Allow is already false so both conditions becoms false.
        if (null == TheNotificationId || Allow)
        {
            final View mview = LayoutInflater.from(PersonProfileActivity.this).inflate(R.layout.dialog_layout, null);
            TextView Message = mview.findViewById(R.id.dialog_text);
            Button OkBtn = mview.findViewById(R.id.dialog_btn);

            AlertDialog.Builder mbuilder = new AlertDialog.Builder(PersonProfileActivity.this, R.style.mydialog);
            mbuilder.setView(mview);
            mbuilder.setCancelable(false);
            String message = "You can't cancel the friend request from here directly.";
            Message.setText(message);

            final Dialog dialog = mbuilder.create();
            dialog.show();

            OkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    ((ViewGroup) mview.getParent()).removeView(mview);
                }
            });
        }

        else {
            FriendRequestRef.child(SenderUserId).child(ReceiverUserId)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FriendRequestRef.child(ReceiverUserId).child(SenderUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    SendFriendRequest.setEnabled(true);
                                                    CURRENT_STATE = "not_friends";
                                                    SendFriendRequest.setText("Send Friend Request");
                                                    try {
                                                        NotificationDeletionRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(PersonProfileActivity.this, "Request Cancelled!", Toast.LENGTH_SHORT).show();
                                                                Allow = true;
                                                            }
                                                        });
                                                    } catch (NullPointerException e) {
                                                        e.printStackTrace();
                                                    }

                                                    DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                    DeclineFriendRequest.setEnabled(false);
                                                }
                                            }
                                        });
                            }
                        }

                    });
        }
    }

    private void MaintainaceOfButton()
    {
        FriendRequestRef.child(SenderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(ReceiverUserId))
                {
                    String request_type = dataSnapshot.child(ReceiverUserId).child("request_type").getValue().toString();
                    if (request_type.equals("sent"))
                    {
                    CURRENT_STATE = "request_sent";
                    SendFriendRequest.setText("Cancel Friend Request");

                    DeclineFriendRequest.setVisibility(View.INVISIBLE);
                    DeclineFriendRequest.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        CURRENT_STATE = "request_received";
                        SendFriendRequest.setText("Accept Friend Request");

                        DeclineFriendRequest.setVisibility(View.VISIBLE);
                        DeclineFriendRequest.setEnabled(true);

                        DeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }

                else
                    {
                        FriendsRef.child(SenderUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if (dataSnapshot.hasChild(ReceiverUserId))
                             {
                                 CURRENT_STATE = "friends";
                                 SendFriendRequest.setText("Unfriend");

                                 DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                 DeclineFriendRequest.setEnabled(false);
                             }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequestToPerson() {

        final View mview = LayoutInflater.from(PersonProfileActivity.this).inflate(R.layout.dialog_two_btns_layout, null);
        TextView Message = mview.findViewById(R.id.two_btns_dialog_text);
        Button OkBtn = mview.findViewById(R.id.ok_dialog_btn);
        Button CancelBtn = mview.findViewById(R.id.cancel_dialog_btn);
        OkBtn.setText("Send");
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(PersonProfileActivity.this, R.style.mydialog);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        String message = "If you send friend request, you won't be able to cancel it, only the receiver party can cancel it!";
        Message.setText(message);

        final Dialog dialog = mbuilder.create();
        dialog.show();

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FriendRequestRef.child(SenderUserId).child(ReceiverUserId)
                        .child("request_type").setValue("sent")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FriendRequestRef.child(ReceiverUserId).child(SenderUserId)
                                            .child("request_type").setValue("received")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        SendFriendRequest.setEnabled(true);
                                                        CURRENT_STATE = "request_sent";
                                                        SendFriendRequest.setText("Friend Request Sent");
                                                        DeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                        DeclineFriendRequest.setEnabled(false);
                                                        AllNotificationInfo( );
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                dialog.dismiss();
                ((ViewGroup) mview.getParent()).removeView(mview);
            }
        });


        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((ViewGroup) mview.getParent()).removeView(mview);
            }
        });

    }

   /* private void PushinSenderID()
    {
        Intent sendingId = new Intent(PersonProfileActivity.this, NotificationsActivity.class);
        sendingId.putExtra("senderId", SenderUserId);
        startActivity(sendingId);
    }*/

    private void AllNotificationInfo()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

//HH means 24 hours time and hh means 12 hours time.
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        UsersRef.child(SenderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.exists())
             {
                 String  userprofileImage = dataSnapshot.child("profileimage").getValue().toString();
                 String fullname = dataSnapshot.child("Full_Name").getValue().toString();

                 HashMap postsMap = new HashMap( );
                 postsMap.put("date", saveCurrentDate);
                 postsMap.put("profileimage", userprofileImage);
                 postsMap.put("fullname", fullname);
                 postsMap.put("receiverid", ReceiverUserId);
                 postsMap.put("senderid", SenderUserId);
                 postsMap.put("notificationtype", "Friend_Request");

                 NotificationRef.child(SenderUserId+saveCurrentDate+saveCurrentTime).updateChildren(postsMap)
                         .addOnCompleteListener(new OnCompleteListener() {
                             @Override
                             public void onComplete(@NonNull Task task) {
                                 if (task.isSuccessful()){

                                     Toast.makeText(PersonProfileActivity.this, "Friend Request Sent!", Toast.LENGTH_SHORT).show();

                                 }
                                 else
                                 {
                                     String message = task.getException().getMessage();
                                     Toast.makeText(PersonProfileActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();

                                 }
                             }
                         });
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void InitializeFields() {
        Username = (TextView) findViewById(R.id.person_username);
        UserProfName = (TextView) findViewById(R.id.person_full_name);
        CURRENT_STATE = "not_friends";
        Userdob = (TextView) findViewById(R.id.person_dob);
        UserCountry = (TextView) findViewById(R.id.person_country);
        UserRelation = (TextView) findViewById(R.id.person_relationship_status);
        UserStatus = (TextView) findViewById(R.id.person_profile_status);
        UserGender = (TextView) findViewById(R.id.person_gender);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        SendFriendRequest = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequest = (Button) findViewById(R.id.person_decline_friend_request_btn);
        fullScroll = (ScrollView) findViewById(R.id.fullscroll);
    }

}
