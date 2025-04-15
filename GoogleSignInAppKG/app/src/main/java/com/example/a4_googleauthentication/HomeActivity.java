package com.example.a4_googleauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvSuccessMessage;
    private ImageView ivProfile;
    private Button btnLogout;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "HomeActivity onCreate");

        // Initialize UI components
        tvWelcome = findViewById(R.id.tv_welcome);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        ivProfile = findViewById(R.id.iv_profile);
        btnLogout = findViewById(R.id.btn_logout);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Check if user is authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is signed in, redirecting to login");
            Toast.makeText(this, "No user is signed in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Display user information
        displayUserInfo();

        // Set the logout button functionality.
        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "Logout button clicked");
            Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
            
            // Sign out from Firebase and Google
            firebaseAuth.signOut();
            
            // Get Google sign in client and sign out from Google too
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            
            // Sign out from Google and then redirect to login
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Log.d(TAG, "User signed out from Google, navigating to MainActivity");
                redirectToLogin();
            });
        });
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void displayUserInfo() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            
            Log.d(TAG, "User info - UID: " + user.getUid() + 
                       ", Name: " + (name != null ? name : "null") + 
                       ", Email: " + (email != null ? email : "null") + 
                       ", Photo URL: " + (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "null"));
            
            // Set welcome message
            if (name != null && !name.isEmpty()) {
                tvWelcome.setText("Welcome, " + name + "!");
                Log.d(TAG, "Displaying welcome message for user: " + name);
            } else if (email != null && !email.isEmpty()) {
                tvWelcome.setText("Welcome, " + email + "!");
                Log.d(TAG, "Displaying welcome message for email: " + email);
            } else {
                tvWelcome.setText("Welcome!");
                Log.d(TAG, "Displaying generic welcome message");
            }
            
            // Load profile image if available
            if (user.getPhotoUrl() != null) {
                String photoUrl = user.getPhotoUrl().toString();
                Log.d(TAG, "Loading profile image from: " + photoUrl);
                try {
                    // Use Glide to load the image
                    Glide.with(this)
                         .load(photoUrl)
                         .apply(RequestOptions.circleCropTransform())
                         .into(ivProfile);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading profile image", e);
                }
            } else {
                Log.d(TAG, "No profile image available");
            }
        } else {
            Log.e(TAG, "No user is currently signed in");
            // Redirect back to login if no user is signed in
            redirectToLogin();
        }
    }
}
