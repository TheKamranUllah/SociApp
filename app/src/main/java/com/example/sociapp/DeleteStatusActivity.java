package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeleteStatusActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private DatabaseReference ClickStatusRef;
    private FirebaseAuth mAuth;
    private StorageReference statusBg;

    private CircleImageView Profileimage;
    private TextView FullName;
    private TextView Date,Time;
    private TextView UserStatus;
    private Button DeleteBtn, UpdateBtn;
    String StatusKey,currentUserID,statusUserID;
    String profileImage, fullName, date, time, userStatus, statusbgUri, statusBgPath;
    long statusColor, statusTextSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_status);

        Profileimage = findViewById(R.id.delete_status_profile_image);
        FullName = findViewById(R.id.delete_status_user_name);
        Date = findViewById(R.id.delete_status_date);
        Time = findViewById(R.id.delete_status_time);
        UserStatus = findViewById(R.id.delete_all_user_status);
        DeleteBtn = findViewById(R.id.status_delete_button);
        UpdateBtn = findViewById(R.id.status_update_button);

        mtoolbar = findViewById(R.id.delete_status_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Status Detail");

        DeleteBtn.setVisibility(View.INVISIBLE);
        UpdateBtn.setVisibility(View.INVISIBLE);

        statusBg = FirebaseStorage.getInstance().getReference().child("Status Backgrounds");
        StatusKey = getIntent().getExtras().get("StatusKey").toString();
        ClickStatusRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(StatusKey);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

       // Toast.makeText(this, ""+StatusKey, Toast.LENGTH_SHORT).show();

        ClickStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    statusUserID = dataSnapshot.child("uid").getValue().toString();
                    profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    fullName = dataSnapshot.child("fullname").getValue().toString();
                    date = dataSnapshot.child("date").getValue().toString();
                    time = dataSnapshot.child("time").getValue().toString();
                    userStatus = dataSnapshot.child("userstatus").getValue().toString();
                    statusbgUri = dataSnapshot.child("backgrounduri").getValue().toString();
                    statusBgPath = dataSnapshot.child("statusBg").getValue().toString();

                    String statuscolor = dataSnapshot.child("textcolor").getValue().toString();
                    String statusize= dataSnapshot.child("textsize").getValue().toString();

                    statusColor = Integer.parseInt(statuscolor);
                    statusTextSize = Integer.parseInt(statusize);


                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(Profileimage);
                    FullName.setText(fullName);
                    Date.setText(date);
                    Time.setText(time);
                    UserStatus.setText(userStatus);
                   // UserStatus.setTextSize(statusTextSize);
                   // UserStatus.setTextColor((int) statusColor);

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try {
                        URL url = new URL(statusbgUri);
                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        Drawable dr = new BitmapDrawable(image);
                        UserStatus.setBackgroundDrawable(dr);
                    } catch(IOException e) {
                        System.out.println(e);
                    }

                    int status_color = (int) statusColor;
                    UserStatus.setTextColor(status_color);
                    int Text_Size = (int) statusTextSize/ 3 ;
                    UserStatus.setTextSize(Text_Size);


                   // Toast.makeText(DeleteStatusActivity.this, ""+statusUserID, Toast.LENGTH_SHORT).show();

                    if (currentUserID.equals(statusUserID))
                    {
                        DeleteBtn.setVisibility(View.VISIBLE);
                        UpdateBtn.setVisibility(View.VISIBLE);
                    }

                    DeleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            StatusDeleteMethod(statusBgPath);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        UpdateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                EditCurrentPost(userStatus);
                Toast.makeText(DeleteStatusActivity.this, "You just clicked the update button", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void StatusDeleteMethod(final String BgPath)
    {

        if (currentUserID.equals(statusUserID)) {

            View view = LayoutInflater.from(DeleteStatusActivity.this).inflate(R.layout.dialog_two_btns_layout, null);
            AlertDialog.Builder mbuilder = new AlertDialog.Builder(DeleteStatusActivity.this, R.style.mydialog);
            mbuilder.setView(view);
            TextView AlertdialogText = view.findViewById(R.id.two_btns_dialog_text);
            Button OkButton = view.findViewById(R.id.ok_dialog_btn);
            Button CancelButton = view.findViewById(R.id.cancel_dialog_btn);
            AlertdialogText.setText("Do you want to delete your status!");

            final Dialog dialog = mbuilder.create();
            dialog.setCancelable(false);
            dialog.show();

            OkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DeleteStatusActivity.this, "You Pressed OK!", Toast.LENGTH_SHORT).show();
                    SendUserToLoadStatusActivity( );

                    statusBg.child(BgPath).delete();
                    ClickStatusRef.removeValue();
                    dialog.dismiss();
                }
            });

            CancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DeleteStatusActivity.this, "You Pressed Cancel!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
        else {
            Toast.makeText(DeleteStatusActivity.this, "This is not your status!", Toast.LENGTH_SHORT).show();
        }
    }

    private void EditCurrentPost(String userStatus)
    {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(DeleteStatusActivity.this);
        builder.setTitle("Edit Status!");

        final EditText inputField = new EditText(DeleteStatusActivity.this);
        inputField.setText(userStatus);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ClickStatusRef.child("userstatus").setValue(inputField.getText().toString());
                Toast.makeText(DeleteStatusActivity.this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show();
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

    private void SendUserToLoadStatusActivity()
    {
        Intent MainIntent = new Intent(DeleteStatusActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
