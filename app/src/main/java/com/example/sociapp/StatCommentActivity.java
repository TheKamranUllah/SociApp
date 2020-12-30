package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class StatCommentActivity extends AppCompatActivity {

    private Toolbar mystatuscommentToolbar;
    private DatabaseReference UsersRef,StatsRef;
    private FirebaseAuth mAuth;

    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private List<Comments> AllUserCommentsList;
    private List<String> AllUserCommentsKey;

    private String Status_Key,current_user_id;
    private long countState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_comment);


        Status_Key = getIntent().getExtras().get("StatusKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StatsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Status_Key).child("Comments");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        AllUserCommentsList = new ArrayList<>();
        AllUserCommentsKey  = new ArrayList<>();

        mystatuscommentToolbar = (Toolbar) findViewById(R.id.my_status_comment_bar_layout);
        setSupportActionBar(mystatuscommentToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Status Comments");

        CommentsList = (RecyclerView) findViewById(R.id.stat_comments_List);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText) findViewById(R.id.stat_comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.stat_post_comment_button);


        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("User_Name").getValue().toString();
                            String ProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                            ValidateComments(userName, ProfileImage);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

//Counter doesn't even exit add if
        Query sortStatusCommentInDescendantOrder = StatsRef.orderByChild("counter");

        sortStatusCommentInDescendantOrder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                AllUserCommentsList.clear();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        AllUserCommentsKey.add(dataSnapshot1.getKey());

                        Comments comments = dataSnapshot1.getValue(Comments.class);
                        AllUserCommentsList.add(comments);
                    }

                    StatusCommentsAdapter commentsAdapter = new StatusCommentsAdapter(Status_Key, AllUserCommentsKey,AllUserCommentsList,StatCommentActivity.this);
                    CommentsList.setAdapter(commentsAdapter);
                    commentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void ValidateComments(String userName, String ProfileIMage)
    {

        StatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    countState = dataSnapshot.getChildrenCount();
                }
                else
                    {
                        countState = 0;
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write some comment!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();

            commentMap.put("uid", current_user_id);
            commentMap.put("profileimage", ProfileIMage);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", userName);

            StatsRef.child(RandomKey).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(StatCommentActivity.this, "Comment Added Successfully!", Toast.LENGTH_SHORT).show();
                                SelfIntent(Status_Key);
                            }

                            else
                            {
                                Toast.makeText(StatCommentActivity.this, "Error Occured Try Again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void SelfIntent(String PostKey)
    {
        Intent SelfIntent = new Intent(StatCommentActivity.this, StatCommentActivity.class);
        SelfIntent.putExtra("StatusKey",PostKey);
        startActivity(SelfIntent);
    }
}
