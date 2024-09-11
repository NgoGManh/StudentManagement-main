package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.activities.AddTeacherActivity;
import com.huflit.studentmanagement.activities.DetailStudentActivity;
import com.huflit.studentmanagement.activities.DetailTeacherActivity;
import com.huflit.studentmanagement.activities.EditTeacherActivity;
import com.huflit.studentmanagement.adapters.TeacherAdapter;
import com.huflit.studentmanagement.databinding.FragmentTeacherBinding;
import com.huflit.studentmanagement.listeners.TeacherListener;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.ExcelUtils;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherFragment extends Fragment implements TeacherListener {

    private FragmentTeacherBinding binding;
    private PreferenceManager preferenceManager;
    private TeacherAdapter teacherAdapter;
    private FirebaseFirestore database;
    private List<Teacher> teachers = new ArrayList<>();
    private String type = "Chọn môn";

    private static final int PICK_FILE_REQUEST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTeacherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.teacherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        listeners();
    }

    private void listeners() {
        SpinnerUtils.setSpinnerData(getContext(), SpinnerUtils.getSubjectList(), binding.subjectSpinner);

        binding.subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = binding.subjectSpinner.getSelectedItem().toString();
                getData(type);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddTeacherActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
    }

    private void getData(String type) {
        loading(true);
        if ("Chọn môn".equals(type)) {
            database.collection(Constants.KEY_COLLECTION_TEACHERS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            teachers.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Teacher teacher = new Teacher();
                                teacher.setId(queryDocumentSnapshot.getId());
                                teacher.setName(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_NAME));
                                teacher.setPhone(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_PHONE));
                                teacher.setEmail(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_EMAIL));
                                teacher.setSubject(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_SUBJECT));
                                teacher.setGender(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_GENDER));
                                teacher.setDob(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_TEACHER_DOB)));
                                teacher.setImage(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_IMAGE));
                                teachers.add(teacher);
                            }
                            updateRecyclerView();
                            loading(false);
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu giáo viên!");
                        }
                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_TEACHERS)
                    .whereEqualTo(Constants.KEY_TEACHER_SUBJECT, type)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            teachers.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Teacher teacher = new Teacher();
                                teacher.setId(queryDocumentSnapshot.getId());
                                teacher.setName(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_NAME));
                                teacher.setPhone(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_PHONE));
                                teacher.setEmail(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_EMAIL));
                                teacher.setSubject(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_SUBJECT));
                                teacher.setGender(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_GENDER));
                                teacher.setDob(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_TEACHER_DOB)));
                                teacher.setImage(queryDocumentSnapshot.getString(Constants.KEY_TEACHER_IMAGE));
                                teachers.add(teacher);
                            }
                            updateRecyclerView();
                            loading(false);
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu giáo viên!");
                        }
                    });
        }
    }

    private void updateRecyclerView() {
        if (teacherAdapter == null) {
            teacherAdapter = new TeacherAdapter(teachers, this);
            binding.teacherRecyclerView.setAdapter(teacherAdapter);
        } else {
            teacherAdapter.notifyDataSetChanged();
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.teacherRecyclerView.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.teacherRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData(type);
    }

    @Override
    public void onTeacherClick(Teacher teacher) {
            preferenceManager.putString(Constants.KEY_TEACHER_ID, teacher.id);

            Intent intent = new Intent(getActivity(), DetailTeacherActivity.class);
            intent.putExtra(Constants.KEY_TEACHER, teacher);
            startActivity(intent);
    }

    @Override
    public void onTeacherLongClick(Teacher teacher) {
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            preferenceManager.putString(Constants.KEY_TEACHER_ID, teacher.id);

            Intent intent = new Intent(getActivity(), EditTeacherActivity.class);
            intent.putExtra(Constants.KEY_TEACHER, teacher);
            startActivity(intent);
        }
    }
}