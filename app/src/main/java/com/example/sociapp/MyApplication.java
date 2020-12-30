package com.example.sociapp;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application implements LifecycleObserver {

    //This class is implemented to detect, that the user is online or not.
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, StateRef;
    private String currentUserID;


    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        mAuth = FirebaseAuth.getInstance();
        try{
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        catch (NullPointerException e){

            e.printStackTrace();}

       // UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StateRef = FirebaseDatabase.getInstance().getReference().child("UserState");
    }

    //I change private modifier to public of all this class method.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
       // Log.d("MyApp", "App in background");
        UpdateUserStatus("offline");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onAppBackgrounded2() {
        // Log.d("MyApp", "App in background");
        UpdateUserStatus("offline");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
       // Log.d("MyApp", "App in foreground");
        UpdateUserStatus("online");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onAppForegrounded1() {
        // Log.d("MyApp", "App in foreground");
        UpdateUserStatus("online");
    }

    public void UpdateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

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

//      try{
//          UsersRef.child(currentUserID).child("userState")
//                .updateChildren(currentStateMap);
//    }
//      catch (NullPointerException e)
//      {
//          e.printStackTrace();
//      }

        StateRef = FirebaseDatabase.getInstance().getReference().child("UserState");

        try{
            StateRef.child(currentUserID).updateChildren(currentStateMap);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

    }

}
