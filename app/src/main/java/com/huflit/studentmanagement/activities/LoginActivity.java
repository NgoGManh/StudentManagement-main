package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityLoginBinding;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    private int seePass = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.btLogin.setOnClickListener(v-> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
        binding.tvForgot.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));

        binding.seePassword.setOnClickListener(v -> {
            if (seePass == 0) {
                binding.inputPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seePass = 1;
            } else {
                binding.inputPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seePass = 0;
            }
        });
    }

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_USER_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_USER_PASSWORD, binding.inputPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_USER_NAME, documentSnapshot.getString(Constants.KEY_USER_NAME));
                        preferenceManager.putString(Constants.KEY_USER_ROLE, documentSnapshot.getString(Constants.KEY_USER_ROLE));
                        preferenceManager.putString(Constants.KEY_USER_IMAGE, documentSnapshot.getString(Constants.KEY_USER_IMAGE));
                        preferenceManager.putString(Constants.KEY_USER_EMAIL, documentSnapshot.getString(Constants.KEY_USER_EMAIL));
                        preferenceManager.putString(Constants.KEY_USER_INFO_ID, documentSnapshot.getString(Constants.KEY_USER_INFO_ID));
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        Utils.ShowToast(this,"Không thể đăng nhập");
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btLogin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btLogin.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidSignInDetails() {
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this,"Hãy nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            Utils.ShowToast(this,"Email không hợp lệ");
            return false;
        } else if (binding.inputPass.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this,"Hãy nhập mật khẩu");
            return false;
        }else {
            return true;
        }
    }
}