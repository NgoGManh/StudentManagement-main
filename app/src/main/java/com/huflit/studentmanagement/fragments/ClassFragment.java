package com.huflit.studentmanagement.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddClassActivity;
import com.huflit.studentmanagement.activities.AddTeacherActivity;
import com.huflit.studentmanagement.activities.DetailClassActivity;
import com.huflit.studentmanagement.adapters.ClassAdapter;
import com.huflit.studentmanagement.adapters.TeacherAdapter;
import com.huflit.studentmanagement.databinding.FragmentClassBinding;
import com.huflit.studentmanagement.databinding.FragmentTeacherBinding;
import com.huflit.studentmanagement.listeners.ClassListener;
import com.huflit.studentmanagement.models.Class;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassFragment extends Fragment implements ClassListener {
    private FragmentClassBinding binding;
    private PreferenceManager preferenceManager;
    private ClassAdapter classAdapter;
    private FirebaseFirestore database;
    private List<Class> classes = new ArrayList<>();
    private String type = "Chọn lớp";
    private String studentClassId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.classRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        listeners();
        getStudentClasses();
    }

    private void listeners() {
        SpinnerUtils.setSpinnerData(getContext(), SpinnerUtils.getClassList(), binding.spinner);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = binding.spinner.getSelectedItem().toString();
                getData(type);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddClassActivity.class);
                startActivity(intent);
            });
        } else {
            binding.spinner.setVisibility(View.GONE);
            binding.imgAdd.setVisibility(View.GONE);
        }
    }

    private void getStudentClasses() {
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .whereEqualTo(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_USER_INFO_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            List<Map<String, String>> studentClasses = (List<Map<String, String>>) queryDocumentSnapshot.get(Constants.KEY_STUDENT_CLASSES);
                            if (studentClasses != null) {
                                for (Map<String, String> studentClass : studentClasses) {
                                    String classId = studentClass.get("classId");
                                    getClassDataById(classId);
                                }
                            }
                        }
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                    }
                });
    }

    private void getClassDataById(String classId) {
        database.collection(Constants.KEY_COLLECTION_CLASSES)
                .document(classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Class Class = new Class();
                        Class.setId(task.getResult().getId());
                        Class.setName(task.getResult().getString(Constants.KEY_CLASS_NAME));
                        Class.setAmount(task.getResult().getString(Constants.KEY_CLASS_AMOUNT));
                        Class.setTermId(task.getResult().getString(Constants.KEY_CLASS_TERM_ID));
                        Class.setTeacher_name(task.getResult().getString(Constants.KEY_CLASS_TEACHER_NAME));
                        classes.add(Class);
                        updateRecyclerView();
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                    }
                });
    }

    private void getData(String type) {
        loading(true);
        if ("Chọn lớp".equals(type)) {
            if (!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
                database.collection(Constants.KEY_COLLECTION_CLASSES)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                classes.clear();
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    Class Class = new Class();
                                    Class.setId(queryDocumentSnapshot.getId());
                                    Class.setName(queryDocumentSnapshot.getString(Constants.KEY_CLASS_NAME));
                                    Class.setAmount(queryDocumentSnapshot.getString(Constants.KEY_CLASS_AMOUNT));
                                    Class.setTermId(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TERM_ID));
                                    Class.setTeacher_name(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TEACHER_NAME));
                                    classes.add(Class);
                                }
                                updateRecyclerView();
                                loading(false);
                            } else {
                                Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                            }
                        });
            } else {
                loading(false);
            }
        } else {
            if (!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
                database.collection(Constants.KEY_COLLECTION_CLASSES)
                        .whereEqualTo(Constants.KEY_CLASS_NAME, type)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                classes.clear();
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    Class Class = new Class();
                                    Class.setId(queryDocumentSnapshot.getId());
                                    Class.setName(queryDocumentSnapshot.getString(Constants.KEY_CLASS_NAME));
                                    Class.setAmount(queryDocumentSnapshot.getString(Constants.KEY_CLASS_AMOUNT));
                                    Class.setTermId(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TERM_ID));
                                    Class.setTeacher_name(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TEACHER_NAME));
                                    classes.add(Class);
                                }
                                updateRecyclerView();
                                loading(false);
                            } else {
                                Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                            }
                        });
            } else {
                database.collection(Constants.KEY_COLLECTION_CLASSES)
                        .whereEqualTo(Constants.KEY_CLASS_NAME, type)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                classes.clear();
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    Class Class = new Class();
                                    Class.setId(queryDocumentSnapshot.getId());
                                    Class.setName(queryDocumentSnapshot.getString(Constants.KEY_CLASS_NAME));
                                    Class.setAmount(queryDocumentSnapshot.getString(Constants.KEY_CLASS_AMOUNT));
                                    Class.setTermId(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TERM_ID));
                                    Class.setTeacher_name(queryDocumentSnapshot.getString(Constants.KEY_CLASS_TEACHER_NAME));
                                    classes.add(Class);
                                }
                                updateRecyclerView();
                                loading(false);
                            } else {
                                Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu lớp học!");
                            }
                        });
            }
        }
    }

    private void updateRecyclerView() {
        if (classAdapter == null) {
            classAdapter = new ClassAdapter(classes, this);
            binding.classRecyclerView.setAdapter(classAdapter);
        } else {
            classAdapter.notifyDataSetChanged();
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.classRecyclerView.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.classRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData(type);
    }

    @Override
    public void onClassClick(Class Class) {
        preferenceManager.putString(Constants.KEY_CLASS_ID, Class.id);

        Intent intent = new Intent(getActivity(), DetailClassActivity.class);
        intent.putExtra(Constants.KEY_CLASS_ID, Class.id);
        intent.putExtra(Constants.KEY_CLASS, Class);
        startActivity(intent);
    }
}