package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityDetailClassBinding;
import com.huflit.studentmanagement.databinding.ActivityDetailTeacherBinding;
import com.huflit.studentmanagement.databinding.FragmentDetailStudentBinding;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

public class DetailTeacherActivity extends AppCompatActivity {
    private ActivityDetailTeacherBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Teacher teacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));

        binding = ActivityDetailTeacherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        listeners();
    }

    private void listeners() {
        getData();

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void getData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_TEACHERS)
                .document(preferenceManager.getString(Constants.KEY_TEACHER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        teacher = new Teacher();
                        teacher.setId(document.getId());
                        teacher.setDob(Utils.getReadableDataTime(document.getDate(Constants.KEY_TEACHER_DOB)));
                        teacher.setSubject(document.getString(Constants.KEY_TEACHER_SUBJECT));
                        teacher.setPhone(document.getString(Constants.KEY_TEACHER_PHONE));
                        teacher.setGender(document.getString(Constants.KEY_TEACHER_GENDER));
                        teacher.setEmail(document.getString(Constants.KEY_TEACHER_EMAIL));
                        teacher.setImage(document.getString(Constants.KEY_TEACHER_IMAGE));
                        teacher.setName(document.getString(Constants.KEY_TEACHER_NAME));

                        setData(teacher.getDob(), teacher.getSubject(), teacher.getPhone(), teacher.getEmail(), teacher.getImage(), teacher.getGender(), teacher.getId(), teacher.getName());
                    } else {
                        Utils.ShowToast(getApplicationContext(), "Không tìm thấy dữ liệu học sinh!");
                    }
                });
    }

    private void setData(String dob, String subject, String phone, String email, String image, String gender, String id, String name) {
        binding.tvName.setText(name);
        binding.tvDOB.setText(dob);
        binding.tvGender.setText(gender);
        binding.tvId.setText(id);
        binding.tvSubject.setText(subject);
        binding.tvPhone.setText(phone);
        binding.tvEmail.setText(email);

        if (image != null && !image.isEmpty()) {
            Bitmap bitmap = Utils.getBitmapFromEncodedString(image);
            if (bitmap != null) {
                binding.imgStudent.setImageBitmap(bitmap);
            } else {
                binding.imgStudent.setImageResource(R.drawable.ic_default_image);
            }
        } else {
            binding.imgStudent.setImageResource(R.drawable.ic_default_image);
        }

        binding.imgStudent.setOnClickListener(v -> {
            if (image != null && !image.isEmpty()) {
                Utils.onImageClick(image, getApplicationContext());
            }
        });
        loading(false);
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.fragContainer.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.fragContainer.setVisibility(View.VISIBLE);
        }
    }
}