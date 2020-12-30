package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

//import static com.example.sociapp.NotificationsActivity.PREFS_NAME;

public class MainActivity extends AppCompatActivity {

    private TextView UserStatusButton, noInternetMessagText;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private ProgressBar progressCircular;

    private List<Posts> mUserPost;
    private List<String> Keys;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private TextView AddNewPostButton;
    private ImageView Status_icon;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef,UserStateRef,StatusRef;

    private DatabaseReference StateRef;
    private String currentUserID;
    private Boolean emailAddressChecker;
   // private int ChangeIcon;

    private DatabaseReference databaseRef, MessageNotificationRef;
    private ValueEventListener valueEventListener, valueEventListener1;

    private boolean wificonnection = false;
    private boolean mobileconnection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
                  super.onCreate(savedInstanceState);
                 setContentView(R.layout.activity_main);

                 //this below method detect mobile data and wifi and also if mobile is not connected to any network.
                 isInternetConnection( );

                 //below method of code is detecting whether the app is conneted to firebase or not.
                detectingConnectionState( );


             mAuth = FirebaseAuth.getInstance();
            currentUserID = mAuth.getCurrentUser().getUid();

        //This below code is very important it writes to firebase node, when we are disconnected.

        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference().child("UserState").child(currentUserID)
                .child("type");
// Write a string when this client loses connection
        presenceRef.onDisconnect().setValue("offline");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        UserStateRef = FirebaseDatabase.getInstance().getReference().child("UserState");

        UserStatusButton = findViewById(R.id.status_post_btn);
        UserStatusButton.setVisibility(View.INVISIBLE);

        noInternetMessagText = (TextView) findViewById(R.id.no_internet_message);
        noInternetMessagText.setVisibility(View.GONE);
        noInternetMessagText.setClickable(false);

        //Below 4 lines of code is for getting Friend Request Notification ReceiverId
        this.databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Notifications");
        //lets initialize the valueEventListener
        this.valueEventListener = getPostInformation();
        //now lets attach the valueEventListener
        this.databaseRef.addValueEventListener(valueEventListener);

        //Below 4 lines of code is for getting messagee notification Info
        this.MessageNotificationRef = FirebaseDatabase.getInstance().getReference()
                .child("MessagesNotification");
        this.valueEventListener1 = getMessagInfo( );
        this.MessageNotificationRef.addValueEventListener(valueEventListener1);


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //below line of code isn't working set it from xml by inserting below code in the toolbar layout
       // app:contentInsetLeft="0dp"
       // app:contentInsetStart="0dp"
       // app:contentInsetStartWithNavigation="0dp"
       // mToolbar.setTitleMarginStart(-8);
        progressCircular = (ProgressBar) findViewById(R.id.progress_circular);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AddNewPostButton = (TextView) findViewById(R.id.add_new_post_button);
        Status_icon = (ImageView) findViewById(R.id.status_icon);

// this code was to test that the url in realtime from storage is correct or not it can be further used for such testing.
        //  String imageUri = "com.google.android.gms.tasks.zzu@60466f0";
//        ImageView ivBasicImage = (ImageView) findViewById(R.id.image);
//        Picasso.get().load(imageUri).into(ivBasicImage);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUserPost = new ArrayList();
        Keys = new ArrayList<>();
        //recycleview stuff
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);


        try {
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
            NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
            NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);
        } catch (InflateException e){e.printStackTrace();}

        UserStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToStatusPostActivity( );
            }
        });

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Validation to check the User name if doesn't exists logout user remove his statenode.
                    if (dataSnapshot.hasChild("Full_Name")) {

                       try {
                           String fullname = dataSnapshot.child("Full_Name").getValue().toString();
                           NavProfileUserName.setText(fullname);
                       }
                       catch (NullPointerException e){e.printStackTrace();}
                    }

                    else {


                        UserStateRef.child(currentUserID).removeValue();
                        View mview = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout, null);
                        TextView Message = mview.findViewById(R.id.dialog_text);
                        Button OkBtn = mview.findViewById(R.id.dialog_btn);

                        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MainActivity.this, R.style.mydialog);
                        mbuilder.setView(mview);
                        mbuilder.setCancelable(false);
                        String message = "You did not uploaded your picture and Can't access further!";
                        Message.setText(message);

                        final Dialog dialog = mbuilder.create();
                        dialog.show();

                        OkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                SendUserToLoginActivity();
                                mAuth.signOut();
                            }
                        });

                    }

                    // Validation to check the Profile Image if doesn't exists logout user remove his statenode.
                    if (dataSnapshot.hasChild("profileimage")) {

                       try {
                           String image = dataSnapshot.child("profileimage").getValue().toString();
                           Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                          }
                       catch (NullPointerException e){e.printStackTrace();}
                    } else {


                        UserStateRef.child(currentUserID).removeValue();
                        View mview = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout, null);
                        TextView Message = mview.findViewById(R.id.dialog_text);
                        Button OkBtn = mview.findViewById(R.id.dialog_btn);

                        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MainActivity.this, R.style.mydialog);
                        mbuilder.setView(mview);
                        mbuilder.setCancelable(false);
                        String message = "You did not uploaded your picture and Info Can't access further!";
                        Message.setText(message);

                        final Dialog dialog = mbuilder.create();
                        dialog.show();

                        OkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                SendUserToLoginActivity();
                                mAuth.signOut();
                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        Status_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPostActivity();
            }
        });

        VerifyEmailAddress( );

    }

    class MyAsyncTask extends AsyncTask<List<Posts>, Void, Boolean>
    {


        @Override
        protected Boolean doInBackground(List<Posts>... posts) {


      try {
          MyApplication myApplicationOnOffStatus = new MyApplication();
          myApplicationOnOffStatus.onCreate();
          myApplicationOnOffStatus.onAppBackgrounded();
          myApplicationOnOffStatus.onAppBackgrounded2();
          myApplicationOnOffStatus.onAppForegrounded();
          myApplicationOnOffStatus.onAppForegrounded1();
      } catch (NullPointerException e){e.printStackTrace();}

            Query sortPostInDescendantOrder = PostsRef.orderByChild("counter");

            sortPostInDescendantOrder.addValueEventListener(new ValueEventListener() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mUserPost.clear();

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                            Keys.add(dataSnapshot1.getKey());

                            Posts posts = dataSnapshot1.getValue(Posts.class);
                            mUserPost.add(posts);
                        }


                          PostAdapter postAdapter = new PostAdapter(mUserPost, Keys, MainActivity.this);
                          postList.setAdapter(postAdapter);
                          progressCircular.setVisibility(View.INVISIBLE);
                          UserStatusButton.setVisibility(View.VISIBLE);

                    }
                        else {

                          Toast.makeText(MainActivity.this, "There is no post Exists! " + DatabaseError.PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
                             progressCircular.setVisibility(View.INVISIBLE);
                             UserStatusButton.setVisibility(View.VISIBLE);
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success)
            {
                //Toast.makeText(MainActivity.this, ""+ Keys, Toast.LENGTH_SHORT).show();
                //PostAdapter postAdapter = new PostAdapter(mUserPost, Keys, MainActivity.this);
               // postList.setAdapter(postAdapter);
               // progressCircular.setVisibility(View.INVISIBLE);
               // UserStatusButton.setVisibility(View.VISIBLE);
            }
            else
            {
              //  Toast.makeText(MainActivity.this, "There is no post Exists! " + DatabaseError.PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
              //  progressCircular.setVisibility(View.INVISIBLE);
               // UserStatusButton.setVisibility(View.VISIBLE);
            }
        }
    }


    private ValueEventListener getMessagInfo()
    {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren())
                    {
                        if (snapshot1 != null)
                        {
                            NotificationModel notificationMode2 = snapshot1.getValue(NotificationModel.class);

                            if (notificationMode2 != null)
                            {
                                String messageReceiverID = notificationMode2.getReceiverid();
                                notificationIconSetting(messageReceiverID, currentUserID);
                               // Toast.makeText(MainActivity.this, ""+messageReceiverID, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener getPostInformation() {

       return new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               if (dataSnapshot.exists())
               {
                   for(DataSnapshot snapshot: dataSnapshot.getChildren())
                   {
                       if (snapshot != null)
                       {
                           NotificationModel notificationModel = snapshot.getValue(NotificationModel.class);

                           if (notificationModel != null)
                           {
                              String ReceiverId = notificationModel.getReceiverid();
                               notificationIconSetting(ReceiverId, currentUserID);
                               //Toast.makeText(MainActivity.this, ""+ReceiverId, Toast.LENGTH_LONG).show();
                           }

                       }
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }

       };

    }

    private void notificationIconSetting( String RId, String CUserId )
    {
       if (RId.equals(CUserId)) {

           navigationView.getMenu().getItem(2).setIcon(R.drawable.notificationalert);
           navigationView.setItemIconTintList(null);

           final MediaPlayer notificationSound =  MediaPlayer.create(this, R.raw.justsaying);
           notificationSound.start();

           notificationSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
               @Override
               public void onCompletion(MediaPlayer mp) {
                   notificationSound.release();
               }
           });
       }
//       else
//           {
//               navigationView.getMenu().getItem(2).setIcon(R.drawable.notification);
//
//
//           }
    }

    private void isInternetConnection( )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            wificonnection = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileconnection = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

            if (wificonnection)
            {
                Toast.makeText(this, "You're mobile is connected to wifi!", Toast.LENGTH_LONG).show();
            }
            else if (mobileconnection)
            {
                Toast.makeText(this, "You're mobile is connected to cellular data!", Toast.LENGTH_LONG).show();
            }
        }
        else
            {
                View mview = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout, null);
                TextView Message = mview.findViewById(R.id.dialog_text);
                Button OkBtn = mview.findViewById(R.id.dialog_btn);
                AlertDialog.Builder mbuilder = new AlertDialog.Builder(MainActivity.this, R.style.mydialog);
                mbuilder.setView(mview);
                mbuilder.setCancelable(false);
                String message = "Please check you'r internet connection!";
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


    public void detectingConnectionState( )
    {
        // The below commented block of code, was to determine whether the app is connected to newtwork or not
        //there was a one problem it will run NOT CONNECTED first and after few second it'll run CONNECTING.

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final boolean connected = snapshot.getValue(Boolean.class);
                        if (connected)
                        {
                            noInternetMessagText.setVisibility(View.GONE);
                            noInternetMessagText.setClickable(false);
                            UserStatusButton.setVisibility(View.VISIBLE);
                            //UpdateUserStatus("online");
                           // Toast.makeText(MainActivity.this, "SociApp Database Connected", Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            UserStatusButton.setVisibility(View.GONE);
                            noInternetMessagText.setVisibility(View.VISIBLE);
                            noInternetMessagText.setText("Connecting SociApp...");
                            noInternetMessagText.setPadding(20, 20, 20, 20);
                            noInternetMessagText.setTextSize(18);
                            noInternetMessagText.setTextColor(getResources().getColor(R.color.red));

                            // UpdateUserStatus("offline");
                           // Toast.makeText(MainActivity.this, "Not Connected To Internet", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this, "Listner was cancelled!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }, 5000);
    }

    private void SendUserToPostActivity()
    {

        Intent PostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(PostIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        new MyAsyncTask().execute(mUserPost);

        postList.setHasFixedSize(true);
        //below line of code worked for smoothing the recyclerview.
        postList.setItemViewCacheSize(20);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            SendUserToLoginActivity();

        }
    }

    @Override
    protected void onDestroy() {

        System.gc();
        super.onDestroy();
    }

    private void SendUserToSetupActivity() {

        Intent SetupActivity = new Intent(MainActivity.this, SetupActivity.class);
        SetupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupActivity);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(getApplicationContext(), "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_notification:
                SendUserToNotificationActivity();
              //  Toast.makeText(getApplicationContext(), "Notifications", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendActivity();
                Toast.makeText(getApplicationContext(), "Friends List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                Toast.makeText(getApplicationContext(), "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                SendUserToFriendActivity();
                Toast.makeText(getApplicationContext(), "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToWholeSettingActivity();
                Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                UpdateUserStatus("offline");
                SendUserToLoginActivity();
                break;

            case R.id.nav_exit:
                finish();
                System.exit(0);
                break;
        }
    }


    private void VerifyEmailAddress( )
    {

        View mview = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout, null);
        TextView Message = mview.findViewById(R.id.dialog_text);
        Button OkBtn = mview.findViewById(R.id.dialog_btn);
        //below two lines of code transparent that Linearlayout.
        //LinearLayout backLinearlayout = (LinearLayout) mview.findViewById(R.id.blinearlayout);
        // backLinearlayout.setAlpha(0.5f);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MainActivity.this, R.style.mydialog);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        String message = "We've sent you an email, check the email and verify your account!";
        Message.setText(message);

        FirebaseUser User = mAuth.getCurrentUser();
        emailAddressChecker = User.isEmailVerified();

        if (!emailAddressChecker)
        {

            final Dialog dialog = mbuilder.create();
            dialog.show();

            OkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    UserStateRef.child(currentUserID).removeValue();
                    SendUserToLoginActivity();
                    mAuth.signOut();
                }
            });

        }
//        else
//        {
//
//
//        }
    }


    public void UpdateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        StateRef = FirebaseDatabase.getInstance().getReference().child("UserState");

        Calendar calForDate = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new java.text.SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        java.text.SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        try{
            StateRef.child(currentUserID)
                    .updateChildren(currentStateMap);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

    }



    private void SendUserToFriendActivity() {
        Intent loginIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToFindFriendsActivity() {
        Intent loginIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToProfileActivity() {
        Intent loginIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToNotificationActivity() {
        Intent loginIntent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToStatusPostActivity() {

        Intent StatusPostIntent = new Intent(MainActivity.this, StatusPostActivity.class);
        startActivity(StatusPostIntent);
    }

    private void SendUserToWholeSettingActivity() {

        Intent StatusPostIntent = new Intent(MainActivity.this, WholeSettingsActivity.class);
        startActivity(StatusPostIntent);
    }
}
