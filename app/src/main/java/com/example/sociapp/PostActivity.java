package com.example.sociapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
//import android.widget.Toolbar; it is not working instead of this the below imported class works fine it is for setSupportActionBar;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private Uri ImageUri;
    private  String Description;
    final static int Gallery_Pick = 1;

    private StorageReference PostsImagesReference;
    private DatabaseReference UsersRef,PostsRef, PhotoPostCountRef;
    private FirebaseAuth mAuth;
    String postImageUri;

    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;
    private long countPosts = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        PhotoPostCountRef = FirebaseDatabase.getInstance().getReference().child("PhotoPostCount");

        loadingBar = new ProgressDialog(this);

         mAuth = FirebaseAuth.getInstance();
         current_user_id = mAuth.getCurrentUser().getUid();

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);

        mtoolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mtoolbar);


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery( );
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Description = PostDescription.getText().toString().trim();

                if (ImageUri == null)
                {
                    Toast.makeText(PostActivity.this, "Please Select An Image!",Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(Description))
                {
                    Toast.makeText(PostActivity.this, "Please say something about your Post!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Add New Post");
                    loadingBar.setMessage("Please wait, while we updating your new Post...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);


                    PhotoPostCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {


                                countPosts = Integer.parseInt(dataSnapshot.child("counter").getValue().toString());

                                countPosts = countPosts + 1;
                                HashMap counterMap = new HashMap();
                                counterMap.put("counter", countPosts);
                                PhotoPostCountRef.updateChildren(counterMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                StoringImageToFirebaseStorage(countPosts);
                                            }
                                        });

                            }
                            else
                            {
                                HashMap counterMap = new HashMap();
                                counterMap.put("counter", countPosts);
                                PhotoPostCountRef.updateChildren(counterMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                StoringImageToFirebaseStorage(countPosts);
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }




            }
        });

    }


    private void StoringImageToFirebaseStorage(final long thecounter)
    {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

       filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               if (task.isSuccessful())
               {  filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {
                      // final String downloadUrl = uri.toString();
                       postImageUri = uri.toString();

                       Toast.makeText(PostActivity.this, "Image Uploaded Successfully To Storage!", Toast.LENGTH_SHORT).show();
                       SavingPostInformationToDatabase(thecounter,ImageUri.getLastPathSegment(), postRandomName+".jpg" );
                   }

               });
// The following code is not working properly to stored image Url in Firebase Realtime if you are using androidx.
// The above code works perfectly,filpath.getDownloadUrl is perfect choice. but creating child code must be inside filepath.getDown...

                  // String downloadUrl2 = task.getResult().getUploadSessionUri().toString();
                  // final String downloadUrl = filePath.getDownloadUrl().toString();
                  // postImageUri = downloadUrl;
                 // downloadUrl = task.getResult().getDownloadUrl().toString();
               }
               else
                   {
                       String message = task.getException().getMessage();
                       Toast.makeText(PostActivity.this, "Error! " + message, Toast.LENGTH_SHORT).show();
                   }
            }
        });
    }

    private void SavingPostInformationToDatabase(final long countPosts, final String fileName, final String RandomName)
    {



        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String userName = dataSnapshot.child("Full_Name").getValue().toString();
                    String userprofileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap( );
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage",postImageUri);
                    postsMap.put("profileimage", userprofileImage);
                    postsMap.put("fullname", userName);
                    postsMap.put("counter", countPosts);
                    postsMap.put("type", "Photo");
                    postsMap.put("storageName",fileName + RandomName);

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){

                                        Toast.makeText(PostActivity.this, "New Post Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        SendUserToMainActivity();
                                        loadingBar.dismiss();


                                    }
                                    else
                                    {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostActivity.this, "The task is cancelled!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){

            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            SendUserToMainActivity( );
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
