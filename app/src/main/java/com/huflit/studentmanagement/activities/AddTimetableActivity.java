package com.huflit.studentmanagement.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.models.Timetable;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.ExcelUtils;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;
import com.huflit.studentmanagement.databinding.ActivityAddTimetableBinding;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class AddTimetableActivity extends AppCompatActivity {
    private ActivityAddTimetableBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int id = 0;
    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddTimetableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getIndex();

        SpinnerUtils.setSpinnerData(AddTimetableActivity.this, SpinnerUtils.getDayList(), binding.inputDay);
        binding.inputDay.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputDay);

        SpinnerUtils.setSpinnerData(AddTimetableActivity.this, SpinnerUtils.getPeriodList(), binding.inputPeriod);
        binding.inputPeriod.setSelection(0);
        SpinnerUtils.onChooseSpinner(binding.inputPeriod);

        SpinnerUtils.setSpinnerData(AddTimetableActivity.this, SpinnerUtils.getSubjectList(), binding.inputSubject);
        binding.inputSubject.setSelection(0);

        binding.inputSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.inputSubject.setSelection(position);

                String subject = binding.inputSubject.getSelectedItem().toString();
                if (!subject.equals("Chọn môn")) {
                    SpinnerUtils.setSpinnerData(AddTimetableActivity.this, SpinnerUtils.getTeacherBySubjectList(subject), binding.inputTeacher);
                    binding.inputTeacher.setSelection(0);
                    SpinnerUtils.onChooseSpinner(binding.inputTeacher);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        binding.btConfirm.setOnClickListener(v -> {
            if (isValidTimetableDetails()) {
                addTimetable();
            }
        });

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });

        binding.imgUpload.setOnClickListener(v -> openFileChooser());
    }

    private void getIndex() {
        database.collection(Constants.KEY_COLLECTION_TIMETABLE)
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
                    List<Timetable> timetables = ExcelUtils.readTimetablesFromExcel(inputStream);
                    saveTimetablesToDatabase(timetables);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveTimetablesToDatabase(List<Timetable> timetables) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (Timetable timetable : timetables) {
            if (timetable.getId() != null && !timetable.getId().isEmpty()) {

                HashMap<String, Object> timeTableData = new HashMap<>();
                timeTableData.put(Constants.KEY_TIMETABLE_ID, timetable.getId());
                timeTableData.put(Constants.KEY_TIMETABLE_CLASS_ID, timetable.getClassId());
                timeTableData.put(Constants.KEY_TIMETABLE_DAY, timetable.getDay());
                timeTableData.put(Constants.KEY_TIMETABLE_PERIOD, timetable.getPeriod());
                timeTableData.put(Constants.KEY_TIMETABLE_SUBJECT, timetable.getSubject());
                timeTableData.put(Constants.KEY_TIMETABLE_TEACHER, timetable.getTeacher());

                db.collection(Constants.KEY_COLLECTION_STUDENTS)
                        .document(timetable.getId())
                        .set(timeTableData)
                        .addOnSuccessListener(aVoid -> {
                            Utils.ShowToast(this, "Thêm tiết học thành công");
                            finish();
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }
    }

    private void addTimetable() {
        String subjectName = binding.inputSubject.getSelectedItem().toString().trim();
        String formattedId = String.format("%02d", id + 1);
        String classId = preferenceManager.getString(Constants.KEY_CLASS_ID);


        String periodId = "T" + Utils.getSubjectId(subjectName) + classId + formattedId;

        String day = binding.inputDay.getSelectedItem().toString();
        String subject = binding.inputSubject.getSelectedItem().toString();
        String period = binding.inputPeriod.getSelectedItem().toString();
        String teacher = binding.inputTeacher.getSelectedItem().toString();
        loading(true);
        HashMap<String, Object> timeTableObj = new HashMap<>();
        timeTableObj.put(Constants.KEY_TIMETABLE_ID, periodId);
        timeTableObj.put(Constants.KEY_TIMETABLE_DAY, day);
        timeTableObj.put(Constants.KEY_TIMETABLE_PERIOD, period);
        timeTableObj.put(Constants.KEY_TIMETABLE_SUBJECT, subject);
        timeTableObj.put(Constants.KEY_TIMETABLE_TEACHER, teacher);
        timeTableObj.put(Constants.KEY_TIMETABLE_CLASS_ID, classId);

        database.collection(Constants.KEY_COLLECTION_TIMETABLE)
                .document(periodId)
                .set(timeTableObj)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(this, "Thêm tiết học thành công");
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

    private boolean isValidTimetableDetails() {
         if (binding.inputSubject.getSelectedItem().toString().trim().equals("Chọn môn")) {
            Utils.ShowToast(this, "Hãy chọn môn học");
            return false;
         } else if (binding.inputTeacher.getSelectedItem().toString().trim().equals("Chọn giáo viên")) {
             Utils.ShowToast(this, "Hãy chọn giáo viên");
             return false;
         } else if (binding.inputDay.getSelectedItem().toString().trim().equals("Chọn thứ")) {
             Utils.ShowToast(this, "Hãy chọn thứ");
             return false;
         } else if (binding.inputPeriod.getSelectedItem().toString().trim().equals("Chọn tiết")) {
             Utils.ShowToast(this, "Hãy chọn tiết");
             return false;
         } else {
            return true;
        }
    }
}