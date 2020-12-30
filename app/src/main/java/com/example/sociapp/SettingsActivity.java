package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ProgressDialog loadingBar;

    private EditText Username, UserProfName, Userdob, UserCountry, UserRelation, UserStatus, UserGender;
    private Button UpdateAccountSettingButton, edit_button;
    private CircleImageView userProfileImage;

    private DatabaseReference SettingUserRef;
    private StorageReference UserProfileImageRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mtoolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mtoolbar);

        getSupportActionBar().setTitle("Profile Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);
        Username = (EditText) findViewById(R.id.settings_Username);
        UserProfName = (EditText) findViewById(R.id.settings_profile_full_name);
        Userdob = (EditText) findViewById(R.id.settings_dob);
        UserCountry = (EditText) findViewById(R.id.settings_country);
        UserRelation = (EditText) findViewById(R.id.settings_relationship_status);
        UserStatus = (EditText) findViewById(R.id.settings_status);
        UserGender = (EditText) findViewById(R.id.settings_gender);
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        edit_button = (Button) findViewById(R.id.edit_button);

        UpdateAccountSettingButton = (Button) findViewById(R.id.update_account_setting_button);

      SettingUserRef.addValueEventListener(new ValueEventListener() {
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
                  Username.setText(myUserName);
                  UserProfName.setText(myProfileName);
                  UserStatus.setText(myStatus);
                  UserRelation.setText(myRelationStatus);
                  UserCountry.setText(myCountry);
                  Userdob.setText(myDob);
                  UserGender.setText(myGender);
              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });

      //That below line of code keeps keypad hidden whenever the activity start as we have EditText in our activity.
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

      UpdateAccountSettingButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              ValidateAccountInfo( );
          }
      });

      userProfileImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v)
          {
              Intent galleryIntent = new Intent();
              galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
              galleryIntent.setType("image/*");
              startActivityForResult(galleryIntent, Gallery_Pick);
          }
      });


      userProfileImage.setEnabled(false);
      Username.setEnabled(false);
      UserGender.setEnabled(false);
      UserRelation.setEnabled(false);
      UserCountry.setEnabled(false);
      UserStatus.setEnabled(false);
      UserProfName.setEnabled(false);
      Userdob.setEnabled(false);

        Username.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        UserStatus.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        Userdob.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        UserProfName.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        UserCountry.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        UserRelation.setBackground(getResources().getDrawable(R.drawable.setting_bg));
        UserGender.setBackground(getResources().getDrawable(R.drawable.setting_bg));


        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userProfileImage.setEnabled(true);
                UserStatus.setEnabled(true);
                Username.setEnabled(true);
                UserGender.setEnabled(true);
                UserRelation.setEnabled(true);
                UserCountry.setEnabled(true);
                UserProfName.setEnabled(true);
                Userdob.setEnabled(true);

                edit_button.setBackgroundColor(getResources().getColor(R.color.chatbackgroundcolor));

                Username.setBackground(getResources().getDrawable(R.drawable.inputs));
                UserStatus.setBackground(getResources().getDrawable(R.drawable.inputs));
                Userdob.setBackground(getResources().getDrawable(R.drawable.inputs));
                UserProfName.setBackground(getResources().getDrawable(R.drawable.inputs));
                UserCountry.setBackground(getResources().getDrawable(R.drawable.inputs));
                UserRelation.setBackground(getResources().getDrawable(R.drawable.inputs));
                UserGender.setBackground(getResources().getDrawable(R.drawable.inputs));

                //below two lines of code hide the keypad or simply not letting keypad appear when activity is launched.

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(SettingsActivity.this, "Image stored to Firebase Storage Successfully", Toast.LENGTH_SHORT).show();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                SettingUserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SettingsActivity.this, "Image Stored to Firebase database successfully", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo()
    {
        String username = Username.getText().toString();
        String userfullname = UserProfName.getText().toString();
        String Dob = Userdob.getText().toString();
        String status = UserStatus.getText().toString();
        String country = UserCountry.getText().toString();
        String relationship = UserRelation.getText().toString();
        String gender = UserGender.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please Write Your Username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userfullname))
        {
            Toast.makeText(this, "Please Write Your Full Name", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(Dob))
        {
            Toast.makeText(this, "Please Write Your Date Of Birth", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please Write Your Status", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please Write Your Country Name", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(relationship))
        {
            Toast.makeText(this, "Please Provide Information About Your Relationship", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please Write Your Gender", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Updating Settings");
            loadingBar.setMessage("Please wait, while we updating your Settings...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdatAccountInfo(username, userfullname, Dob, status, country, relationship, gender);
        }

    }

    private void UpdatAccountInfo(String username, String userfullname, String dob, String status, String country, String relationship, String gender)
    {
        HashMap  hashMap = new HashMap( );

        hashMap.put("User_Name", username);
        hashMap.put("Full_Name", userfullname );
        hashMap.put("Date_Of_Birth", dob);
        hashMap.put("Status", status);
        hashMap.put("Country_Name", country);
        hashMap.put("RelationshipStatus", relationship);
        hashMap.put("Gender", gender);

        SettingUserRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                  if (task.isSuccessful())
                  {
                      SendUserToMainActivity();
                      Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully!", Toast.LENGTH_SHORT).show();
                      loadingBar.dismiss();
                  }
                  else
                      {
                          Toast.makeText(SettingsActivity.this, "Error Occured, While Updating Account Setting Info!", Toast.LENGTH_SHORT).show();
                          loadingBar.dismiss();
                      }
            }
        });
    }


    private void SendUserToMainActivity()
    {
        Intent MainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
