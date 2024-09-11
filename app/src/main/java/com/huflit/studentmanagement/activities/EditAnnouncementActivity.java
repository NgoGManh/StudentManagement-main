package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityEditAnnouncementBinding;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.HashMap;


public class EditAnnouncementActivity extends AppCompatActivity {
    private ActivityEditAnnouncementBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Announcement announcement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityEditAnnouncementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        announcement = (Announcement) getIntent().getSerializableExtra(Constants.KEY_ANNOUNCEMENT);
        binding.btConfirm.setOnClickListener(v -> {
            if (isValidAnnouncementDetails()) {
                editAnnouncement();
            }
        });
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
        binding.imageDelete.setOnClickListener(v -> {
            deleteAnnouncement();
        });
        getData();
    }

    private void getData() {
        binding.inputTitle.setText(announcement.title);
        binding.inputContent.setText(announcement.content);
    }

    private void editAnnouncement() {
        loading(true);
        HashMap<String, Object> announcement = new HashMap<>();
        announcement.put(Constants.KEY_ANNOUNCEMENT_TITLE, binding.inputTitle.getText().toString().trim());
        announcement.put(Constants.KEY_ANNOUNCEMENT_CONTENT, binding.inputContent.getText().toString().trim());

        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENT)
                .document(this.announcement.id)
                .update(announcement)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(getApplicationContext(), "Sửa thông báo thành công");
                    finish();
                })
                .addOnFailureListener(e ->  {
                    Utils.ShowToast(getApplicationContext(), e.getMessage());
                    loading(false);
                });
    }

    private void deleteAnnouncement() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENT)
                .document(announcement.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Utils.ShowToast(getApplicationContext(), "Xoá thông báo thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Utils.ShowToast(getApplicationContext(), e.getMessage());
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
            Utils.ShowToast(getApplicationContext(), "Hãy nhập tiêu đề");
            return false;
        } else if (binding.inputContent.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(getApplicationContext(), "Hãy nhập nội dung");
            return false;
        }else {
            return true;
        }
    }
}