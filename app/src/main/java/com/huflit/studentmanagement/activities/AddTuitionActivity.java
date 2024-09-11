package com.huflit.studentmanagement.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityAddGradeBinding;
import com.huflit.studentmanagement.databinding.ActivityAddTuitionBinding;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.ExcelUtils;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class AddTuitionActivity extends AppCompatActivity {
    private ActivityAddTuitionBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddTuitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getIndex();
        binding.btConfirm.setOnClickListener(v -> {
            if (isValidTuitionDetails()) {
                addTuition();
            }
        });

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void getIndex() {
        database.collection(Constants.KEY_COLLECTION_TUITIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String teacherId = documentSnapshot.getId();
                        if (teacherId.length() >= 2) {
                            String indexStr = teacherId.substring(teacherId.length() - 2);
                            try {
                                int i = Integer.parseInt(indexStr);
                                if (i > id) {
                                    id = i;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void addTuition() {
        String studentId = preferenceManager.getString(Constants.KEY_STUDENT_ID);
        if (studentId != null && studentId.length() > 2) {
            studentId = studentId.substring(2);
        }
        String formattedId = String.format("%02d", id + 1);

        String tuitionId = "HP" + studentId + formattedId;

        String amount = binding.inputAmount.getText().toString().trim();
        String status = "Chưa thanh toán";
        loading(true);
        HashMap<String, Object> tuitionObj = new HashMap<>();
        tuitionObj.put(Constants.KEY_TUITION_ID, tuitionId);
        tuitionObj.put(Constants.KEY_TUITION_AMOUNT, amount);
        tuitionObj.put(Constants.KEY_TUITION_STATUS, status);
        tuitionObj.put(Constants.KEY_TUITION_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
        tuitionObj.put(Constants.KEY_TUITION_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID));

        database.collection(Constants.KEY_COLLECTION_TUITIONS)
                .document(tuitionId)
                .set(tuitionObj)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(this, "Thêm học phí thành công");
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

    private boolean isValidTuitionDetails() {
        if (binding.inputAmount.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(getApplicationContext(), "Hãy nhập số tiền học!");
            return false;
        }else {
            return true;
        }
    }
}