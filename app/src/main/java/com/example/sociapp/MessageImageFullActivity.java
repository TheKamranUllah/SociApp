package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MessageImageFullActivity extends AppCompatActivity {

    private ImageView fullSizeImageView;
    private String messageRecieverId, messageKey, messageRecieverName;
    private FirebaseAuth mAuth;
    private String messageSenderId, FromUserID;
    private DatabaseReference  MessageSenderRef, MessageRecieverRef, GettingFromUserIDReference;
    private  DatabaseReference RootRef;

    private StorageReference deletingImageMessageReference = FirebaseStorage.getInstance().getReference().child("Image Messages");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_image_full);

        RootRef = FirebaseDatabase.getInstance().getReference();

        fullSizeImageView = (ImageView) findViewById(R.id.full_size_message_image_view);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        messageRecieverId = getIntent().getExtras().get("reciever_id").toString();
         messageKey = getIntent().getExtras().get("image_message_key").toString();
         messageRecieverName = getIntent().getExtras().get("messageRecieverName").toString();

        MessageRecieverRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageSenderId).child(messageRecieverId);
        MessageSenderRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageRecieverId).child(messageSenderId);
        GettingFromUserIDReference = FirebaseDatabase.getInstance().getReference().child("Messages")
                .child(messageSenderId).child(messageRecieverId).child(messageKey);

        fullSizeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gettingFromUserID( );

            }
        });


        RootRef.child("Messages").child(messageSenderId).child(messageRecieverId).child(messageKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                       String myImage = dataSnapshot.child("message").getValue().toString();
                       Picasso.get().load(myImage).placeholder(R.drawable.profile).into(fullSizeImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void gettingFromUserID( )
    {
        GettingFromUserIDReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    FromUserID = dataSnapshot.child("from").getValue().toString();

                    if (FromUserID.equals(messageSenderId))
                    {
                        final View mview = LayoutInflater.from(MessageImageFullActivity.this).inflate(R.layout.dialog_no_btn_layout, null);
                        final TextView message1 = mview.findViewById(R.id.dialog_option_one);
                        final TextView message2 = mview.findViewById(R.id.dialog_option_two);
                        final ImageView imageView = mview.findViewById(R.id.dialog_Image);


                        imageView.setImageResource(R.drawable.process);
                        final AlertDialog.Builder mbuilder = new AlertDialog.Builder(MessageImageFullActivity.this, R.style.mydialog);
                        mbuilder.setView(mview);

                        final Dialog dialog = mbuilder.create();

                        message1.setText("Message Status");
                        message2.setText("Delete Message");

                        message1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Toast.makeText(MessageImageFullActivity.this, "Image Message Sent", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                //The below code remove parentView the view we click once, if we don't add that the alert
                                //dialoge won't appear next time we click in chat activity and will throuw exception.
                                ((ViewGroup) mview.getParent()).removeView(mview);

                            }
                        });

                        message2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MessageSenderRef.child(messageKey).removeValue();
                                MessageRecieverRef.child(messageKey).removeValue();
                                deletingImageMessageReference.child(messageKey).delete();

                                dialog.dismiss();
                                Toast.makeText(MessageImageFullActivity.this, "Image Message Deleted!", Toast.LENGTH_SHORT).show();
                                ((ViewGroup) mview.getParent()).removeView(mview);
                                SendUserToChatActivity();

                            }
                        });

                        try {
                            dialog.show();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(MessageImageFullActivity.this, "You can't delete someone else message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToChatActivity() {

        Intent StatusPostIntent = new Intent(MessageImageFullActivity.this, ChatActivity.class);
        StatusPostIntent.putExtra("visit_user_id", messageRecieverId);
        StatusPostIntent.putExtra("userName", messageRecieverName);
        startActivity(StatusPostIntent);

        startActivity(StatusPostIntent);
    }
}
