package com.example.sociapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BugReportActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView gmailAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report);

        mToolbar = (Toolbar) findViewById(R.id.bug_report_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Report Problem");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       gmailAccount = (TextView) findViewById(R.id.gmail_account);

       gmailAccount.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               Intent emailIntent = new Intent(Intent.ACTION_SEND);
               emailIntent.setType("text/plain");
               startActivity(emailIntent);
           }
       });


    }
}
