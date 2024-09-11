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
import com.huflit.studentmanagement.databinding.ActivityAddClassBinding;
import com.huflit.studentmanagement.databinding.ActivityAddGradeBinding;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.ExcelUtils;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class AddGradeActivity extends AppCompatActivity {
    private ActivityAddGradeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int id = 0;
    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddGradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getIndex();

        SpinnerUtils.setSpinnerData(AddGradeActivity.this, SpinnerUtils.getGradeTypeList(), binding.inputType);
        binding.inputType.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputType);

        SpinnerUtils.setSpinnerData(AddGradeActivity.this, SpinnerUtils.getSubjectList(), binding.inputSubject);
        binding.inputSubject.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputSubject);

        binding.btConfirm.setOnClickListener(v -> {
            if (isValidGradeDetails()) {
                addGrade();
            }
        });

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });

        binding.imgUpload.setOnClickListener(v -> openFileChooser());
    }

    private void getIndex() {
        database.collection(Constants.KEY_COLLECTION_GRADES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String gradeId = documentSnapshot.getId();
                        if (gradeId.length() >= 2) {
                            String indexStr = gradeId.substring(gradeId.length() - 2);
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

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode ==  RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    List<Grade> grades = ExcelUtils.readGradesFromExcel(inputStream);
                    saveGradesToDatabase(grades);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveGradesToDatabase(List<Grade> grades) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (Grade grade : grades) {
            if (grade.getId() != null && !grade.getId().isEmpty()) {

                HashMap<String, Object> gradeData = new HashMap<>();
                gradeData.put(Constants.KEY_GRADE_ID, grade.getId());
                gradeData.put(Constants.KEY_GRADE_BAND, grade.getBand());
                gradeData.put(Constants.KEY_GRADE_TYPE, grade.getType());
                gradeData.put(Constants.KEY_GRADE_SUBJECT, grade.getSubject());
                gradeData.put(Constants.KEY_GRADE_STUDENT_ID, grade.getStudentId());
                gradeData.put(Constants.KEY_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID));


                db.collection(Constants.KEY_COLLECTION_STUDENTS)
                        .document(grade.getId())
                        .set(gradeData)
                        .addOnSuccessListener(aVoid -> {
                            Utils.ShowToast(this, "Thêm điểm thành công");
                            finish();
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }
    }

    private void addGrade() {
        String subjectName = binding.inputSubject.getSelectedItem().toString().trim();
        String formattedId = String.format("%03d", id + 1);
        String studentId = preferenceManager.getString(Constants.KEY_STUDENT_ID);
        if (studentId != null && studentId.length() > 2) {
            studentId = studentId.substring(2);
        }

        String gradeId = "D" + Utils.getSubjectId(subjectName) + studentId + formattedId;

        String grade = binding.inputGrade.getText().toString();
        String subject = binding.inputSubject.getSelectedItem().toString();
        String type = binding.inputType.getSelectedItem().toString();
        loading(true);
        HashMap<String, Object> gradeObj = new HashMap<>();
        gradeObj.put(Constants.KEY_GRADE_ID, gradeId);
        gradeObj.put(Constants.KEY_GRADE_BAND, grade);
        gradeObj.put(Constants.KEY_GRADE_SUBJECT, subject);
        gradeObj.put(Constants.KEY_GRADE_TYPE, type);
        gradeObj.put(Constants.KEY_GRADE_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
        gradeObj.put(Constants.KEY_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID));

        database.collection(Constants.KEY_COLLECTION_GRADES)
                .document(gradeId)
                .set(gradeObj)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(this, "Thêm điểm thành công");
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