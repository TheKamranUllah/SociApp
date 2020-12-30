package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Person;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private Toolbar NotificationToolbar;
    private Button clearAllNotificationBtn;

    private ArrayList<NotificationModel> clearNotificationList;
    private ArrayList<String> clearNotifiationViewKeys;

    private RecyclerView RnotificationList;
    private DatabaseReference NotificationRef, ClearAllNotificationRef, FriendRequestRef, DeletionRef;
    private FirebaseAuth mAuth;
    private String notification_sender_id,CurrentUserId;
    List<String> NKeyList;
    List<NotificationModel> NotificationList;
    //public static final String PREFS_NAME = "com.example.sociapp";
  //  int isthereItem;
   ValueEventListener valueEventListener, valueEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
try {
    CurrentUserId = mAuth.getCurrentUser().getUid();

} catch (NullPointerException e){e.printStackTrace();}

        clearAllNotificationBtn = (Button) findViewById(R.id.clear_all_button);
        NotificationToolbar = (Toolbar) findViewById(R.id.notification_toolbar_layout);
        setSupportActionBar(NotificationToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        RnotificationList = (RecyclerView) findViewById(R.id.notification_list);

        RnotificationList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        RnotificationList.setLayoutManager(linearLayoutManager);

       // final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        NKeyList = new ArrayList<>();
        NotificationList = new ArrayList<>();

        NotificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                NotificationList.clear();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        NKeyList.add(dataSnapshot1.getKey());

                        NotificationModel model = dataSnapshot1.getValue(NotificationModel.class);
                        NotificationList.add(model);
                }
                    MessageNotificationAdapter messageNotificationAdapter = new MessageNotificationAdapter(NotificationsActivity.this, NotificationList, NKeyList);
                    RnotificationList.setAdapter(messageNotificationAdapter);

                 //ALl the commented code in this activity was passing values into MainActivity through SharedPreference.
           /*         isthereItem = notificationAdapter1.getItemCount();
                   // Toast.makeText(NotificationsActivity.this, ""+isthereItem, Toast.LENGTH_SHORT).show();

    //writing data into SharedPreference
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt("changingicon",isthereItem);
    //editor.commit();
    //editor.clear();
    editor.apply();*/
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //The below code determine that which kind of notification icon to be displayed.
        //code for determining is in the mainactivity after we initializing navigationView.

       //sendingSharePreference(isthereItem);

       clearAllNotificationBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               FriendRequestNodeMangement( );
               ManaginClearingNotifiation();
           }
       });

    }

    private void FriendRequestNodeMangement()
    {
        this.FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        this.DeletionRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        this.valueEventListener1 = ClearAllFriendRequestNodes( );
        this.FriendRequestRef.addListenerForSingleValueEvent(valueEventListener1);
    }

    private ValueEventListener ClearAllFriendRequestNodes()
    {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot2: dataSnapshot.getChildren())
                    {
                        NotificationModel notificationMode2 = dataSnapshot2.getValue(NotificationModel.class);

                        if (notificationMode2 != null) {
                            final String Notification_Type = notificationMode2.getNotificationtype();
                            if (Notification_Type.equals("Friend_Request")) {

                                final String ReceiverId = notificationMode2.getReceiverid();
                                final String SenderID = notificationMode2.getSenderid();

                                DeletionRef.child(SenderID).child(ReceiverId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                DeletionRef.child(ReceiverId).child(SenderID).removeValue();
                                            }
                                        });

                            }
                        }
                    }
                }
                else
                    {
                        Toast.makeText(NotificationsActivity.this, "There is no Notification!", Toast.LENGTH_SHORT).show();
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public void ManaginClearingNotifiation( )
    {
        this.ClearAllNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        this.valueEventListener = ClearAllNotification();
        this.ClearAllNotificationRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private ValueEventListener ClearAllNotification()
    {
        clearNotificationList = new ArrayList<>();
        clearNotifiationViewKeys = new ArrayList<>();

         return new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
             {
                 int position = 0;

                 if (dataSnapshot.exists())
                 {
                  for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                  {
                      clearNotifiationViewKeys.add(dataSnapshot1.getKey());
                      NotificationModel notificationModel = dataSnapshot1.getValue(NotificationModel.class);

                      if (notificationModel != null)
                      {
                          String ReceiverId = notificationModel.getReceiverid();
                          if (CurrentUserId.equals(ReceiverId)) {
                              ClearAllNotificationRef.child(clearNotifiationViewKeys.get(position)).removeValue();
                          }
                      }

                      position++;
                  }
                 }
                 else {
                     Toast.makeText(NotificationsActivity.this, "There is no Notification!", Toast.LENGTH_SHORT).show();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         };

    }

  /*  public void sendingSharePreference(int checkItem)
    {
        if (checkItem == 0)
        {
            final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            //writing data into SharedPreference
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("changingicon",checkItem);
            //editor.commit();
            editor.clear();
            editor.apply();
        }
    }*/
}
