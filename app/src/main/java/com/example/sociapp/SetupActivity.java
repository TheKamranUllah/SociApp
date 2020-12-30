package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,OnOffStatuesRef;
    private StorageReference UserProfileImageRef;

    private  String currentUserId;
    final static int Gallery_Pick = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();

  try
  {
      currentUserId = mAuth.getCurrentUser().getUid();
  }
       catch (NullPointerException e)
       {
    Toast.makeText(this, "There is no user!", Toast.LENGTH_SHORT).show();
    new Exception().printStackTrace();
    new Error().printStackTrace();
     }


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
  //I created the below ref for UpdateUserStatusRef Only because using above one was creating another child.
        OnOffStatuesRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.save_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("profileimage") && dataSnapshot.hasChild("User_Name")
                   && dataSnapshot.hasChild("Full_Name") && dataSnapshot.hasChild("Country_Name"))
                {
                   // SendUserToMainActivity();
                    SendUserToLoginActivity();
                }
                else
                    {
                        View mview = LayoutInflater.from(SetupActivity.this).inflate(R.layout.dialog_layout, null);
                        TextView Message = mview.findViewById(R.id.dialog_text);
                        Button OkBtn = mview.findViewById(R.id.dialog_btn);

                        AlertDialog.Builder mbuilder = new AlertDialog.Builder(SetupActivity.this, R.style.mydialog);
                        mbuilder.setView(mview);
                        mbuilder.setCancelable(false);
                        String message = "You have to update, Profile Image,User Name,Full Name, and Country Name!";
                        Message.setText(message);

                        final Dialog dialog = mbuilder.create();
                        dialog.show();

                        OkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                            }
                        });
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });


           UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild("profileimage"))
                        {
                            String image = dataSnapshot.child("profileimage").getValue().toString().trim();
                            // Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                            Picasso.get().load(image).resize(200, 200)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.close)
                                    .into(ProfileImage, new com.squareup.picasso.Callback() {


                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(SetupActivity.this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                        }
                        else
                            {
                            Toast.makeText(SetupActivity.this, "Please select profile image first", Toast.LENGTH_SHORT).show();
                            }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
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
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(SetupActivity.this, "Image stored to Firebase Storage Successfully", Toast.LENGTH_SHORT).show();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this, "Image Stored to Firebase database successfully", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
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


    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(),"Please enter User_Name!",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(getApplicationContext(),"Please enter Full_Name!",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(country)){
            Toast.makeText(getApplicationContext(),"Please enter your Country Name!",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Saving Information!");
            loadingBar.setMessage("Please wait, Until we Save and Create you're account!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap( );
            userMap.put("User_Name", username);
            userMap.put("Full_Name", fullname);
            userMap.put("Country_Name", country);
            userMap.put("Status", "Hey there I am using SociApp!");
            userMap.put("Gender", "None");
            userMap.put("Date_Of_Birth", "None");
            userMap.put("RelationshipStatus", "None");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){

                        loadingBar.dismiss();
                        Toast.makeText(SetupActivity.this, "You're account is created successfully!", Toast.LENGTH_LONG).show();

                               /*  View mview = LayoutInflater.from(SetupActivity.this).inflate(R.layout.dialog_layout, null);
                                 TextView Message = mview.findViewById(R.id.dialog_text);
                                 Button OkBtn = mview.findViewById(R.id.dialog_btn);

                                 AlertDialog.Builder mbuilder = new AlertDialog.Builder(SetupActivity.this, R.style.mydialog);
                                 mbuilder.setView(mview);
                                 mbuilder.setCancelable(false);
                                 String message = "We sent you a confirmation email,check email and verify your account." +
                                         "if your loggin with google account simply press Ok";
                                 Message.setText(message);

                                 final Dialog dialog = mbuilder.create();
                                 dialog.show();

                                 OkBtn.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         dialog.dismiss();
                                         SendUserToLoginActivity( );
                                     }
                                 });*/
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured! "+message , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }
    }

    public void UpdateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        java.text.SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        try{
            OnOffStatuesRef.child(currentUserId).child("userState")
                    .updateChildren(currentStateMap);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

    }



    private void SendUserToLoginActivity() {
        Intent MainIntent = new Intent(SetupActivity.this, LoginActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToMainActivity()
    {
        Intent MainIntent = new Intent(SetupActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
    }


}
