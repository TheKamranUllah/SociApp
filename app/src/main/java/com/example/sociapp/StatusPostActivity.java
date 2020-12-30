package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.TypeVariable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class StatusPostActivity extends AppCompatActivity {

    private ProgressDialog loadingBar;
    private Toolbar mtoolbar;
    private Spinner TextSize, TextColor, TextBackground;
    private EditText statusText;
    private Button addStatusPostBtn;
    private long textSize, textColor;
    private Bitmap textBackground;
    private String saveCurrentDate, saveCurrentTime, current_User_Id, statusRandomName;
    private String StatusText, backgroundURI;
    private ToggleButton ShowHideButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, SpostRef, StatusPostCountRef;
    private StorageReference SpostBg;
    private long countPosts = 0;
    private TextView sizeText, colorText, backgroungText;

    ArrayList<String> Arraylist1, Arraylist2, Arraylist3;
    ArrayAdapter<String> arrayAdapter1, arrayAdapter2, arrayAdapter3;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_post);

        mAuth = FirebaseAuth.getInstance();
        current_User_Id = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        SpostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        SpostBg = FirebaseStorage.getInstance().getReference();

        StatusPostCountRef = FirebaseDatabase.getInstance().getReference().child("PhotoPostCount");

        loadingBar = new ProgressDialog(this);

        sizeText = findViewById(R.id.size_text);
        colorText = findViewById(R.id.color_text);
        backgroungText = findViewById(R.id.background_text);

        InitializeFields();

        TextSize.setVisibility(View.GONE);
        TextColor.setVisibility(View.GONE);
        TextBackground.setVisibility(View.GONE);

        sizeText.setVisibility(View.GONE);
        colorText.setVisibility(View.GONE);
        backgroungText.setVisibility(View.GONE);

        TextSize.setClickable(false);
        TextColor.setClickable(false);
        TextBackground.setClickable(false);


        ShowHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOn = ((ToggleButton)v).isChecked();

                if (isOn)
                {
               TextSize.setVisibility(View.VISIBLE);
               TextColor.setVisibility(View.VISIBLE);
               TextBackground.setVisibility(View.VISIBLE);

                    sizeText.setVisibility(View.VISIBLE);
                    colorText.setVisibility(View.VISIBLE);
                    backgroungText.setVisibility(View.VISIBLE);

                }
                else
                    {
                        TextSize.setVisibility(View.GONE);
                        TextColor.setVisibility(View.GONE);
                        TextBackground.setVisibility(View.GONE);

                        TextSize.setClickable(false);
                        TextColor.setClickable(false);
                        TextBackground.setClickable(false);

                        sizeText.setVisibility(View.GONE);
                        colorText.setVisibility(View.GONE);
                        backgroungText.setVisibility(View.GONE);
                    }
            }
        });

        Arraylist1 = new ArrayList<>();
        Arraylist1.add("10pt");
        Arraylist1.add("15pt");
        Arraylist1.add("20pt");
        Arraylist1.add("25pt");
        Arraylist1.add("30pt");
        Arraylist1.add("35pt");
        Arraylist1.add("40pt");
        Arraylist1.add("45pt");
        arrayAdapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arraylist1);
        TextSize.setAdapter(arrayAdapter1);

        TextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text10sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text10sp));
                } else if (position == 1) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text10sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text15sp));
                } else if (position == 2) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text15sp));
                    textSize = (long) (getResources().getDimension(R.dimen.text20sp));
                } else if (position == 3) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text20sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text25sp));
                } else if (position == 4) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text25sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text30sp));
                } else if (position == 5) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text30sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text35sp));
                } else if (position == 6) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text35sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text40sp));
                } else if (position == 7) {
                    statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text40sp));
                    textSize = (int) (getResources().getDimension(R.dimen.text45sp));
                } else {
                    Toast.makeText(StatusPostActivity.this, "Nothing is selected.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                Toast.makeText(StatusPostActivity.this, "Nothing is selected.", Toast.LENGTH_SHORT).show();
            }
        });

        Arraylist2 = new ArrayList<>();
        Arraylist2.add("Black");
        Arraylist2.add("Blue");
        Arraylist2.add("Green");
        Arraylist2.add("Yellow");
        Arraylist2.add("Red");
        Arraylist2.add("Purple");
        Arraylist2.add("Pink");
        Arraylist2.add("Violet");
        Arraylist2.add("Navy");
        Arraylist2.add("white");

        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arraylist2);
        TextColor.setAdapter(arrayAdapter2);

        TextColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        statusText.setTextColor(getResources().getColor(R.color.black));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.black, null);
                        //  statusText.setTextColor(this);
                        break; // optional

                    case 1:

                        statusText.setTextColor(getResources().getColor(R.color.blue));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.blue, null);
                        break;

                    case 2:

                        statusText.setTextColor(getResources().getColor(R.color.green));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.green, null);
                        break;

                    case 3:

                        statusText.setTextColor(getResources().getColor(R.color.yellow));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.yellow, null);
                        break;

                    case 4:

                        statusText.setTextColor(getResources().getColor(R.color.red));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.red, null);
                        break;

                    case 5:

                        statusText.setTextColor(getResources().getColor(R.color.purple));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.purple, null);
                        break;

                    case 6:

                        statusText.setTextColor(getResources().getColor(R.color.pink));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.pink, null);
                        break;

                    case 7:

                        statusText.setTextColor(getResources().getColor(R.color.violet));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.violet, null);
                        break;
                    case 8:

                        statusText.setTextColor(getResources().getColor(R.color.navy));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.navy, null);
                        break;

                    case 9:

                        statusText.setTextColor(getResources().getColor(R.color.white));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.white, null);
                        break;

                    default: // Optional
                        // Statements
                        statusText.setTextColor(getResources().getColor(R.color.black));
                        textColor = ResourcesCompat.getColor(getResources(), R.color.black, null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Arraylist3 = new ArrayList<>();
        Arraylist3.add("Bg1");
        Arraylist3.add("Bg2");
        Arraylist3.add("Bg3");
        Arraylist3.add("Bg4");
        Arraylist3.add("Bg5");
        Arraylist3.add("Bg6");
        Arraylist3.add("Bg7");
        Arraylist3.add("Bg8");
        Arraylist3.add("Bg9");
        Arraylist3.add("Bg10");
        arrayAdapter3 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arraylist3);
        TextBackground.setAdapter(arrayAdapter3);

        TextBackground.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        statusText.setBackground(getDrawable(R.drawable.backgone));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgone, null)).getBitmap();
                        break;

                    case 1:

                        statusText.setBackground(getDrawable(R.drawable.backgtwo));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgtwo, null)).getBitmap();
                        break;

                    case 2:

                        statusText.setBackground(getDrawable(R.drawable.backgthree));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgthree, null)).getBitmap();
                        break;

                    case 3:

                        statusText.setBackground(getDrawable(R.drawable.backgfour));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgfour, null)).getBitmap();
                        break;

                    case 4:

                        statusText.setBackground(getDrawable(R.drawable.backgfive));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgfive, null)).getBitmap();
                        break;

                    case 5:

                        statusText.setBackground(getDrawable(R.drawable.backgsix));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgsix, null)).getBitmap();
                        break;

                    case 6:

                        statusText.setBackground(getDrawable(R.drawable.backgseven));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgseven, null)).getBitmap();
                        break;

                    case 7:

                        statusText.setBackground(getDrawable(R.drawable.backgeight));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgeight, null)).getBitmap();
                        break;
                    case 8:

                        statusText.setBackground(getDrawable(R.drawable.backgnin));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgnin, null)).getBitmap();
                        break;

                    case 9:

                        statusText.setBackground(getDrawable(R.drawable.backgten));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgten, null)).getBitmap();
                        break;

                    default:

                        statusText.setBackground(getDrawable(R.drawable.backgone));
                        textBackground = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.backgone, null)).getBitmap();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addStatusPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StatusText = statusText.getText().toString().trim();
                // Toast.makeText(context, ""+textBackground, Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(StatusText)) {
                    Toast.makeText(StatusPostActivity.this, "Please write some status!", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Add New Status");
                    loadingBar.setMessage("Please wait, while we are updating your new Status...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    StatusPostCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                countPosts = Integer.parseInt(dataSnapshot.child("counter").getValue().toString());

                                countPosts = countPosts + 1;
                                HashMap counterMap = new HashMap();
                                counterMap.put("counter", countPosts);
                                StatusPostCountRef.updateChildren(counterMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                StoringBgToFirebaseStorage(countPosts);
                                               // Toast.makeText(context, "" + countPosts, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                HashMap counterMap = new HashMap();
                                counterMap.put("counter", countPosts);
                                StatusPostCountRef.updateChildren(counterMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                StoringBgToFirebaseStorage(countPosts);
                                                //Toast.makeText(context, "" + countPosts, Toast.LENGTH_SHORT).show();
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



    private void StoringBgToFirebaseStorage(final long counter1) {

        final StorageReference filePath = SpostBg.child("Status Backgrounds").child(statusRandomName + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

         textBackground.compress(Bitmap.CompressFormat.JPEG, 100, baos);


        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = filePath.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Text background is not updated!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        backgroundURI = uri.toString();
                        //Toast.makeText(context, "TextBackground Updated! "+backgroundURI, Toast.LENGTH_SHORT).show();
                        SavingStatusPostInfo(counter1,statusRandomName + ".jpg");
                    }
                });
            }
        });
    }

    private void SavingStatusPostInfo(final long countPosts, final String StatusBgName)
    {


        UsersRef.child(current_User_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String userName = dataSnapshot.child("Full_Name").getValue().toString();
                    String userprofileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap statusMap = new HashMap( );
                    statusMap.put("uid", current_User_Id);
                    statusMap.put("date", saveCurrentDate);
                    statusMap.put("time", saveCurrentTime);
                    statusMap.put("userstatus", StatusText);
                    statusMap.put("textcolor",textColor);
                    statusMap.put("textsize",textSize);
                    statusMap.put("backgrounduri",backgroundURI);
                    statusMap.put("profileimage", userprofileImage);
                    statusMap.put("fullname", userName);
                    statusMap.put("counter", countPosts);
                    statusMap.put("type", "Text");
                    statusMap.put("statusBg", StatusBgName);

                    SpostRef.child(current_User_Id + statusRandomName).updateChildren(statusMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful())
                            {
                                Toast.makeText(StatusPostActivity.this, "New Status Updated Successfully!", Toast.LENGTH_SHORT).show();
                                SendUserToLoadStatusActivity();
                                loadingBar.dismiss();
                            }
                            else
                                {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(StatusPostActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(StatusPostActivity.this, "The task is cancelled!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeFields()
        {
            context = StatusPostActivity.this;

            mtoolbar = findViewById(R.id.update_post_status_page_toolbar);
            setSupportActionBar(mtoolbar);

            TextSize = (Spinner) findViewById(R.id.text_size);
            TextColor = (Spinner) findViewById(R.id.text_color);
            TextBackground = (Spinner) findViewById(R.id.text_background);
            addStatusPostBtn = (Button) findViewById(R.id.update_status_post_button);
            statusText = (EditText) findViewById(R.id.status_text);
            ShowHideButton = (ToggleButton) findViewById(R.id.show_off_button);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Update Status");

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            statusRandomName = saveCurrentDate + saveCurrentTime;

        }

    private void SendUserToLoadStatusActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    }


