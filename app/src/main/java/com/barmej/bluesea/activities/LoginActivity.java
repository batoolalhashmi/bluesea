package com.barmej.bluesea.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.barmej.bluesea.R;
import com.barmej.bluesea.domain.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {
    private static final String USER_REF_PATH = "users";
    private TextInputEditText emailEt;
    private TextInputEditText passwordEt;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button loginBt;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private User user;


    public static Intent getStartIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        passwordEt = findViewById(R.id.password_text_input_edit_text);
        emailEt = findViewById(R.id.email_text_input_edit_text);
        loginBt = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.progressBar);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        hideForm(false);

        database = FirebaseDatabase.getInstance();
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginClicked();
            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            hideForm(true);
            startActivity(HomeActivity.getStartIntent(LoginActivity.this));

        }
    }

    private void loginClicked() {
        if (!isValidEmail(emailEt.getText())) {
            emailEt.setError(getString(R.string.invalid_email));
            return;
        }
        if (passwordEt.getText().length() < 6) {
            passwordEt.setError(getString(R.string.invalid_password_length));
            return;
        }
        hideForm(true);

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(emailEt.getText().toString(), passwordEt.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getUser().getUid();

                            fetchUserProfileAndLogin(userId);
                        } else {
                            hideForm(false);
                            Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserProfileAndLogin(String userId) {
        database.getReference(USER_REF_PATH).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    startActivity(HomeActivity.getStartIntent(LoginActivity.this));
                    finish();
                } else {
                    hideForm(false);
                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void hideForm(boolean hide) {
        if (hide) {
            progressBar.setVisibility(View.VISIBLE);

            passwordLayout.setVisibility(View.INVISIBLE);
            emailLayout.setVisibility(View.INVISIBLE);
            loginBt.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            emailLayout.setVisibility(View.VISIBLE);
            loginBt.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());

    }
}
