package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityEditAnnouncementBinding;
import com.huflit.studentmanagement.databinding.ActivityEditGradeBinding;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.HashMap;

public class EditGradeActivity extends AppCompatActivity {
    private ActivityEditGradeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Grade gradeObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityEditGradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        SpinnerUtils.setSpinnerData(EditGradeActivity.this, SpinnerUtils.getGradeTypeList(), binding.inputType);
        binding.inputType.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputType);

        SpinnerUtils.setSpinnerData(EditGradeActivity.this, SpinnerUtils.getSubjectList(), binding.inputSubject);
        binding.inputSubject.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputSubject);


        gradeObj = (Grade) getIntent().getSerializableExtra(Constants.KEY_GRADE);
        binding.btConfirm.setOnClickListener(v -> {
            if (isValidGradeDetails()) {
                editGrade();
            }
        });
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
        binding.imageDelete.setOnClickListener(v -> {
            deleteGrade();
        });
        getData();
    }

    private void getData() {
        binding.inputGrade.setText(gradeObj.band);
    }

    private void editGrade() {
        loading(true);
        String subject = binding.inputSubject.getSelectedItem().toString();
        String type = binding.inputType.getSelectedItem().toString();
        HashMap<String, Object> grade = new HashMap<>();
        grade.put(Constants.KEY_GRADE_BAND, binding.inputGrade.getText().toString().trim());
        grade.put(Constants.KEY_GRADE_SUBJECT, subject);
        grade.put(Constants.KEY_GRADE_TYPE, type);

        database.collection(Constants.KEY_COLLECTION_GRADES)
                .document(gradeObj.id)
                .update(grade)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(getApplicationContext(), "Sửa điểm thành công");
                    finish();
                })
                .addOnFailureListener(e ->  {
                    Utils.ShowToast(getApplicationContext(), e.getMessage());
                    loading(false);
                });
    }

    private void deleteGrade() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_GRADES)
                .document(gradeObj.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Utils.ShowToast(getApplicationContext(), "Xoá điểm thành công");
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

    private boolean isValidGradeDetails() {
        if (binding.inputSubject.getSelectedItem().toString().trim().equals("Chọn môn")) {
            Utils.ShowToast(this, "Hãy chọn môn học");
            return false;
        } else if (binding.inputGrade.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập số điểm");
            return false;
        } else if (Float.parseFloat(binding.inputGrade.getText().toString().trim()) < 0 || Float.parseFloat(binding.inputGrade.getText().toString().trim()) > 10) {
            Utils.ShowToast(this, "Điểm phải nằm trong khoảng 0-10");
            return false;
        } else if (binding.inputType.getSelectedItem().toString().trim().equals("Chọn loại điểm")) {
            Utils.ShowToast(this, "Hãy chọn loại điểm");
            return false;
        } else {
            return true;
        }
    }
}