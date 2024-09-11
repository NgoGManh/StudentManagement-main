package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddGradeActivity;
import com.huflit.studentmanagement.activities.EditGradeActivity;
import com.huflit.studentmanagement.adapters.GradeAdapter;
import com.huflit.studentmanagement.databinding.FragmentTranscriptBinding;
import com.huflit.studentmanagement.listeners.GradeListener;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TranscriptFragment extends Fragment implements GradeListener {

    private FragmentTranscriptBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    private List<Grade> grades1 = new ArrayList<>();
    private List<Grade> grades2 = new ArrayList<>();
    private List<Grade> grades3 = new ArrayList<>();
    private List<Grade> grades4 = new ArrayList<>();

    private GradeAdapter adapter1, adapter2, adapter3, adapter4;
    private String subjectType = "Chọn môn";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTranscriptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        setupRecyclerViews();
        listeners();
    }

    private void setupRecyclerViews() {
        setupRecyclerView(binding.rvType1, grades1);
        setupRecyclerView(binding.rvType2, grades2);
        setupRecyclerView(binding.rvType3, grades3);
        setupRecyclerView(binding.rvType4, grades4);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Grade> grades) {
        GradeAdapter gradeAdapter = new GradeAdapter(grades, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(gradeAdapter);

        if (recyclerView.getId() == R.id.rvType1) {
            adapter1 = gradeAdapter;
        } else if (recyclerView.getId() == R.id.rvType2) {
            adapter2 = gradeAdapter;
        } else if (recyclerView.getId() == R.id.rvType3) {
            adapter3 = gradeAdapter;
        } else if (recyclerView.getId() == R.id.rvType4) {
            adapter4 = gradeAdapter;
        }
    }

    private void listeners() {
        SpinnerUtils.setSpinnerData(getContext(), SpinnerUtils.getSubjectList(), binding.subjectSpinner);

        binding.subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectType = binding.subjectSpinner.getSelectedItem().toString();
                getData(subjectType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddGradeActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
    }

    private void getData(String subjectType) {
        if ("Chọn môn".equals(subjectType)) {
            isShow(false);
            Utils.ShowToast(getContext(), "Vui lòng chọn môn học!");
        } else {
            isShow(true);
            loadGrades(subjectType);
        }
    }

    private void loadGrades(String subjectType) {
        loading(true);
        if(!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
            database.collection(Constants.KEY_COLLECTION_GRADES)
                    .whereEqualTo(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .whereEqualTo(Constants.KEY_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            clearAllGrades();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Grade grade = new Grade();
                                grade.setId(queryDocumentSnapshot.getId());
                                grade.setType(queryDocumentSnapshot.getString(Constants.KEY_GRADE_TYPE));
                                grade.setBand(queryDocumentSnapshot.getString(Constants.KEY_GRADE_BAND));
                                grade.setStudentId(queryDocumentSnapshot.getString(Constants.KEY_GRADE_STUDENT_ID));
                                grade.setSubject(queryDocumentSnapshot.getString(Constants.KEY_GRADE_SUBJECT));

                                if (subjectType == null || subjectType.equals(grade.getSubject())) {
                                    addGradeToList(grade);
                                }
                            }
                            updateAllRecyclerViews();
                            calculateAverage();
                            loading(false);
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu điểm!");
                        }
                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_GRADES)
                    .whereEqualTo(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_USER_INFO_ID))
                    .whereEqualTo(Constants.KEY_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            clearAllGrades();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Grade grade = new Grade();
                                grade.setId(queryDocumentSnapshot.getId());
                                grade.setType(queryDocumentSnapshot.getString(Constants.KEY_GRADE_TYPE));
                                grade.setBand(queryDocumentSnapshot.getString(Constants.KEY_GRADE_BAND));
                                grade.setStudentId(queryDocumentSnapshot.getString(Constants.KEY_GRADE_STUDENT_ID));
                                grade.setSubject(queryDocumentSnapshot.getString(Constants.KEY_GRADE_SUBJECT));

                                if (subjectType == null || subjectType.equals(grade.getSubject())) {
                                    addGradeToList(grade);
                                }
                            }
                            updateAllRecyclerViews();
                            calculateAverage();
                            loading(false);
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu điểm!");
                        }
                    });
        }
    }

    private void calculateAverage() {
        double total = 0;
        int count = 0;

        for (Grade grade : grades1) {
            total += Double.parseDouble(grade.getBand());
            count += 1;
        }

        for (Grade grade : grades2) {
            total += Double.parseDouble(grade.getBand()) * 2;
            count += 2;
        }

        for (Grade grade : grades3) {
            total += Double.parseDouble(grade.getBand()) * 3;
            count += 3;
        }

        for (Grade grade : grades4) {
            total += Double.parseDouble(grade.getBand()) * 3;
            count += 3;
        }

        double average = total / count;

        if (Double.isNaN(average)) {
            average = 0;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String formattedAverage = decimalFormat.format(average);
        binding.tvAverage.setText(formattedAverage);
    }

    private void clearAllGrades() {
        grades1.clear();
        grades2.clear();
        grades3.clear();
        grades4.clear();
    }

    private void addGradeToList(Grade grade) {
        switch (grade.getType()) {
            case "Miệng":
                grades1.add(grade);
                break;
            case "15 phút":
                grades2.add(grade);
                break;
            case "Giữa kỳ":
                grades3.add(grade);
                break;
            case "Cuối kỳ":
                grades4.add(grade);
                break;
        }
    }

    private void updateAllRecyclerViews() {
        isShow(true);
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        adapter3.notifyDataSetChanged();
        adapter4.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData(subjectType);
    }

    private void isShow(boolean isShow) {
        if (isShow) {
            binding.containerTranscript.setVisibility(View.VISIBLE);
        } else {
            binding.containerTranscript.setVisibility(View.GONE);
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.containerTranscript.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.containerTranscript.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGradeClick(Grade grade) {
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
            //Nothing
        } else {
            Intent intent = new Intent(getContext(), EditGradeActivity.class);
            intent.putExtra(Constants.KEY_GRADE, grade);
            startActivity(intent);

//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle("Xoá Điểm");
//            builder.setMessage("Bạn có đồng ý xoá điểm này không?");
//            builder.setPositiveButton("Đồng ý", (dialog, which) -> {
//                database.collection(Constants.KEY_COLLECTION_GRADES)
//                        .document(grade.id)
//                        .delete()
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Utils.ShowToast(getContext(), "Xóa điểm thành công");
//                                getData(subjectType);
//                            } else {
//                                Utils.ShowToast(getContext(), "Lỗi: " + task.getException().getMessage());
//                            }
//                        });
//            });
//
//            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
//
//            AlertDialog dialog = builder.create();
//            dialog.show();
        }
    }
}
