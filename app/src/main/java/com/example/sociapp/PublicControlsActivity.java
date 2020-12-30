package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.CompoundButtonCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class PublicControlsActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private long progressValue = 0;
    private androidx.appcompat.widget.Toolbar mtoolbar;

    private TextView textSizeTv, switchTextViewShows, postSwitchTextShow, friendListSwitchTextShow;

    private DatabaseReference publicControlReference;
    private FirebaseAuth pAuth;
    private String currentUserId;
    private Switch  aSwitch, postSwitch, friend_list_switch;

    boolean testIfTrue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_controls);


          pAuth = FirebaseAuth.getInstance();
          currentUserId = pAuth.getCurrentUser().getUid();
          publicControlReference = FirebaseDatabase.getInstance().getReference().child("PublicControls");

          mtoolbar = (Toolbar) findViewById(R.id.public_controls_app_bar);
          setSupportActionBar(mtoolbar);
          getSupportActionBar().setTitle("Public Controls Settings");
          getSupportActionBar().setDisplayHomeAsUpEnabled(true);

          seekBar = (SeekBar) findViewById(R.id.seekBar);
          textSizeTv = (TextView) findViewById(R.id.seekBar_text_size_tv);

          switchTextViewShows = (TextView) findViewById(R.id.switch_button_shows);
          postSwitchTextShow = (TextView) findViewById(R.id.post_hidden_shown_text);
          friendListSwitchTextShow = (TextView) findViewById(R.id.friend_list_hidden_shown_text);

          aSwitch = (Switch) findViewById(R.id.switch1);
          postSwitch = (Switch) findViewById(R.id.post_hidden_shown_switch);
          friend_list_switch = (Switch) findViewById(R.id.friend_list_hidden_shown_switch);


        publicControlReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("textsize").exists())
                    {

                        progressValue = (long) dataSnapshot.child("textsize").getValue();

                        seekBar.setProgress(Integer.parseInt(String.valueOf(progressValue)));
                        textSizeTv.setText(String.valueOf(progressValue));
                    }

                  if (dataSnapshot.child("hideIt").exists())
                  {
                      testIfTrue = (boolean) dataSnapshot.child("hideIt").getValue();

                      if (testIfTrue)
                      {
                          aSwitch.setChecked(true);
                          aSwitch.setText("Hidden");
                      } else
                          {
                          aSwitch.setChecked(false);
                          aSwitch.setText("Shown");
                         }
                  }

                  if (dataSnapshot.child("hidepostlist").exists())
                  {
                      testIfTrue = (boolean) dataSnapshot.child("hidepostlist").getValue();

                      if (testIfTrue)
                      {
                          postSwitch.setChecked(true);
                          postSwitch.setText("Hidden");
                      }
                      else
                          {
                          postSwitch.setChecked(false);
                          postSwitch.setText("Shown");
                          }
                  }

                  if (dataSnapshot.child("hidefriendlist").exists())
                  {
                      testIfTrue = (boolean) dataSnapshot.child("hidefriendlist").getValue();

                      if (testIfTrue)
                      {
                          friend_list_switch.setChecked(true);
                          friend_list_switch.setText("Hidden");
                      }
                      else
                          {
                          friend_list_switch.setChecked(false);
                          friend_list_switch.setText("Shown");
                          }
                  }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                  progressValue = progress;
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {


              }

              @Override
              public void onStopTrackingTouch(final SeekBar seekBar) {

                  //the below line can also be written as textSizeTv.setText(Integer.toString(progressValue+" ");
                  textSizeTv.setText(String.valueOf(progressValue));

                  HashMap publicControlMap = new HashMap();
                  publicControlMap.put("textsize", progressValue);
                  publicControlReference.child(currentUserId).updateChildren(publicControlMap)
                          .addOnCompleteListener(new OnCompleteListener() {
                              @Override
                              public void onComplete(@NonNull Task task) {

                                  Toast.makeText(PublicControlsActivity.this, "Chat Font Size Changed "+progressValue, Toast.LENGTH_SHORT).show();

                              }
                          });
              }
          });

             aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {


                     if (isChecked)
                     {
                         aSwitch.setText("Hidden");

                     }
                     else
                         {
                             aSwitch.setText("Shown");

                         }

                     HashMap publicControlMap = new HashMap();
                     publicControlMap.put("hideIt", isChecked);
                     publicControlReference.child(currentUserId).updateChildren(publicControlMap)
                             .addOnCompleteListener(new OnCompleteListener() {
                                 @Override
                                 public void onComplete(@NonNull Task task) {

                                     Toast.makeText(PublicControlsActivity.this, "You Information is updated!", Toast.LENGTH_SHORT).show();

                                 }
                             });
                 }
             });

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {


                if (isChecked)
                {
                    postSwitch.setText("Hidden");

                }
                else
                {
                    postSwitch.setText("Shown");

                }

                HashMap publicControlMap = new HashMap();
                publicControlMap.put("hidepostlist", isChecked);
                publicControlReference.child(currentUserId).updateChildren(publicControlMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                Toast.makeText(PublicControlsActivity.this, "You Information is updated!", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });

        friend_list_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {


                if (isChecked)
                {
                    friend_list_switch.setText("Hidden");

                }
                else
                {
                    friend_list_switch.setText("Shown");

                }

                HashMap publicControlMap = new HashMap();
                publicControlMap.put("hidefriendlist", isChecked);
                publicControlReference.child(currentUserId).updateChildren(publicControlMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                Toast.makeText(PublicControlsActivity.this, "You Information is updated!", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });
    }
}
