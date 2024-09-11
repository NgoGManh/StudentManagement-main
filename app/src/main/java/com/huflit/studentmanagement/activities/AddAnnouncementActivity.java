package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityAddAnnouncementBinding;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.Date;
import java.util.HashMap;

public class AddAnnouncementActivity extends AppCompatActivity {

    private ActivityAddAnnouncementBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddAnnouncementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        binding.btConfirm.setOnClickListener(v -> {
            if (isValidAnnouncementDetails()) {
                addAnnouncement();
            }
        });
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }


    private void addAnnouncement() {
        loading (true);
        Timestamp currentTime = new Timestamp(new Date());

        HashMap<String, Object> announcement = new HashMap<>();
        announcement.put(Constants.KEY_ANNOUNCEMENT_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID));
        announcement.put(Constants.KEY_ANNOUNCEMENT_TITLE, binding.inputTitle.getText().toString().trim());
        announcement.put(Constants.KEY_ANNOUNCEMENT_CONTENT, binding.inputContent.getText().toString().trim());
        announcement.put(Constants.KEY_ANNOUNCEMENT_TIME, currentTime);

        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENT)
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(AddAnnouncementActivity.this, "Thêm thông báo thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Utils.ShowToast(AddAnnouncementActivity.this, "Lỗi: " + e.getMessage());
                    loading(false);
                });
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btConfirm.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btConfirm.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidAnnouncementDetails() {
        if (binding.inputTitle.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(AddAnnouncementActivity.this, "Hãy nhập tiêu đề");
            return false;
        } else if (binding.inputContent.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(AddAnnouncementActivity.this, "Hãy nhập nội dung");
            return false;
        }else {
            return true;
        }
    }
}