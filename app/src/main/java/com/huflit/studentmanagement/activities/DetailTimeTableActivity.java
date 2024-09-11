package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityDetailTeacherBinding;
import com.huflit.studentmanagement.databinding.ActivityDetailTimeTableBinding;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.models.Timetable;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

public class DetailTimeTableActivity extends AppCompatActivity {
    private ActivityDetailTimeTableBinding binding;
    private Timetable timetable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityDetailTimeTableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        listeners();
    }

    private void listeners() {
        setData();

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }


    private void setData() {
        timetable = (Timetable) getIntent().getSerializableExtra(Constants.KEY_TIMETABLE);
        if (timetable != null) {
            binding.tvTitle.setText(timetable.subject);
            binding.tvName.setText(timetable.teacher);
            binding.tvDay.setText("Thứ " + timetable.day);
            binding.tvPeriod.setText("Tiết " +timetable.period);
        }
    }
}