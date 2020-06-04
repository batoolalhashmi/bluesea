package com.barmej.bluesea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.barmej.bluesea.R;
import com.barmej.bluesea.domain.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import static com.barmej.bluesea.activities.LoginActivity.isValidEmail;


public class SignUpActivity extends AppCompatActivity {
    private static final int REQUEST_GET_PHOTO = 3;
    private FirebaseAuth mAuth;
    private TextInputEditText userNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private ImageView mUserPhotoImageView;
    private Uri mUserPhotoUri;
    private String userName;
    private String email;
    private String password;
    private Button createAccountButton;
    private Button loginButton;
    private ConstraintLayout mConstraintLayout;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userNameEditText = findViewById(R.id.user_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        createAccountButton = findViewById(R.id.create_account_button);
        loginButton = findViewById(R.id.login_button);
        mUserPhotoImageView = findViewById(R.id.user_photo);
        mConstraintLayout = findViewById(R.id.constraint_layout);
        progressBar = findViewById(R.id.progress_bar_sign_up);

        createAccountButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);

        mUserPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGalleryIntent();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(userNameEditText.getText())) {
                    userNameEditText.setError(getString(R.string.username_is_empty));
                    return;
                }
                if (!isValidEmail(emailEditText.getText())) {
                    emailEditText.setError(getString(R.string.invalid_email));
                    return;
                }
                if (passwordEditText.getText().length() < 6) {
                    passwordEditText.setError(getString(R.string.invalid_password_length));
                    return;
                }
                if (mUserPhotoUri == null) {
                    Snackbar.make(mConstraintLayout, R.string.select_photo, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                registerNewUser();
            }
        });

    }

    public void registerNewUser() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        final StorageReference photoStorageReference = storageReference.child(UUID.randomUUID().toString());

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mDbRef = mDatabase.getReference("users");
        hideForm(true);
        mAuth = FirebaseAuth.getInstance();
        userName = userNameEditText.getText().toString();
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            final String userId = task.getResult().getUser().getUid();
                            photoStorageReference.putFile(mUserPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        photoStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {

                                                    final User user = new User();
                                                    user.setUserName(userName);
                                                    user.setEmail(email);
                                                    user.setUserPhoto(task.getResult().toString());
                                                    user.setStatus(User.Status.FREE.name());
                                                    mDbRef.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                startActivity(LoginActivity.getStartIntent(SignUpActivity.this));
                                                                finish();
                                                            } else {
                                                                Snackbar.make(mConstraintLayout, R.string.failed_sign_up, Snackbar.LENGTH_SHORT).show();
                                                                hideForm(false);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Snackbar.make(mConstraintLayout, R.string.failed_sign_up, Snackbar.LENGTH_SHORT).show();
                                                    hideForm(false);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void hideForm(boolean hide) {
        if (hide) {
            createAccountButton.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            createAccountButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    mUserPhotoUri = data.getData();
                    mUserPhotoImageView.setImageURI(mUserPhotoUri);
                } catch (Exception e) {
                    Snackbar.make(mConstraintLayout, R.string.photo_selection_error, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_photo)), REQUEST_GET_PHOTO);
    }
}
