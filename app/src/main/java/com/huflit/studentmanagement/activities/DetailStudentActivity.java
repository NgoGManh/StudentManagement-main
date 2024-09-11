package com.huflit.studentmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityDetailClassBinding;
import com.huflit.studentmanagement.databinding.ActivityDetailStudentBinding;
import com.huflit.studentmanagement.fragments.AnnouncementFragment;
import com.huflit.studentmanagement.fragments.DetailStudentFragment;
import com.huflit.studentmanagement.fragments.StudentFragment;
import com.huflit.studentmanagement.fragments.TimetableFragment;
import com.huflit.studentmanagement.fragments.TranscriptFragment;
import com.huflit.studentmanagement.fragments.TuitionFragment;
import com.huflit.studentmanagement.models.Class;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;

public class DetailStudentActivity extends AppCompatActivity {
    private ActivityDetailStudentBinding binding;
    private FrameLayout container;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private Student student;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));

        binding = ActivityDetailStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")){
            binding.bottomNavigationView.setVisibility(View.GONE);
        }

        student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);
        if (student != null) {
            setData();
        }

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });

        container = findViewById(R.id.fragContainer);

        setBottomNavigationView();

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_infoStudent) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new DetailStudentFragment()).commit();
                    return true;
                }else if (item.getItemId() == R.id.item_transcript) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TranscriptFragment()).commit();
                    return true;
                }else if (item.getItemId() == R.id.item_tuition) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TuitionFragment()).commit();
                    return true;
                }
                return false;
            }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragContainer, new DetailStudentFragment())
                    .commit();
        }
    }
    public void setBottomNavigationView() {
            binding.bottomNavigationView.inflateMenu(R.menu.admin_student_bt_menu);
    }

    public void setData() {
        binding.tvName.setText(student.getName());
    }


}