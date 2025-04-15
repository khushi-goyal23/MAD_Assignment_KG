package com.example.a4_googleauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "MainActivity";

    // ActivityResultLauncher to handle the sign-in intent result.
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Sign-in result received with code: " + result.getResultCode());
                
                // Check if data is null
                if (result.getData() == null) {
                    Log.e(TAG, "Sign-in result data is null");
                    Toast.makeText(this, "Sign-In Failed: No data received", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Handle the Google Sign-In result regardless of result code
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling sign-in result", e);
                    Toast.makeText(this, "Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Also initialize Google Sign In to check its state
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if user is still signed in with both Firebase and Google
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        
        if (currentUser != null && account != null) {
            Log.d(TAG, "Firebase user already signed in, navigating to HomeActivity");
            navigateToHomeActivity();
            return;
        }

        setContentView(R.layout.activity_main);

        // Set the click listener for the Google Sign-In button.
        findViewById(R.id.btn_google_sign_in).setOnClickListener(view -> {
            signIn();
        });
    }

    private void signIn() {
        Log.d(TAG, "Starting Google sign-in process");
        
        // Create a sign in intent directly
        Intent signInIntent = googleSignInClient.getSignInIntent();
        Log.d(TAG, "Launching sign-in intent");
        signInLauncher.launch(signInIntent);
    }

    // Handle the Google sign-in result.
    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful: " + account.getEmail());
            Toast.makeText(this, "Sign-In Successful!", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode(), e);
            
            // Provide more helpful error message based on the error code
            String errorMessage;
            switch (e.getStatusCode()) {
                case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                    errorMessage = "Sign-in was cancelled";
                    break;
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    errorMessage = "Network error occurred";
                    break;
                default:
                    errorMessage = "Sign-In Failed: " + e.getStatusCode();
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    // Authenticate with Firebase using the Google account's ID token.
    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Log.e(TAG, "ID token is null");
            Toast.makeText(this, "Authentication Failed: ID token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Authenticating with Firebase using Google ID token");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        // Show a loading message
        Toast.makeText(MainActivity.this, "Authenticating...", Toast.LENGTH_SHORT).show();
        
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "Firebase auth successful, navigating to HomeActivity");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d(TAG, "User info - Name: " + (user != null ? user.getDisplayName() : "null") + 
                                  ", Email: " + (user != null ? user.getEmail() : "null"));
                        navigateToHomeActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Exception exception = task.getException();
                        Log.e(TAG, "Authentication Failed!", exception);
                        String errorMessage = "Authentication Failed";
                        if (exception != null) {
                            Log.e(TAG, "Exception class: " + exception.getClass().getName());
                            Log.e(TAG, "Exception message: " + exception.getMessage());
                            errorMessage += ": " + exception.getMessage();
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Authentication failure", e);
                    Toast.makeText(MainActivity.this, "Authentication Failure: " + e.getMessage(), 
                                   Toast.LENGTH_LONG).show();
                });
    }
    
    private void navigateToHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Double-check if user is already signed in with both Firebase and Google
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        
        if (currentUser != null && account != null) {
            Log.d(TAG, "User is signed in with both Firebase and Google");
            navigateToHomeActivity();
        } else {
            Log.d(TAG, "User is not fully signed in: Firebase = " + (currentUser != null) + 
                      ", Google = " + (account != null));
        }
    }
}
