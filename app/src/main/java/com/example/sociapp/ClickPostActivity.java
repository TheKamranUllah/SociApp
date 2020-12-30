package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class ClickPostActivity extends AppCompatActivity {

    Toolbar mtoolbar;
    ImageView PostImage;
    TextView PostDescription;
    Button  EditPostButton, DeletePostButton;

    private DatabaseReference ClickPostRef;
    private StorageReference ImageRef;
    private FirebaseAuth mAuth;
    private String PostKey, currentUserID, PostUserID, description, postimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mtoolbar = (Toolbar) findViewById(R.id.click_activity_status_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Detail");

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        ImageRef = FirebaseStorage.getInstance().getReference().child("Post Images");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostImage = (ImageView) findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);

        EditPostButton.setVisibility(View.INVISIBLE);
        DeletePostButton.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    description = dataSnapshot.child("description").getValue().toString();
                    postimage = dataSnapshot.child("postimage").getValue().toString();
                    PostUserID = dataSnapshot.child("uid").getValue().toString();
                final String ImagePath = dataSnapshot.child("storageName").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.get().load(postimage).into(PostImage);

                    if (currentUserID.equals(PostUserID))
                    {

                        EditPostButton.setVisibility(View.VISIBLE);
                        DeletePostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            EditCurrentPost(description);
                        }
                    });

                    DeletePostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            DeleteCurrentPost(ImagePath, currentUserID, PostUserID);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void EditCurrentPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post!");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost(final String ImagePath, String currentUserID, String postUserID)
    {

        if (currentUserID.equals(postUserID)) {

            View view = LayoutInflater.from(ClickPostActivity.this).inflate(R.layout.dialog_two_btns_layout, null);
            android.app.AlertDialog.Builder mbuilder = new android.app.AlertDialog.Builder(ClickPostActivity.this, R.style.mydialog);
            mbuilder.setView(view);
            TextView AlertdialogText = view.findViewById(R.id.two_btns_dialog_text);
            Button OkButton = view.findViewById(R.id.ok_dialog_btn);
            Button CancelButton = view.findViewById(R.id.cancel_dialog_btn);
            AlertdialogText.setText("Do you want to delete your Post!");

            final Dialog dialog = mbuilder.create();
            dialog.setCancelable(false);
            dialog.show();

            OkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickPostRef.removeValue();
                    ImageRef.child(ImagePath).delete();
                    SendUserToMainActivity();
                    Toast.makeText(ClickPostActivity.this, "Post Deleted Successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            CancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ClickPostActivity.this, "You Pressed Cancel!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
        else {
            Toast.makeText(ClickPostActivity.this, "This is not your Post!", Toast.LENGTH_SHORT).show();
        }

    }

    private void SendUserToMainActivity() {

        Intent MainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
