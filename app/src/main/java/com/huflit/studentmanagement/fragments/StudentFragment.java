package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddStudentActivity;
import com.huflit.studentmanagement.activities.AddStudentClassActivity;
import com.huflit.studentmanagement.activities.AddTeacherActivity;
import com.huflit.studentmanagement.activities.DetailClassActivity;
import com.huflit.studentmanagement.activities.DetailStudentActivity;
import com.huflit.studentmanagement.activities.EditStudentActivity;
import com.huflit.studentmanagement.adapters.StudentAdapter;
import com.huflit.studentmanagement.adapters.TeacherAdapter;
import com.huflit.studentmanagement.databinding.FragmentStudentBinding;
import com.huflit.studentmanagement.databinding.FragmentTeacherBinding;
import com.huflit.studentmanagement.listeners.StudentListener;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentFragment extends Fragment implements StudentListener {

    private FragmentStudentBinding binding;
    private PreferenceManager preferenceManager;
    private StudentAdapter studentAdapter;
    private FirebaseFirestore database;
    private List<Student> students = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStudentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.studentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        listeners();
    }

    private void listeners() {
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddStudentClassActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
    }

    private void getData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        students.clear();
                        String currentClassId = preferenceManager.getString(Constants.KEY_CLASS_ID);
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            List<Map<String, String>> classes = (List<Map<String, String>>) queryDocumentSnapshot.get(Constants.KEY_STUDENT_CLASSES);
                            if (classes != null) {
                                for (Map<String, String> classMap : classes) {
                                    if (classMap.containsValue(currentClassId)) {
                                        Student student = new Student();
                                        student.setId(queryDocumentSnapshot.getId());
                                        student.setName(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_NAME));
                                        student.setPhone(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_PHONE));
                                        student.setEmail(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_EMAIL));
                                        student.setAddress(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_ADDRESS));
                                        student.setGender(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_GENDER));
                                        student.setDob(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_STUDENT_DOB)));
                                        student.setImage(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_IMAGE));
                                        student.setClasses(classes);
                                        students.add(student);
                                        break;
                                    }
                                }
                            }
                        }
                        loading(false);
                        updateRecyclerView();
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học sinh!");
                        loading(false);
                    }
                });
    }

    private void updateRecyclerView() {
        if (studentAdapter == null) {
            studentAdapter = new StudentAdapter(students, this, "main", preferenceManager);
            binding.studentRecyclerView.setAdapter(studentAdapter);
        } else {
            studentAdapter.notifyDataSetChanged();
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.studentRecyclerView.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.studentRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onStudentClick(Student student) {
        preferenceManager.putString(Constants.KEY_STUDENT_ID, student.id);

        Intent intent = new Intent(getActivity(), DetailStudentActivity.class);
        intent.putExtra(Constants.KEY_STUDENT, student);
        startActivity(intent);
    }

    @Override
    public void onStudentLongClick(Student student) {
        if (!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
            List<Map<String, String>> updatedClasses = new ArrayList<>();
            String classIdToRemove = preferenceManager.getString(Constants.KEY_CLASS_ID);

            if (student.getClasses() != null) {
                for (Map<String, String> studentClass : student.getClasses()) {
                    if (!studentClass.get("classId").equals(classIdToRemove)) {
                        updatedClasses.add(studentClass);
                    }
                }
            }

            Student updatedStudent = new Student();
            updatedStudent.setClasses(updatedClasses);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Xoá Học Sinh");
            builder.setMessage("Bạn có đồng ý xoá học sinh khỏi lớp này không?");
            builder.setPositiveButton("Đồng ý", (dialog, which) -> {
                loading(true);
                database.collection(Constants.KEY_COLLECTION_STUDENTS)
                        .document(student.getId())
                        .update(Constants.KEY_STUDENT_CLASSES, updatedClasses)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                database.collection(Constants.KEY_COLLECTION_GRADES)
                                        .whereEqualTo(Constants.KEY_GRADE_STUDENT_ID, student.getId())
                                        .whereEqualTo(Constants.KEY_CLASS_ID, classIdToRemove)
                                        .get()
                                        .addOnCompleteListener(
                                            task1 -> {
                                                if (task1.isSuccessful() && task1.getResult() != null) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                                        database.collection(Constants.KEY_COLLECTION_GRADES)
                                                                .document(queryDocumentSnapshot.getId())
                                                                .delete();
                                                    }
                                                }
                                            }
                                        );
                                database.collection(Constants.KEY_COLLECTION_TUITIONS)
                                        .whereEqualTo(Constants.KEY_TUITION_STUDENT_ID, student.getId())
                                        .whereEqualTo(Constants.KEY_CLASS_ID, classIdToRemove)
                                        .get()
                                        .addOnCompleteListener(
                                            task1 -> {
                                                if (task1.isSuccessful() && task1.getResult() != null) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                                        database.collection(Constants.KEY_COLLECTION_TUITIONS)
                                                                .document(queryDocumentSnapshot.getId())
                                                                .delete();
                                                    }
                                                }
                                            }
                                        );
                                Utils.ShowToast(getContext(), "Xóa học sinh thành công");
                                loading(false);
                                getData();
                            } else {
                                loading(false);
                                Utils.ShowToast(getContext(), "Lỗi: " + task.getException().getMessage());
                            }
                        });
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                loading(false);
                dialog.cancel();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onStudentChange(Student student) {
        List<String> classNames = new ArrayList<>();
        List<String> classIds = new ArrayList<>();

        database.collection(Constants.KEY_COLLECTION_CLASSES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            classNames.add(queryDocumentSnapshot.getString(Constants.KEY_CLASS_NAME));
                            classIds.add(queryDocumentSnapshot.getId());
                        }
                        showChangeClassDialog(student, classNames, classIds);
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                    }
                });
    }

    private void showChangeClassDialog(Student student, List<String> classNames, List<String> classIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn Lớp Mới");
        builder.setItems(classNames.toArray(new String[0]), (dialog, which) -> {
            String newClassId = classIds.get(which);
            changeStudentClass(student, newClassId);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeStudentClass(Student student, String newClassId) {
        String currentClassId = preferenceManager.getString(Constants.KEY_CLASS_ID);
        List<Map<String, String>> updatedClasses = new ArrayList<>();

        if (student.getClasses() != null) {
            for (Map<String, String> studentClass : student.getClasses()) {
                if (!studentClass.get("classId").equals(currentClassId)) {
                    updatedClasses.add(studentClass);
                }
            }
            Map<String, String> newClassMap = new HashMap<>();
            newClassMap.put("classId", newClassId);
            updatedClasses.add(newClassMap);
        }

        Student updatedStudent = new Student();
        updatedStudent.setClasses(updatedClasses);

        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .document(student.getId())
                .update(Constants.KEY_STUDENT_CLASSES, updatedClasses)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //tôi cần chuyển cả classID ở học phí và điểm số
                        database.collection(Constants.KEY_COLLECTION_GRADES)
                                .whereEqualTo(Constants.KEY_GRADE_STUDENT_ID, student.getId())
                                .whereEqualTo(Constants.KEY_CLASS_ID, currentClassId)
                                .get()
                                .addOnCompleteListener(
                                        task1 -> {
                                            if (task1.isSuccessful() && task1.getResult() != null) {
                                                for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                                    database.collection(Constants.KEY_COLLECTION_GRADES)
                                                            .document(queryDocumentSnapshot.getId())
                                                            .update(Constants.KEY_CLASS_ID, newClassId);
                                                }
                                            }
                                        }
                                );
                        database.collection(Constants.KEY_COLLECTION_TUITIONS)
                                .whereEqualTo(Constants.KEY_TUITION_STUDENT_ID, student.getId())
                                .whereEqualTo(Constants.KEY_CLASS_ID, currentClassId)
                                .get()
                                .addOnCompleteListener(
                                        task1 -> {
                                            if (task1.isSuccessful() && task1.getResult() != null) {
                                                for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                                    database.collection(Constants.KEY_COLLECTION_TUITIONS)
                                                            .document(queryDocumentSnapshot.getId())
                                                            .update(Constants.KEY_CLASS_ID, newClassId);
                                                }
                                            }
                                        }
                                );
                        Utils.ShowToast(getContext(), "Chuyển lớp thành công");
                        loading(false);
                        getData();
                    } else {
                        loading(false);
                        Utils.ShowToast(getContext(), "Lỗi: " + task.getException().getMessage());
                    }
                });
    }
}