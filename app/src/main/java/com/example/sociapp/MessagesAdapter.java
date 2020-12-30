package com.example.sociapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private List<String> messageKeys;
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseRef, MessageSenderRef, MessageRecieverRef, publicControlReference;;
    private Context context;
    private String messageReceiverId, messageRecieverName, fontChangerUserId;
    private int chatFontSize;
    private String FontSize;

    public MessagesAdapter(List<Messages> userMessagesList, List<String> messagekeys,String ReceiverId,String messageRecieverName, Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.messageKeys = messagekeys;
        this.messageReceiverId = ReceiverId;
        this.messageRecieverName = messageRecieverName;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView SenderMessageText, ReceiverMessageText;
        public CircleImageView ReceiverProfileImage;
        public ImageView reciverMessageView, SenderMessageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            ReceiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            reciverMessageView = (ImageView) itemView.findViewById(R.id.image_message_reciever_view);
            SenderMessageView = (ImageView) itemView.findViewById(R.id.image_message_sender_view);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_user, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        final String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        final String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        publicControlReference = FirebaseDatabase.getInstance().getReference().child("PublicControls").child(messageSenderID);




        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.ReceiverProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {

            holder.ReceiverMessageText.setVisibility(View.GONE);
            holder.ReceiverProfileImage.setVisibility(View.GONE);
            holder.SenderMessageText.setVisibility(View.GONE);

            holder.SenderMessageView.setVisibility(View.GONE);
            holder.reciverMessageView.setVisibility(View.GONE);

            if (fromUserID.equals(messageSenderID)) {

                holder.SenderMessageText.setVisibility(View.VISIBLE);

                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderMessageText.setTextColor(Color.WHITE);
                holder.SenderMessageText.setGravity(Gravity.LEFT);
                holder.SenderMessageText.setText(messages.getMessage());

                publicControlReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("textsize").exists())
                            {
                                fontChangerUserId = dataSnapshot.getKey();
                                FontSize = dataSnapshot.child("textsize").getValue().toString();
                                chatFontSize = Integer.valueOf(FontSize);

                                if (fontChangerUserId.equals(messageSenderID)) {
                                    holder.SenderMessageText.setTextSize(chatFontSize);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.SenderMessageView.getLayoutParams().height = 0;
                holder.SenderMessageView.getLayoutParams().width = 0;

            } else {

                holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                holder.ReceiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                publicControlReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("textsize").exists())
                            {
                                fontChangerUserId = dataSnapshot.getKey();
                                FontSize = dataSnapshot.child("textsize").getValue().toString();
                                chatFontSize = Integer.valueOf(FontSize);

                                if (fontChangerUserId.equals(messageSenderID)) {
                                    holder.ReceiverMessageText.setTextSize(chatFontSize);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.ReceiverMessageText.setTextColor(Color.WHITE);
                holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                holder.ReceiverMessageText.setText(messages.getMessage());
                holder.reciverMessageView.getLayoutParams().height = 0;
                holder.reciverMessageView.getLayoutParams().width = 0;
            }
        }

       else if (fromMessageType.equals("image"))
       {

           holder.ReceiverMessageText.setVisibility(View.GONE);
           holder.ReceiverProfileImage.setVisibility(View.GONE);
           holder.SenderMessageText.setVisibility(View.GONE);

           holder.SenderMessageView.setVisibility(View.GONE);
           holder.reciverMessageView.setVisibility(View.GONE);

           if (fromUserID.equals(messageSenderID)) {

               holder.SenderMessageView.setVisibility(View.VISIBLE);
               holder.SenderMessageView.getLayoutParams().height = 450;
               holder.SenderMessageView.getLayoutParams().width = 450;
               Picasso.get().load(messages.getMessage()).placeholder(R.drawable.select_image).into(holder.SenderMessageView);

           } else {

               holder.ReceiverProfileImage.setVisibility(View.VISIBLE);
               holder.reciverMessageView.setVisibility(View.VISIBLE);
               holder.reciverMessageView.getLayoutParams().height = 450;
               holder.reciverMessageView.getLayoutParams().width = 450;
               holder.reciverMessageView.requestLayout();
               Picasso.get().load(messages.getMessage()).placeholder(R.drawable.select_image).into(holder.reciverMessageView);
           }



       } else
           {
               Toast.makeText(context, "Message Formate Not Known!", Toast.LENGTH_SHORT).show();
           }


        MessageRecieverRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageSenderID).child(messageReceiverId);
        MessageSenderRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageReceiverId).child(messageSenderID);

        final String messagekey = messageKeys.get(position);

        final View mview = LayoutInflater.from(context).inflate(R.layout.dialog_no_btn_layout, null);
        final TextView message1 = mview.findViewById(R.id.dialog_option_one);
        final TextView message2 = mview.findViewById(R.id.dialog_option_two);
        final ImageView imageView = mview.findViewById(R.id.dialog_Image);

    holder.reciverMessageView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //Toast.makeText(context, "You clicked on recieverMessageView", Toast.LENGTH_SHORT).show();

         Intent intent = new Intent(context, MessageImageFullActivity.class);
         intent.putExtra("reciever_id", messageReceiverId);
         intent.putExtra("image_message_key", messagekey);
        intent.putExtra("messageRecieverName", messageRecieverName);
         context.startActivity(intent);
    }
});

    holder.SenderMessageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           // Toast.makeText(context, "You clicked on SenderMessageView", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, MessageImageFullActivity.class);
            intent.putExtra("reciever_id", messageReceiverId);
            intent.putExtra("image_message_key", messagekey);
            intent.putExtra("messageRecieverName", messageRecieverName);
            context.startActivity(intent);

        }
    });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (messageSenderID.equals(fromUserID)) {

                    imageView.setImageResource(R.drawable.process);
                    final AlertDialog.Builder mbuilder = new AlertDialog.Builder(context, R.style.mydialog);
                    mbuilder.setView(mview);

                    final Dialog dialog = mbuilder.create();

                    message1.setText("Message Status");
                    message2.setText("Delete Message");

                    message1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            //The below code remove parentView the view we click once, if we don't add that the alert
                            //dialoge won't appear next time we click in chat activity and will throuw exception.
                            ((ViewGroup) mview.getParent()).removeView(mview);

                        }
                    });

                    message2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            MessageSenderRef.child(messagekey).removeValue();
                            MessageRecieverRef.child(messagekey).removeValue();
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(holder.getAdapterPosition(), userMessagesList.size());
                            dialog.dismiss();
                            notifyDataSetChanged();
                            Toast.makeText(context, "Message Deleted!", Toast.LENGTH_SHORT).show();
                            ((ViewGroup) mview.getParent()).removeView(mview);

                            getItemId(position);

                        }
                    });

                    try {
                        dialog.show();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    Toast.makeText(context, "You can't delete someone else message", Toast.LENGTH_SHORT).show();
                  }

                return true;
            }
        });

    }

//the below method is called inside deleted option declared above, and is intended to remove message on chat.
    @Override
    public long getItemId(int position) {

        try {

            userMessagesList.remove(position);
            notifyItemRemoved(position);

    }catch (IllegalStateException e){e.printStackTrace();}

        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }
}
