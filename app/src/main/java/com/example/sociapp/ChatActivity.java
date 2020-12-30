package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.network.UpdateMetadataNetworkRequest;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

private     Toolbar ChattoolBar;
private     ImageButton SendMessageButton;
private     EditText userMessageInput;
private     RecyclerView userMessageList;
private     final List<Messages> messagesList = new ArrayList<>();
private     final List<String> keysofmessage = new ArrayList<>();
private     LinearLayoutManager  linearLayoutManager;
private     MessagesAdapter messagesAdapter;

private     String   messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime;
private     TextView  ReceiverName, userLastSeen;
private     CircleImageView ReceiverProfileImage;
private     ProgressDialog loadingBar;

private     DatabaseReference RootRef,UsersRef ,StateRef, MessageNotificationRef, ChatRef;
private     FirebaseAuth mAuth;
private     ImageView sendImageButtton;
private     Uri imageUri;
private     StorageTask uploadTask;
private     String myUri = "";
private     ImageView previewImage, closePreviewImageMessage;
private     DatabaseReference publicControlReference;
private     boolean testIfTrue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StateRef = FirebaseDatabase.getInstance().getReference().child("UserState").child(messageReceiverID);
        MessageNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        ChatRef = FirebaseDatabase.getInstance().getReference().child("ChatState");
        publicControlReference = FirebaseDatabase.getInstance().getReference().child("PublicControls");


        IntializeFields( );
      previewImage.setVisibility(View.GONE);
      closePreviewImageMessage.setVisibility(View.GONE);

        DisplayReceiverInfo( );

        fetchMessages( );

        sendImageButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), 444);
            };
        });


        closePreviewImageMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage.setImageResource(0);
                previewImage.setVisibility(View.GONE);
                closePreviewImageMessage.setVisibility(View.GONE);
            }
        });


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String messageText = userMessageInput.getText().toString();

                if (TextUtils.isEmpty(messageText) && previewImage.getDrawable() != null)
                {
                   sendImageMessage();
                }
                else if (!TextUtils.isEmpty(messageText))
                {
                    SendMessage( );
                }
                else
                    {
                        Toast.makeText(ChatActivity.this, "Please select and image or write a text message and try again!", Toast.LENGTH_SHORT).show();
                    }



            }
        });

        //fetchMessages( ); we were fetching all the messages in onStart metho of this activity.

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        previewImage.setVisibility(View.VISIBLE);
        closePreviewImageMessage.setVisibility(View.VISIBLE);

        if (requestCode == 444 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {


              imageUri = data.getData();
              previewImage.setImageURI(imageUri);


        }
    }

    private void sendImageMessage( )
    {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.justsaying);

        loadingBar.setTitle("Sending Image");
        loadingBar.setMessage("Please wait, while we are sending Image...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Messages");

        //the firebase store Message is a child of root and messageSenderID is the child of Message and so on in hirarchy.
        final String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                .child(messageReceiverID).push();

        //below line of code generate unique Key for each file to be store in firebase storage.
        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = storageReference.child(message_push_id);
        uploadTask = filePath.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {

                if (!task.isSuccessful())
                {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful())
                {
                    Uri downloadUri = task.getResult();
                    myUri = downloadUri.toString();
                    // Picasso.get().load(myUri).into(previewImage);


                    Map messageImageBody = new HashMap();

                    messageImageBody.put("message", myUri);
                    //last segment can only be retrived when the image is got from gallery I think that is now saved in imageUri
                    messageImageBody.put("name", imageUri.getLastPathSegment());
                    messageImageBody.put("time", saveCurrentTime);
                    messageImageBody.put("date", saveCurrentDate);
                    messageImageBody.put("messageId", message_push_id);
                    messageImageBody.put("type", "image");
                    messageImageBody.put("from", messageSenderID);
                    messageImageBody.put("to", messageReceiverID);

                    Map messageBodyDetail = new HashMap();
                    messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageImageBody);
                    messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageImageBody);

                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });


                    RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                previewImage.setVisibility(View.GONE);
                                //MessagesNotificationInfo(messageText);
                                closePreviewImageMessage.setVisibility(View.GONE);
                                imageMessagesNotificationInfo( );
                            }
                            else
                            {
                                loadingBar.dismiss();
                                previewImage.setVisibility(View.GONE);
                                closePreviewImageMessage.setVisibility(View.GONE);
                                String message = task.getException().getMessage();
                                Toast.makeText(ChatActivity.this, "Error "+ message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(ChatActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void fetchMessages()
    {

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.exists()) {

                            keysofmessage.add(dataSnapshot.getKey());

                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);

                            userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

                            messagesAdapter.notifyDataSetChanged();

                        }
                        //Toast.makeText(ChatActivity.this, ""+keysofmessage, Toast.LENGTH_SHORT).show();
//                        messagesAdapter = new MessagesAdapter(messagesList, keysofmessage,messageReceiverID,ChatActivity.this);
//                        userMessageList.setAdapter(messagesAdapter);


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void SendMessage()
    {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.justsaying);


        final String messageText = userMessageInput.getText().toString();

                //the firebase store Message is a child of root and messageSenderID is the child of Message and so on in hirarchy.
        String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
        String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
        .child(messageReceiverID).push();

        String message_push_id = user_message_key.getKey();

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
                saveCurrentTime = currentTime.format(calForTime.getTime());

                Map messageTextBody = new HashMap();

                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);

                Map messageBodyDetail = new HashMap();
                messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageTextBody);
                messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });


                RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful())
                        {
                            MessagesNotificationInfo(messageText);
                            userMessageInput.setText("");
                        }
                        else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ChatActivity.this, "Error "+ message, Toast.LENGTH_SHORT).show();
                                userMessageInput.setText("");
                            }
                    }
                });


    }


    private void MessagesNotificationInfo(final String messageText)
    {
         ChatRef.child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.exists())
                 {
                     String ChatState = dataSnapshot.child("chatStat").getValue().toString();

                     //Toast.makeText(ChatActivity.this, ""+ChatState, Toast.LENGTH_SHORT).show();

                     if (ChatState.equals("offline"))
                     {
                         UsersRef.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 if (dataSnapshot.exists())
                                 {
                                     String senderProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                                     String senderProfileName = dataSnapshot.child("Full_Name").getValue().toString();

                                     HashMap messageNotifiationMap = new HashMap( );
                                     messageNotifiationMap.put("date", saveCurrentDate);
                                     messageNotifiationMap.put("time", saveCurrentTime);
                                     messageNotifiationMap.put("profileimage", senderProfileImage);
                                     messageNotifiationMap.put("fullname", senderProfileName);
                                     messageNotifiationMap.put("receiverid", messageReceiverID);
                                     messageNotifiationMap.put("senderid", messageSenderID);
                                     messageNotifiationMap.put("usermessage", messageText);
                                     messageNotifiationMap.put("notificationtype", "Message");

                                     MessageNotificationRef.child(messageSenderID+saveCurrentDate+saveCurrentTime).updateChildren(messageNotifiationMap)
                                             .addOnCompleteListener(new OnCompleteListener() {
                                                 @Override
                                                 public void onComplete(@NonNull Task task) {
                                                     if (task.isSuccessful()){

                                                          Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                                                     }
                                                     else
                                                     {
                                                         String message = task.getException().getMessage();
                                                         Toast.makeText(ChatActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();

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
                     else if(ChatState.equals("online"))
                     {
                         Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                     }
                     else{
                         Toast.makeText(ChatActivity.this, "else Part running", Toast.LENGTH_SHORT).show();
                     }

                 }

                 else
                     {
                         UsersRef.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 if (dataSnapshot.exists())
                                 {
                                     String senderProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                                     String senderProfileName = dataSnapshot.child("Full_Name").getValue().toString();

                                     HashMap messageNotifiationMap = new HashMap( );
                                     messageNotifiationMap.put("date", saveCurrentDate);
                                     messageNotifiationMap.put("time", saveCurrentTime);
                                     messageNotifiationMap.put("profileimage", senderProfileImage);
                                     messageNotifiationMap.put("fullname", senderProfileName);
                                     messageNotifiationMap.put("receiverid", messageReceiverID);
                                     messageNotifiationMap.put("senderid", messageSenderID);
                                     messageNotifiationMap.put("usermessage", messageText);
                                     messageNotifiationMap.put("notificationtype", "Message");

                                     MessageNotificationRef.child(messageSenderID+saveCurrentDate+saveCurrentTime).updateChildren(messageNotifiationMap)
                                             .addOnCompleteListener(new OnCompleteListener() {
                                                 @Override
                                                 public void onComplete(@NonNull Task task) {
                                                     if (task.isSuccessful()){

                                                          Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                                                     }
                                                     else
                                                     {
                                                         String message = task.getException().getMessage();
                                                         Toast.makeText(ChatActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();

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
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }


    private void imageMessagesNotificationInfo(  )
    {

        final String ImageMessageDetail = "Message Contains an Image, click to see...";

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        ChatRef.child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String ChatState = dataSnapshot.child("chatStat").getValue().toString();

                    //Toast.makeText(ChatActivity.this, ""+ChatState, Toast.LENGTH_SHORT).show();

                    if (ChatState.equals("offline"))
                    {
                        UsersRef.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    String senderProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                                    String senderProfileName = dataSnapshot.child("Full_Name").getValue().toString();

                                    HashMap messageNotifiationMap = new HashMap( );
                                    messageNotifiationMap.put("date", saveCurrentDate);
                                    messageNotifiationMap.put("time", saveCurrentTime);
                                    messageNotifiationMap.put("profileimage", senderProfileImage);
                                    messageNotifiationMap.put("fullname", senderProfileName);
                                    messageNotifiationMap.put("receiverid", messageReceiverID);
                                    messageNotifiationMap.put("senderid", messageSenderID);
                                    messageNotifiationMap.put("usermessage", ImageMessageDetail);
                                    messageNotifiationMap.put("notificationtype", "Message");

                                    MessageNotificationRef.child(messageSenderID+saveCurrentDate+saveCurrentTime).updateChildren(messageNotifiationMap)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()){

                                                        Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                                                    }
                                                    else
                                                    {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(ChatActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();

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
                    else if(ChatState.equals("online"))
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "else Part running", Toast.LENGTH_SHORT).show();
                    }

                }

                else
                {
                    UsersRef.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                String senderProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                                String senderProfileName = dataSnapshot.child("Full_Name").getValue().toString();

                                HashMap messageNotifiationMap = new HashMap( );
                                messageNotifiationMap.put("date", saveCurrentDate);
                                messageNotifiationMap.put("time", saveCurrentTime);
                                messageNotifiationMap.put("profileimage", senderProfileImage);
                                messageNotifiationMap.put("fullname", senderProfileName);
                                messageNotifiationMap.put("receiverid", messageReceiverID);
                                messageNotifiationMap.put("senderid", messageSenderID);
                                messageNotifiationMap.put("usermessage", ImageMessageDetail);
                                messageNotifiationMap.put("notificationtype", "Message");

                                MessageNotificationRef.child(messageSenderID+saveCurrentDate+saveCurrentTime).updateChildren(messageNotifiationMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()){

                                                    Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                                                }
                                                else
                                                {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(ChatActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void DisplayReceiverInfo()
    {
        ReceiverName.setText(messageReceiverName);

        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    final String ProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(ProfileImage).placeholder(R.drawable.profile).into(ReceiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        publicControlReference.child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("hideIt").exists())
                    {
                        testIfTrue = (boolean) dataSnapshot.child("hideIt").getValue();

                        if (testIfTrue)
                        {
                            userLastSeen.setText(" ");
                        }
                        else
                            {
                                StateRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            final String type = dataSnapshot.child("type").getValue().toString();
                                            final String lastDate = dataSnapshot.child("date").getValue().toString();
                                            final String lastTime = dataSnapshot.child("time").getValue().toString();

                                            if (type.equals("online")) {
                                                userLastSeen.setText("Online");
                                            } else {
                                                userLastSeen.setText("Last seen: " + lastTime + "  " + lastDate);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                    }
                    else if(!dataSnapshot.child("hideIt").exists())
                    {
                        // The below code till the semicolon was after the String ProfileImage above in RootRef.
                        StateRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    final String type = dataSnapshot.child("type").getValue().toString();
                                    final String lastDate = dataSnapshot.child("date").getValue().toString();
                                    final String lastTime = dataSnapshot.child("time").getValue().toString();

                                    if (type.equals("online")) {
                                        userLastSeen.setText("Online");
                                    } else {
                                        userLastSeen.setText("Last seen: " + lastTime + "  " + lastDate);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                        {
                            Toast.makeText(ChatActivity.this, "Else Part: not known condition!", Toast.LENGTH_SHORT).show();
                        }
                }
                else
                    {
                        StateRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    final String type = dataSnapshot.child("type").getValue().toString();
                                    final String lastDate = dataSnapshot.child("date").getValue().toString();
                                    final String lastTime = dataSnapshot.child("time").getValue().toString();

                                    if (type.equals("online")) {
                                        userLastSeen.setText("Online");
                                    } else {
                                        userLastSeen.setText("Last seen: " + lastTime + "  " + lastDate);
                                    }
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

    private void IntializeFields()
    {
        ChattoolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChattoolBar);
        //not working
       // ChattoolBar.setTitleMarginStart(-8);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custome_bar, null);
        actionBar.setCustomView(action_bar_view);

        previewImage = (ImageView) findViewById(R.id.message_image_preview);
        closePreviewImageMessage = (ImageView) findViewById(R.id.close_preview_image_message);

        loadingBar = new ProgressDialog(this);
        sendImageButtton = (ImageView) findViewById(R.id.send_image_button);
        ReceiverProfileImage = (CircleImageView) findViewById(R.id.cutome_profile_image);
        ReceiverName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);

        messagesAdapter = new MessagesAdapter(messagesList, keysofmessage,messageReceiverID, messageReceiverName ,ChatActivity.this);

        userMessageInput = (EditText) findViewById(R.id.input_message);
        userMessageList = (RecyclerView) findViewById(R.id.message_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
       // userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);

        userMessageList.setAdapter(messagesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        managChatState("online");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        managChatState("online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        managChatState("offline");
    }

    @Override
    protected void onPause() {
        super.onPause();

        managChatState("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        managChatState("offline");
    }

    public void managChatState(String state)
    {

        HashMap chatMap = new HashMap();
        chatMap.put("chatStat", state);

        ChatRef.child(messageSenderID).updateChildren(chatMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

             //   Toast.makeText(ChatActivity.this, "Chat state updated", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
