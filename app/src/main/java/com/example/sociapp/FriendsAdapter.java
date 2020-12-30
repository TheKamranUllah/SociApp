package com.example.sociapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

  private  DatabaseReference UsersRef,StateRef;
  private  List<String> FreindkeyList;
  private   List<Friends> List;
  private   Context context;
  private     DatabaseReference publicControlReference;
  private     boolean testIfTrue;

    public FriendsAdapter(java.util.List<String> freindkeyList,List<Friends> list, Context context) {
        FreindkeyList = freindkeyList;
        List = list;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.all_user_display_layout, parent, false);
        return new FriendsAdapter.FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position) {

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StateRef = FirebaseDatabase.getInstance().getReference().child("UserState");
        publicControlReference = FirebaseDatabase.getInstance().getReference().child("PublicControls");
        final String userIDs = FreindkeyList.get(position);

        final Friends friends = List.get(position);

        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists())
    {

       final String userName = dataSnapshot.child("Full_Name").getValue().toString();
       final String userProfile = dataSnapshot.child("profileimage").getValue().toString();

        publicControlReference.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("hideIt").exists())
                    {
                        testIfTrue = (boolean) dataSnapshot.child("hideIt").getValue();

                        if (testIfTrue)
                        {
                            holder.onlineStatusView.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            //final String type;
                            StateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final String type;
                                    if (dataSnapshot.hasChild(userIDs))
                                    {
                                        // type = dataSnapshot.child("userState").child("type").getValue().toString();
                                        type = dataSnapshot.child(userIDs).child("type").getValue().toString();
                                        //Toast.makeText(context, ""+ type, Toast.LENGTH_SHORT).show();
                                        if (type.equals("online"))
                                        {
                                            holder.onlineStatusView.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            holder.onlineStatusView.setVisibility(View.INVISIBLE);
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
                        //final String type;
                        StateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String type;
                                if (dataSnapshot.hasChild(userIDs))
                                {
                                    // type = dataSnapshot.child("userState").child("type").getValue().toString();
                                    type = dataSnapshot.child(userIDs).child("type").getValue().toString();
                                    //Toast.makeText(context, ""+ type, Toast.LENGTH_SHORT).show();
                                    if (type.equals("online"))
                                    {
                                        holder.onlineStatusView.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        holder.onlineStatusView.setVisibility(View.INVISIBLE);
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
                        Toast.makeText(context, "Else Part in FriendsAdapter: not known condition!", Toast.LENGTH_SHORT).show();
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(holder.AllUserProfilePic);
        holder.AllUserFullName.setText(userName);
        holder.friendsDate.setText("Friends since: " + friends.getDate());

        final View mview = LayoutInflater.from(context).inflate(R.layout.dialog_no_btn_layout, null);
        final TextView message1 = mview.findViewById(R.id.dialog_option_one);
        final TextView message2 = mview.findViewById(R.id.dialog_option_two);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder mbuilder = new AlertDialog.Builder(context, R.style.mydialog);
                mbuilder.setView(mview);


                final Dialog dialog = mbuilder.create();
                message1.setText(userName+"'s Profile");
                message2.setText("Send Message");

               /* CharSequence options[] = new CharSequence[]
                        {
                                 userName + "'s Profile",
                                "Send Message"
                        };*/
               message1.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent profileIntent = new Intent(context, PersonProfileActivity.class);
                       profileIntent.putExtra("visit_user_id", userIDs);
                       context.startActivity(profileIntent);
                      ((ViewGroup) mview.getParent()).removeView(mview);
                      dialog.dismiss();
                   }
               });
               message2.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent ChatIntent = new Intent(context, ChatActivity.class);
                       ChatIntent.putExtra("visit_user_id", userIDs);
                       ChatIntent.putExtra("userName", userName);
                       context.startActivity(ChatIntent);
                       ((ViewGroup) mview.getParent()).removeView(mview);
                       dialog.dismiss();
                   }
               });

        /*        AlertDialog.Builder dialog = new AlertDialog.Builder(context,R.style.mydialog);
                dialog.setTitle("Select Option");

                dialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                              if (which == 0)
                              {
                                  Intent profileIntent = new Intent(context, PersonProfileActivity.class);
                                  profileIntent.putExtra("visit_user_id", userIDs);
                                  context.startActivity(profileIntent);
                              }

                              if (which == 1)
                              {
                                  Intent ChatIntent = new Intent(context, ChatActivity.class);
                                  ChatIntent.putExtra("visit_user_id", userIDs);
                                  ChatIntent.putExtra("userName", userName);
                                  context.startActivity(ChatIntent);
                              }
                    }
                });*/
           try {
               dialog.show();
           }
           catch (IllegalStateException e){e.printStackTrace();}
            }
        });
    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        if(List != null) {
            return List.size();
        }
        Toast.makeText(context, "List is null", Toast.LENGTH_SHORT).show();
        return 0;
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView AllUserProfilePic;
        TextView AllUserFullName;
        TextView friendsDate;
        ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            AllUserProfilePic = itemView.findViewById(R.id.all_user_post_image);
            AllUserFullName = itemView.findViewById(R.id.all_user_profile_full_name);
            friendsDate = itemView.findViewById(R.id.all_user_status);
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
        }
    }
}
