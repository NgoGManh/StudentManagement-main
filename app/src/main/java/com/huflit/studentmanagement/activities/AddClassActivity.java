package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityAddClassBinding;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.HashMap;

public class AddClassActivity extends AppCompatActivity {
    private ActivityAddClassBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddClassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        SpinnerUtils.setSpinnerData(AddClassActivity.this, SpinnerUtils.getTeacherList(), binding.inputTeacher);
        binding.inputTeacher.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputTeacher);

        SpinnerUtils.setSpinnerData(AddClassActivity.this, SpinnerUtils.getTermList(), binding.inputTerm);
        binding.inputTerm.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputTerm);

        binding.btConfirm.setOnClickListener(v -> {
            if (isValidClassDetails()) {
                addClass();
            }
        });

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void addClass() {
        String name = binding.inputClassName.getText().toString();
        String term = binding.inputTerm.getSelectedItem().toString();
        String termId = name + term;
        loading(true);
        HashMap<String, Object> Class = new HashMap<>();
        Class.put(Constants.KEY_CLASS_ID, termId);
        Class.put(Constants.KEY_CLASS_NAME, name);
        Class.put(Constants.KEY_CLASS_AMOUNT, binding.inputAmount.getText().toString());
        Class.put(Constants.KEY_CLASS_TEACHER_NAME, binding.inputTeacher.getSelectedItem().toString());
        Class.put(Constants.KEY_CLASS_TERM_ID, term);

        database.collection(Constants.KEY_COLLECTION_CLASSES)
                .document(termId)
                .set(Class)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(this, "Thêm lớp thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Utils.ShowToast(this, "Lỗi: " + e.getMessage());
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

    private boolean isValidClassDetails() {
        if (binding.inputClassName.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập tên lớp");
            return false;
        } else if (binding.inputAmount.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập số lượng học sinh");
            return false;
        } else if (binding.inputTeacher.getSelectedItem().toString().trim().equals("Chọn giáo viên")) {
            Utils.ShowToast(this, "Hãy chọn giáo viên");
            return false;
        } else {
            return true;
        }
    }
}