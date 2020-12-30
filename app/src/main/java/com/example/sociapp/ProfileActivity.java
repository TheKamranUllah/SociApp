package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar myProfileToolbar;
    private TextView Username, UserProfName, Userdob, UserCountry, UserRelation, UserStatus, UserGender;
    private CircleImageView userProfileImage;

    private DatabaseReference UserProfileRef, FriendsRef, PostsRef,StatusRef;
    private FirebaseAuth mAuth;
    private Button myPosts, myFriends;
    private DatabaseReference publicControlReference;

    private String currentUserId, settingUserkey, loginUserId;
    private int CountFriends = 0, CountPost = 0, CountStatus = 0 ;
    private boolean hidepost, hidefriendlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       try {
           currentUserId = getIntent().getExtras().get("currentprofileid").toString();
       }
       catch (NullPointerException e){e.printStackTrace();}

        if (currentUserId == null)
        {
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
        }



        UserProfileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        publicControlReference = FirebaseDatabase.getInstance().getReference().child("PublicControls");

        myProfileToolbar = (Toolbar) findViewById(R.id.my_profile_bar_layout);
        setSupportActionBar(myProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        Username = (TextView) findViewById(R.id.my_profile_username);
        UserProfName = (TextView) findViewById(R.id.my_profile_full_name);
        Userdob = (TextView) findViewById(R.id.my_dob);
        UserCountry = (TextView) findViewById(R.id.my_country);
        UserRelation = (TextView) findViewById(R.id.my_relationship_status);
        UserStatus = (TextView) findViewById(R.id.my_profile_status);
        UserGender = (TextView) findViewById(R.id.my_gender);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);

        myPosts = (Button) findViewById(R.id.my_post_button);
        myFriends = (Button) findViewById(R.id.my_friends_button);

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    currentUserId = getIntent().getExtras().get("currentprofileid").toString();
                }
                catch (NullPointerException e){e.printStackTrace();}

                if (currentUserId == null)
                {
                    SendUserToFriendActivity();
                }
                else
                    {
                        Intent friendIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
                        friendIntent.putExtra("uservisitingid", currentUserId);
                        startActivity(friendIntent);
                    }


            }
        });

        publicControlReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    settingUserkey = dataSnapshot.getKey();

                     mAuth = FirebaseAuth.getInstance();
                    loginUserId = mAuth.getCurrentUser().getUid();

                    if (settingUserkey.equals(loginUserId))
                    {
                        ShowPostButton();
                    }
                    else
                        {
                            if (dataSnapshot.child("hidepostlist").exists())
                            {
                                hidepost = (boolean) dataSnapshot.child("hidepostlist").getValue();

                                if (hidepost)
                                {
                                    myPosts.setText("User posts are hidden");
                                    myPosts.setClickable(false);
                                }
                                else
                                {
                                    ShowPostButton();
                                }
                            }
                            else
                                {
                                    ShowPostButton();
                                }
                        }
                   }
                else
                    {
                        ShowPostButton();
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        publicControlReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    settingUserkey = dataSnapshot.getKey();
                    mAuth = FirebaseAuth.getInstance();
                    loginUserId = mAuth.getCurrentUser().getUid();

                 //   Toast.makeText(ProfileActivity.this, ""+settingUserkey, Toast.LENGTH_SHORT).show();

                    if (settingUserkey.equals(loginUserId))
                    {
                        showFriendList();
                    }
                    else
                        {
                            if (dataSnapshot.child("hidefriendlist").exists())
                            {
                                hidefriendlist = (boolean) dataSnapshot.child("hidefriendlist").getValue();

                                if (hidefriendlist)
                                {
                                    myFriends.setText("User friend list is hidden");
                                    myFriends.setClickable(false);
                                }
                                else
                                {
                                    showFriendList();
                                }
                            }
                            else
                                {
                                    showFriendList();
                                }
                        }
                }
                else
                {
                    showFriendList();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    currentUserId = getIntent().getExtras().get("currentprofileid").toString();
                }
                catch (NullPointerException e){e.printStackTrace();}

                if (currentUserId == null)
                {
                    SendUserToMyPostActivity();
                }
                else
                {
                    Intent friendIntent = new Intent(ProfileActivity.this, MyPostActivity.class);
                    friendIntent.putExtra("uservisitingidpost", currentUserId);
                    startActivity(friendIntent);
                }


            }
        });

        /*PostsRef.orderByChild("uid")
        .startAt(currentUserId).endAt(currentUserId + "\uf8ff")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if (dataSnapshot.exists())
                  {
                     CountPost = (int) dataSnapshot.getChildrenCount();
                     if (CountPost <= 1)
                     {
                         myPosts.setText(Integer.toString(CountPost) + " Post");
                     }
                     else
                         {
                         myPosts.setText(Integer.toString(CountPost) + " Posts");
                         }
                  }
                  else
                      {
                          myPosts.setText("0 Posts");
                      }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/


        /*FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    CountFriends = (int) dataSnapshot.getChildrenCount();
                    if (CountFriends <= 1)
                    {
                        myFriends.setText(Integer.toString(CountFriends) + " Friend");
                    }
                    else
                        {
                            myFriends.setText(Integer.toString(CountFriends) + " Friends");
                        }
                }
                else
                    {
                        myFriends.setText("0 Friends");
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });*/

        UserProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("User_Name").getValue().toString();
                    String myProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String myStatus = dataSnapshot.child("Status").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("RelationshipStatus").getValue().toString();
                    String myCountry = dataSnapshot.child("Country_Name").getValue().toString();
                    String myDob = dataSnapshot.child("Date_Of_Birth").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    Username.setText("@ " + myUserName);
                    UserProfName.setText(myProfileName);
                    UserStatus.setText(myStatus);
                    UserRelation.setText("Relationship: "+myRelationStatus);
                    UserCountry.setText("Country: " + myCountry);
                    Userdob.setText("Date Of Birth: "+myDob);
                    UserGender.setText("Gender "+myGender);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void ShowPostButton( )
    {
        PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            CountPost = (int) dataSnapshot.getChildrenCount();
                            if (CountPost <= 1)
                            {
                                myPosts.setText(Integer.toString(CountPost) + " Post");
                            }
                            else
                            {
                                myPosts.setText(Integer.toString(CountPost) + " Posts");
                            }
                        }
                        else
                        {
                            myPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void showFriendList( )
    {
        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    CountFriends = (int) dataSnapshot.getChildrenCount();
                    if (CountFriends <= 1)
                    {
                        myFriends.setText(Integer.toString(CountFriends) + " Friend");
                    }
                    else
                    {
                        myFriends.setText(Integer.toString(CountFriends) + " Friends");
                    }
                }
                else
                {
                    myFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToFriendActivity()
    {
        Intent loginIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToMyPostActivity()
    {
        Intent loginIntent = new Intent(ProfileActivity.this, MyPostActivity.class);
        startActivity(loginIntent);

    }
}
