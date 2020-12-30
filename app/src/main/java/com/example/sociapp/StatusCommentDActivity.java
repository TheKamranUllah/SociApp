package com.example.sociapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusCommentDActivity extends AppCompatActivity {

    private DatabaseReference CommentDeletionRef;
    private FirebaseAuth mAuth;
    private String PostKey, currentUserID, CurrentCommentId, CommentUserId, UserComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_comment_d);


        PostKey = getIntent().getExtras().get("PostKey").toString();
        CurrentCommentId = getIntent().getExtras().get("CommentId").toString();
        CommentUserId = getIntent().getExtras().get("CommentUId").toString();
        UserComment = getIntent().getExtras().get("userComment").toString();

        CommentDeletionRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments")
                .child(CurrentCommentId);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        View view = LayoutInflater.from(StatusCommentDActivity.this).inflate(R.layout.dialog_two_btns_layout, null);
        android.app.AlertDialog.Builder mbuilder = new android.app.AlertDialog.Builder(StatusCommentDActivity.this, R.style.mydialog);
        mbuilder.setView(view);
        TextView AlertdialogText = view.findViewById(R.id.two_btns_dialog_text);
        Button OkButton = view.findViewById(R.id.ok_dialog_btn);
        Button CancelButton = view.findViewById(R.id.cancel_dialog_btn);
        AlertdialogText.setText("Choose what to do with your Comment!");
        CancelButton.setText("Edit");

        final Dialog dialog = mbuilder.create();
        dialog.setCancelable(false);
        dialog.show();

        OkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteCurrentPost(currentUserID, CommentUserId, PostKey);
            }
        });

        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditCurrentPost(UserComment, PostKey);
            }
        });
    }


    private void EditCurrentPost(String description, String postKey)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(StatusCommentDActivity.this);
        builder.setTitle("Edit Post!");

        final EditText inputField = new EditText(StatusCommentDActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CommentDeletionRef.child("comment").setValue(inputField.getText().toString());
                Toast.makeText(StatusCommentDActivity.this, "Comment Updated Successfully!", Toast.LENGTH_SHORT).show();
                SendUserToCommentActivity(PostKey);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                SendUserToCommentActivity(PostKey);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost(String currentUserID, String commentUserID, final String ThePostkey)
    {

        if (currentUserID.equals(commentUserID)) {

            View view = LayoutInflater.from(StatusCommentDActivity.this).inflate(R.layout.dialog_two_btns_layout, null);
            android.app.AlertDialog.Builder mbuilder = new android.app.AlertDialog.Builder(StatusCommentDActivity.this, R.style.mydialog);
            mbuilder.setView(view);
            TextView AlertdialogText = view.findViewById(R.id.two_btns_dialog_text);
            Button OkButton = view.findViewById(R.id.ok_dialog_btn);
            Button CancelButton = view.findViewById(R.id.cancel_dialog_btn);
            AlertdialogText.setText("Are you sure to delete your Comment!");
            CancelButton.setText("Cancel");

            final Dialog dialog = mbuilder.create();
            dialog.setCancelable(false);
            dialog.show();

            OkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentDeletionRef.removeValue();
                    SendUserToCommentActivity(ThePostkey);
                    Toast.makeText(StatusCommentDActivity.this, "Comment Deleted Successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            CancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(StatusCommentDActivity.this, "You Pressed Cancel!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    SendUserToCommentActivity(ThePostkey);
                }
            });
        }
        else {
            Toast.makeText(StatusCommentDActivity.this, "This is not your Comment!", Toast.LENGTH_SHORT).show();
        }

    }

    private void SendUserToCommentActivity(String Postkey) {

        Intent MainIntent = new Intent(StatusCommentDActivity.this, StatCommentActivity.class);
        MainIntent.putExtra("StatusKey",Postkey);
        //MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
