package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityDetailAnnouncementBinding;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.Utils;

public class DetailAnnouncementActivity extends AppCompatActivity {
    private ActivityDetailAnnouncementBinding binding;
    private Announcement announcement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityDetailAnnouncementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        listeners();
    }

    private void listeners(){
        announcement = (Announcement) getIntent().getSerializableExtra(Constants.KEY_ANNOUNCEMENT);
        if (announcement != null) {
            getData();
        }

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void getData() {
        binding.tvTitle.setText(announcement.title);
        binding.tvContent.setText(announcement.content);
        binding.tvDate.setText(Utils.formatDateTime(announcement.time));
    }
}