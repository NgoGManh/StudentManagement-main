package com.huflit.studentmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityDetailClassBinding;
import com.huflit.studentmanagement.databinding.ActivityMainBinding;
import com.huflit.studentmanagement.fragments.AnnouncementFragment;
import com.huflit.studentmanagement.fragments.ClassFragment;
import com.huflit.studentmanagement.fragments.HomeFragment;
import com.huflit.studentmanagement.fragments.MoreFragment;
import com.huflit.studentmanagement.fragments.StudentFragment;
import com.huflit.studentmanagement.fragments.TeacherFragment;
import com.huflit.studentmanagement.fragments.TimetableFragment;
import com.huflit.studentmanagement.fragments.TranscriptFragment;
import com.huflit.studentmanagement.fragments.TuitionFragment;
import com.huflit.studentmanagement.models.Class;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;

import java.util.HashMap;

public class DetailClassActivity extends AppCompatActivity {
    private ActivityDetailClassBinding binding;
    private FrameLayout container;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private Class class1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));

        binding = ActivityDetailClassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        class1 = (Class) getIntent().getSerializableExtra(Constants.KEY_CLASS);
        if (class1 != null) {
            setData();
        }

        binding.imageBack.setOnClickListener(v -> {
            preferenceManager.remove(Constants.KEY_CLASS_ID);
            preferenceManager.remove(Constants.KEY_STUDENT_ID);
            finish();
        });

        container = findViewById(R.id.fragContainer);

        setBottomNavigationView();

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(Constants.KEY_OPEN_TIMETABLE_FRAGMENT, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TimetableFragment()).commit();
            } else if(getIntent().getBooleanExtra(Constants.KEY_OPEN_ANNOUNCEMENT_FRAGMENT, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new AnnouncementFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new StudentFragment()).commit();
            }
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_studentList) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new StudentFragment()).commit();
                    return true;
                }else if (item.getItemId() == R.id.item_transcript) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TranscriptFragment()).commit();
                    return true;
                }else if (item.getItemId() == R.id.item_timeTable) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TimetableFragment()).commit();
                    return true;
                }else if (item.getItemId() == R.id.item_announcement) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new AnnouncementFragment()).commit();
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
                    .replace(R.id.fragContainer, new StudentFragment())
                    .commit();
        }
    }
    public void setBottomNavigationView() {
        if ("Quản lý".equals(preferenceManager.getString(Constants.KEY_USER_ROLE)) || "Giáo viên".equals(preferenceManager.getString(Constants.KEY_USER_ROLE))) {
            binding.bottomNavigationView.inflateMenu(R.menu.class_bt_menu);
        } else {
            binding.bottomNavigationView.inflateMenu(R.menu.class_student_bt_menu);
        }
    }

    public void setData() {
        String termId = class1.getTermId();
        db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_TERMS)
                        .document(termId)
                                .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot != null) {
                                                binding.tvClassName.setText(class1.getName());
                                                binding.tvYear.setText(documentSnapshot.getString(Constants.KEY_TERM_NAME));
                                                binding.tvTeacher.setText(class1.getTeacher_name());
                                            }
                                        });
    }
}