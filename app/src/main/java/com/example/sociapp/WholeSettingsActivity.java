package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class WholeSettingsActivity extends AppCompatActivity {

    private Toolbar Wtoolbar;
    private LinearLayout profileSetting, changePassword, publicControls, reportProblem, about, toGetUserInfo;

    private FirebaseAuth SAuth;
    private DatabaseReference WholeSettingUserRef;
    private String ScurrentUserId;

    private CircleImageView sUserProfileImage;
    private TextView sName, sDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whole_settings);

        Wtoolbar = (Toolbar) findViewById(R.id.whole_settings_bar);
        setSupportActionBar(Wtoolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SAuth = FirebaseAuth.getInstance();
        ScurrentUserId = SAuth.getCurrentUser().getUid();
        WholeSettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(ScurrentUserId);

        toGetUserInfo = (LinearLayout) findViewById(R.id.suser_name_description);
        profileSetting = (LinearLayout) findViewById(R.id.profile_setting);
        changePassword = (LinearLayout) findViewById(R.id.change_password);
        publicControls = (LinearLayout) findViewById(R.id.public_controls);
        reportProblem = (LinearLayout) findViewById(R.id.report_problem);
        about = (LinearLayout) findViewById(R.id.about);

        sUserProfileImage = (CircleImageView) findViewById(R.id.whol_setting_profile_image);
        sName = (TextView) toGetUserInfo.findViewById(R.id.whole_setting_user_name) ;
        sDescription = (TextView) toGetUserInfo.findViewById(R.id.whole_setting_user_description);


        profileSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToSettingsActivity();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(WholeSettingsActivity.this,ResetPasswordActivity.class));
            }
        });


        publicControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPublicControlsActivity();
            }
        });

        reportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPublicBugReportActivity();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 SendUserToAboutActivity();
            }
        });



        WholeSettingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    String sMyProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String sMyProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String sMyStatus = dataSnapshot.child("Status").getValue().toString();

                    Picasso.get().load(sMyProfileImage).placeholder(R.drawable.profile).into(sUserProfileImage);
                    sName.setText(sMyProfileName);
                    sDescription.setText(sMyStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSettingsActivity() {
        Intent loginIntent = new Intent(WholeSettingsActivity.this, SettingsActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToPublicControlsActivity() {
        Intent loginIntent = new Intent(WholeSettingsActivity.this, PublicControlsActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToPublicBugReportActivity() {
        Intent loginIntent = new Intent(WholeSettingsActivity.this, BugReportActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToAboutActivity() {
        Intent loginIntent = new Intent(WholeSettingsActivity.this, AboutActivity.class);
        startActivity(loginIntent);
    }
}
