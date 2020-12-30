package com.example.sociapp;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    List<NotificationModel> myMessageNotification = new ArrayList<>();
    List<String> myMessageNotificationKey = new ArrayList<>();
    FirebaseAuth mAuth;
    String CurrentUserId;
    DatabaseReference messageNotificationRef, UsersRef;

    private static int TYPE_FRIEND_REQUEST = 0;
    private static int TYPE_MESSAGE = 1;



    public MessageNotificationAdapter(Context context, List<NotificationModel> myMessageNotification, List<String> myMessageNotificationKey) {
        this.context = context;
        this.myMessageNotification = myMessageNotification;
        this.myMessageNotificationKey = myMessageNotificationKey;
    }

    @Override
    public int getItemViewType(int position) {

        if (myMessageNotification.get(position).getNotificationtype().equals("Message"))
        {
            return TYPE_MESSAGE;
        }

        else
            {
                return TYPE_FRIEND_REQUEST;
            }
       // return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        mAuth = FirebaseAuth.getInstance();

        View view;

        if (viewType == TYPE_MESSAGE)
        {
            view = LayoutInflater.from(context).inflate(R.layout.message_notification_layout, parent, false);
            return new MessageNotificationAdapter.MessageNotificationViewHolder(view);
        }
        else
            {
                view = LayoutInflater.from(context).inflate(R.layout.friend_request_notification_layout, parent, false);
                return new MessageNotificationAdapter.NotificationViewHolder(view);

            }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_MESSAGE)
        {
            ((MessageNotificationViewHolder) holder).setMessageNotification
                    (myMessageNotification.get(position),myMessageNotificationKey.get(position));
        } else {

    ((NotificationViewHolder) holder).setFriendRequestNotification
            (myMessageNotification.get(position), myMessageNotificationKey.get(position));

        }
    }


    @Override
    public int getItemCount() {
        if(myMessageNotification != null) {
            return myMessageNotification.size();
        }
        else
        {
            Toast.makeText(context, "List is null", Toast.LENGTH_SHORT).show();
            return 0;

        }
    }



    public  class MessageNotificationViewHolder extends RecyclerView.ViewHolder
    {

        public CircleImageView profileimage;
        public TextView FullName, Date, UserMessage, Time;
        public LinearLayout MessageNotificaitonLayout;

        public MessageNotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);

            MessageNotificaitonLayout = itemView.findViewById(R.id.message_notification_layout);
            profileimage = itemView.findViewById(R.id.message_notifcation_profile_image);
            FullName = itemView.findViewById(R.id.message_notification_full_name_in);
            Date = itemView.findViewById(R.id.message_notication_date);
            UserMessage = itemView.findViewById(R.id.user_message_notification);
            Time = itemView.findViewById(R.id.message_notication_time);

        }


        public void setMessageNotification(NotificationModel myMessageNotification1,String messageNotificationKey)
        {
            final String RecieverId = myMessageNotification1.getReceiverid();
            final String SenderId = myMessageNotification1.getSenderid();

            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            messageNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(messageNotificationKey);

            try {
                CurrentUserId = mAuth.getCurrentUser().getUid();

            } catch (NullPointerException e){e.printStackTrace();}

            if (!RecieverId.equals(CurrentUserId))
            {
                MessageNotificaitonLayout.setVisibility(View.GONE);
            }


            String ProfileImage = myMessageNotification1.getProfileimage();
            String UserFullName = myMessageNotification1.getFullname();
            String date = myMessageNotification1.getDate();
            String SenderMessage = myMessageNotification1.getUsermessage();
            String time = myMessageNotification1.getTime();

            Picasso.get().load(ProfileImage).placeholder(R.drawable.profile).into(profileimage);
            FullName.setText(UserFullName);
            Date.setText(date);
            UserMessage.setText(SenderMessage);
            Time.setText(time);

            UsersRef.child(SenderId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        final String userName = dataSnapshot.child("Full_Name").getValue().toString();


                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent AcceptIntent = new Intent(context,ChatActivity.class);
                                AcceptIntent.putExtra("visit_user_id",SenderId );
                                AcceptIntent.putExtra("userName", userName);
                                context.startActivity(AcceptIntent);
                                ((NotificationsActivity)context).finish();

                                MessageNotificaitonLayout.setVisibility(View.GONE);
                                messageNotificationRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "message Notification Removed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        }
    }

    public  class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        public CircleImageView profileimage;
        public TextView FullName, Date;
        public Button AcceptBtn, DeclineBtn;
        public LinearLayout NotificaitonLayout;

        public NotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);

            NotificaitonLayout = itemView.findViewById(R.id.notification_layout);
            profileimage = itemView.findViewById(R.id.notifcation_profile_image);
            FullName = itemView.findViewById(R.id.notification_full_name_in);
            Date = itemView.findViewById(R.id.notication_date);
            AcceptBtn = itemView.findViewById(R.id.accept_friend_request);
            DeclineBtn = itemView.findViewById(R.id.decline_friend_request);
        }

    public void setFriendRequestNotification(NotificationModel notificationMode1, final String notificationKey)
        {


            final String RecieverId = notificationMode1.getReceiverid();
            final String SenderId = notificationMode1.getSenderid();


            //Below commented block of code was transferring ReceiverId to MainActivity.

       /* final SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //writing data into SharedPreference
        SharedPreferences.Editor editor = settings.edit();
        //editor.putInt("changingicon",isthereItem);
        editor.putString("ReceiverID", RecieverId);
        //editor.commit();
        editor.apply();*/

       DatabaseReference  NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(notificationKey);

            try {
                CurrentUserId = mAuth.getCurrentUser().getUid();

            } catch (NullPointerException e){e.printStackTrace();}


            if (!RecieverId.equals(CurrentUserId))
            {
                NotificaitonLayout.setVisibility(View.GONE);
            }

       DatabaseReference FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");


            String ProfileImage = notificationMode1.getProfileimage();
            String UserFullName = notificationMode1.getFullname();
            String date = notificationMode1.getDate();

            Picasso.get().load(ProfileImage).placeholder(R.drawable.profile).into(profileimage);
            FullName.setText(UserFullName);
            Date.setText(date);

            AcceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent AcceptIntent = new Intent(context,PersonProfileActivity.class);
                    AcceptIntent.putExtra("visit_user_id",SenderId );
                    AcceptIntent.putExtra("notificationid",notificationKey);
                    AcceptIntent.putExtra("actionbar",420);
                    context.startActivity(AcceptIntent);
                    NotificaitonLayout.setVisibility(View.GONE);
             /*   NotificationRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Notification Removed!", Toast.LENGTH_SHORT).show();
                    }
                });*/
                }
            });

            DeclineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent DeclineIntent = new Intent(context,PersonProfileActivity.class);
                    DeclineIntent.putExtra("visit_user_id",SenderId );
                    DeclineIntent.putExtra("notificationid",notificationKey);
                    DeclineIntent.putExtra("actionbar",420);
                    context.startActivity(DeclineIntent);
                    NotificaitonLayout.setVisibility(View.GONE);

          /*      NotificationRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Notification Removed!", Toast.LENGTH_SHORT).show();
                    }
                });*/
                }
            });
        }
    }
}
