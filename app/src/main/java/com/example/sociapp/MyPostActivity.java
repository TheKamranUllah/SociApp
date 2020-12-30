package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPostActivity extends AppCompatActivity {

  private   Toolbar mypostToolbar;
  private RecyclerView myPostList;
  private DatabaseReference ProfilePostsRef, UsersRef,LikesRef;
  private FirebaseAuth mAuth;
  private String currentUserId;

  private List<Posts> ProfilePostList = new ArrayList<>();
  private final List<String> ProfileKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);


        try
        {
            currentUserId = getIntent().getExtras().get("uservisitingidpost").toString();
        }
        catch (NullPointerException e)
             {e.printStackTrace();}

        if (currentUserId == null)
        {
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        ProfilePostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mypostToolbar = (Toolbar) findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mypostToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostList = (RecyclerView) findViewById(R.id.my_post_layout);

        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);


        DisplayMyAllPosts ( );

    }

    private void DisplayMyAllPosts()
    {
        Query myPostQuary = ProfilePostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff");

        myPostQuary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ProfilePostList.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        ProfileKeys.add(dataSnapshot1.getKey());

                        Posts posts = dataSnapshot1.getValue(Posts.class);
                        ProfilePostList.add(posts);

                    }
                    PostAdapter profilePostAdapter = new PostAdapter(ProfilePostList,ProfileKeys,MyPostActivity.this);
                    myPostList.setAdapter(profilePostAdapter);

                }
                else {

                    Toast.makeText(MyPostActivity.this, "There is no post Exits! "+ DatabaseError.PERMISSION_DENIED, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
