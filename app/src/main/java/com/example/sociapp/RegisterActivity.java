package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreatAccountButton;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

      loadingBar = new ProgressDialog(this);
       mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText)findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        CreatAccountButton = (Button)findViewById(R.id.register_create_account);

        CreatAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CreatNewAccount( );
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){

            SendUserToMainActivity( );
        }
    }

    private void SendUserToMainActivity() {

        Intent MainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void CreatNewAccount() {

        String email = UserEmail.getText().toString().trim();
        String password = UserPassword.getText().toString().trim();
        String confirmpassword = UserConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this, "Please Enter your Email!", Toast.LENGTH_SHORT).show();
        }
       else if (TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Please Enter your Password!", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() <= 6){
            Toast.makeText(RegisterActivity.this, " Your Password must be greater than 6 characters!", Toast.LENGTH_SHORT).show();
        }
       else if (TextUtils.isEmpty(confirmpassword)){
            Toast.makeText(RegisterActivity.this, "Please Confirm your Password!", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmpassword)){
            Toast.makeText(RegisterActivity.this, "Confirm Password does not match your Password!", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Authenticting User");
            loadingBar.setMessage("Please wait, Until we authenticate the User!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        loadingBar.dismiss();
                        sendEmailVerificationMessage( );

                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error Occured! "+message , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }

    }

    private void sendEmailVerificationMessage( )
    {
      /*  final AlertDialog.Builder mbuilder = new AlertDialog.Builder(RegisterActivity.this);
        mbuilder.setTitle("Email Verification!");
        mbuilder.setMessage("Email Saved, on second page Info is must, otherwise you won't proceed!");
        mbuilder.setCancelable(false);
        mbuilder.setIcon(R.drawable.alert);*/

        FirebaseUser User = mAuth.getCurrentUser();

        if (User != null)
        {
            User.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful())
                    {
                        View mview = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.dialog_layout, null);
                        TextView Message = mview.findViewById(R.id.dialog_text);
                        Button OkBtn = mview.findViewById(R.id.dialog_btn);
                        //below two lines of code transparent that Linearlayout.
                        //LinearLayout backLinearlayout = (LinearLayout) mview.findViewById(R.id.blinearlayout);
                        // backLinearlayout.setAlpha(0.5f);
                        AlertDialog.Builder mbuilder = new AlertDialog.Builder(RegisterActivity.this, R.style.mydialog);
                        mbuilder.setView(mview);
                        mbuilder.setCancelable(false);
                        String message = "Email Saved, on next page Info is must, otherwise you won't proceed!";
                        Message.setText(message);

                        final Dialog dialog = mbuilder.create();
                        dialog.show();

                        OkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                SendUserToSetupActivity( );
                            }
                        });
                       /* mbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                SendUserToSetupActivity( );
                                //mAuth.signOut();
                            }
                        });

                        Dialog dialog = mbuilder.create();
                        dialog.show();*/

                    }
                    else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                }
            });
        }
    }

    private void SendUserToLoginActivity() {

        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }
    private void SendUserToSetupActivity() {

        Intent SetupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }

}
