package com.huflit.studentmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityMainBinding;
import com.huflit.studentmanagement.fragments.ClassFragment;
import com.huflit.studentmanagement.fragments.HomeFragment;
import com.huflit.studentmanagement.fragments.MoreFragment;
import com.huflit.studentmanagement.fragments.TeacherFragment;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout container;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        bottomNavigationView = findViewById(R.id.bottomMenu);
        container = findViewById(R.id.fragContainer);

        bottomNavigationView.inflateMenu(R.menu.btmenu);
        setData();

        binding.imgTop.setOnClickListener(v -> {


            for (int startYear = 19; startYear <= 25; startYear++) {
                String nextYear = String.valueOf(startYear + 1);
                String year = "20" + startYear + "-" + "20" + (startYear + 1);

                for (int term = 1; term <= 2; term++) {
                    String termId = "HK" + String.format("%02d", term) + startYear + nextYear;
                    String termName = term == 1 ? "I" : "II";

                    HashMap<String, Object> data = new HashMap<>();
                    data.put(Constants.KEY_TERM_ID, termId);
                    data.put(Constants.KEY_TERM_NAME, "HK " + termName + " - " + year);
                    data.put(Constants.KEY_TERM_YEAR, year);

                    db.collection(Constants.KEY_COLLECTION_TERMS)
                            .document(termId)
                            .set(data)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Optionally, you can add some log or toast here
                                } else {
                                    // Optionally, handle the failure here
                                }
                            });
                }
            }
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(Constants.KEY_OPEN_CLASS_FRAGMENT, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new ClassFragment()).commit();
            } else if(getIntent().getBooleanExtra(Constants.KEY_OPEN_TEACHER_FRAGMENT, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TeacherFragment()).commit();
            } else if(getIntent().getBooleanExtra(Constants.KEY_OPEN_MORE_FRAGMENT, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new MoreFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new HomeFragment()).commit();
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new HomeFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.item_class) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new ClassFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.item_teacher) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new TeacherFragment()).commit();
                    return true;
                } else if (item.getItemId() == R.id.item_more) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new MoreFragment()).commit();
                    return true;
                }
                return false;
            }
        });
    }


    public void setData() {
        binding.tvHello.setText("Xin chào, " + preferenceManager.getString(Constants.KEY_USER_NAME));
        String image = preferenceManager.getString(Constants.KEY_USER_IMAGE);
            if (image != null && !image.isEmpty()) {
                Bitmap bitmap = Utils.getBitmapFromEncodedString(image);
                if (bitmap != null) {
                    binding.imgTop.setImageBitmap(bitmap);
                } else {
                    binding.imgTop.setImageResource(R.drawable.ic_user_white);
                }
            } else {
                binding.imgTop.setImageResource(R.drawable.ic_user_white);
            }

            binding.imgTop.setOnClickListener(v -> {
                if (image != null && !image.isEmpty()) {
                    Utils.onImageClick(image, getApplicationContext());
                }
            });
    }
}