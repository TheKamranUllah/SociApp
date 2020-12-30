package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextView googleSignButton,NeedNewAccountLink;
    private Button loginButton;
    private EditText UserEmail, UserPassword;
    private TextView  ForgetPasswordLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private final static int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private Boolean emailAddressChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      try {
          setContentView(R.layout.activity_login);
      } catch (InflateException e){

           e.printStackTrace();
          Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
      }
      catch (OutOfMemoryError e){ e.printStackTrace();}


        ForgetPasswordLink = (TextView) findViewById(R.id.forgot_password_link);
        googleSignButton = (TextView) findViewById(R.id.google_signin_button);
       NeedNewAccountLink = (TextView) findViewById(R.id.register_account_link);
       UserEmail = (EditText) findViewById(R.id.login_email);
       UserPassword = (EditText) findViewById(R.id.login_password);
       loginButton = (Button) findViewById(R.id.login_button);
        loadingBar = new ProgressDialog(this);
       mAuth = FirebaseAuth.getInstance();

    try {
        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });
    } catch (NullPointerException e){e.printStackTrace();}

       NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               SendUserToRegisterActivity( );
           }
       });

       loginButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AllowingUserToLogin ( );
           }
       });

        // Configure Google Sign Inpcx
              GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Connection to Google Sign in failed!", Toast.LENGTH_SHORT).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            loadingBar.setTitle("Google sign in");
            loadingBar.setMessage("Please wait, Until we are allowing you to login using you're Google account!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
              //  Toast.makeText(this, "Please wait, while we are getting your Auth result.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Can't get Auth result.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            VerifyEmailAddressForGoogleUser( );
                            loadingBar.dismiss();

                        } else
                            {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            SendUserToLoginActivity( );
                            Toast.makeText(LoginActivity.this, "Error occured! "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    // If we remove the below code, we'll be needing to logging into our app every time we install it on emulator.
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){

            SendUserToMainActivity( );
        }
    }

    private void AllowingUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please write you email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write you password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Logging to SociApp");
            loadingBar.setMessage("Please wait, Until we are allowing you to login!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                       loadingBar.dismiss();
                      VerifyEmailAddress();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error!"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }


    private void VerifyEmailAddress( )
    {

        View mview = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_layout, null);
        TextView Message = mview.findViewById(R.id.dialog_text);
        Button OkBtn = mview.findViewById(R.id.dialog_btn);
        //below two lines of code transparent that Linearlayout.
        //LinearLayout backLinearlayout = (LinearLayout) mview.findViewById(R.id.blinearlayout);
        // backLinearlayout.setAlpha(0.5f);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(LoginActivity.this, R.style.mydialog);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        String message = "Your email is not verified, please verify your email and try again!";
        Message.setText(message);

     FirebaseUser User = mAuth.getCurrentUser();
     emailAddressChecker = User.isEmailVerified();

     if (emailAddressChecker)
     {
            SendUserToMainActivity();
        // Toast.makeText(getApplicationContext(), "User logged in successfully", Toast.LENGTH_SHORT).show();
     }
     else
         {
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


    private void VerifyEmailAddressForGoogleUser( )
    {

        View mview = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_layout, null);
        TextView Message = mview.findViewById(R.id.dialog_text);
        Button OkBtn = mview.findViewById(R.id.dialog_btn);
        //below two lines of code transparent that Linearlayout.
        //LinearLayout backLinearlayout = (LinearLayout) mview.findViewById(R.id.blinearlayout);
        // backLinearlayout.setAlpha(0.5f);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(LoginActivity.this, R.style.mydialog);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        String message = "Your email is not verified, please verify your email and try again!";
        Message.setText(message);

        FirebaseUser User = mAuth.getCurrentUser();
        emailAddressChecker = User.isEmailVerified();

        if (emailAddressChecker)
        {
            SendUserToSetupActivity();
            // Toast.makeText(getApplicationContext(), "User logged in successfully", Toast.LENGTH_SHORT).show();
        }
        else
        {
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
    private void SendUserToMainActivity() {

        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {

        Intent MainIntent = new Intent(LoginActivity.this, LoginActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);

    }

    private void SendUserToSetupActivity( )
    {
        Intent intent = new Intent(LoginActivity.this, SetupActivity.class);
        startActivity(intent);
    }
}
