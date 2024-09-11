package com.huflit.studentmanagement.utilities;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SpinnerUtils {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static void setSpinnerData(Context context, List<String> spinnerData, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>( context, android.R.layout.simple_spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void onChooseSpinner(Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });
    }

    public static List<String> getSubjectList() {
        List<String> spinnerData = new java.util.ArrayList<>();
        spinnerData.add("Chọn môn");
        db.collection(Constants.KEY_COLLECTION_SUBJECTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            spinnerData.add(document.getString(Constants.KEY_SUBJECT_NAME));
                        }

                    }
                });
        return spinnerData;
    }

    public static List<String> getTeacherList() {
        List<String> spinnerData = new java.util.ArrayList<>();
        spinnerData.add("Chọn giáo viên");
        db.collection(Constants.KEY_COLLECTION_TEACHERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            spinnerData.add(document.getString(Constants.KEY_TEACHER_NAME));
                        }

                    }
                });
        return spinnerData;
    }

    public static List<String> getTeacherBySubjectList(String subject) {
        List<String> spinnerData = new java.util.ArrayList<>();
        spinnerData.add("Chọn giáo viên");
        db.collection(Constants.KEY_COLLECTION_TEACHERS)
                .whereEqualTo(Constants.KEY_TEACHER_SUBJECT, subject)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            spinnerData.add(document.getString(Constants.KEY_TEACHER_NAME));
                        }

                    }
                });
        return spinnerData;
    }

    public static List<String> getClassList() {
        List<String> spinnerData = new ArrayList<>();
        spinnerData.add("Chọn lớp");
        Set<String> classNamesSet = new HashSet<>();
        db.collection(Constants.KEY_COLLECTION_CLASSES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String className = document.getString(Constants.KEY_CLASS_NAME);
                            if (className != null && !classNamesSet.contains(className)) {
                                spinnerData.add(className);
                                classNamesSet.add(className);
                            }
                        }
                    }
                });
        return spinnerData;
    }

    public static List<String> getTermList() {
        List<String> spinnerData = new ArrayList<>();
        spinnerData.add("Chọn học kỳ");
        db.collection(Constants.KEY_COLLECTION_TERMS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String termId = document.getId();
                                spinnerData.add(termId);
                        }
                    }
                });
        return spinnerData;
    }

    public static List<String> getGradeTypeList() {
        List<String> spinnerData = new ArrayList<>();
        spinnerData.add("Chọn loại điểm");
        spinnerData.add("Miệng");
        spinnerData.add("15 phút");
        spinnerData.add("Giữa kỳ");
        spinnerData.add("Học kỳ");

        return spinnerData;
    }

    public static List<String> getDayList() {
        List<String> spinnerData = new ArrayList<>();
        spinnerData.add("Chọn thứ");
        spinnerData.add("2");
        spinnerData.add("3");
        spinnerData.add("4");
        spinnerData.add("5");
        spinnerData.add("6");
        spinnerData.add("7");

        return spinnerData;
    }

    public static List<String> getPeriodList() {
        List<String> spinnerData = new ArrayList<>();
        spinnerData.add("Chọn tiết");
        spinnerData.add("1");
        spinnerData.add("2");
        spinnerData.add("3");
        spinnerData.add("4");
        spinnerData.add("5");
        spinnerData.add("6");
        spinnerData.add("7");
        spinnerData.add("8");
        spinnerData.add("9");

        return spinnerData;
    }
}
