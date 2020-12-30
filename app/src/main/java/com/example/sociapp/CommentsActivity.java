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

public class CommentsActivity extends AppCompatActivity {

    private Toolbar mypostcommentToolbar;
    private DatabaseReference UsersRef,PostsRef;
    private FirebaseAuth mAuth;

    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText  CommentInputText;

    private List<Comments> AllUserCommentsList;
    private List<String> AllUserCommentKeys;

    private String Post_Key,current_user_id;
    private long countCmnts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        AllUserCommentsList = new ArrayList<>();
        AllUserCommentKeys  = new ArrayList<>();

        mypostcommentToolbar = (Toolbar) findViewById(R.id.my_comment_bar_layout);
        setSupportActionBar(mypostcommentToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Comments");

        CommentsList = (RecyclerView) findViewById(R.id.comments_List);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_comment_button);

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

        Query sortPostCommentInDescendantOrder = PostsRef.orderByChild("counter");

        sortPostCommentInDescendantOrder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllUserCommentsList.clear();
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        AllUserCommentKeys.add(dataSnapshot1.getKey());

                        Comments comments = dataSnapshot1.getValue(Comments.class);
                        AllUserCommentsList.add(comments);

                    }

                      CommentsAdapter commentsAdapter = new CommentsAdapter(Post_Key, AllUserCommentKeys, AllUserCommentsList, CommentsActivity.this);
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

        PostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countCmnts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countCmnts = 0;
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
                commentMap.put("counter", countCmnts);
                commentMap.put("username", userName);

                PostsRef.child(RandomKey).updateChildren(commentMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                           if (task.isSuccessful())
                           {
                               Toast.makeText(CommentsActivity.this, "Comment Added Successfully!", Toast.LENGTH_SHORT).show();
                               SelfIntent(Post_Key);
                           }

                           else
                               {
                                   Toast.makeText(CommentsActivity.this, "Error Occured Try Again!", Toast.LENGTH_SHORT).show();
                               }
                            }
                        });
            }
    }

    public void SelfIntent(String PostKey)
    {
        Intent SelfIntent = new Intent(CommentsActivity.this, CommentsActivity.class);
        SelfIntent.putExtra("PostKey",PostKey);
        startActivity(SelfIntent);
    }

}
