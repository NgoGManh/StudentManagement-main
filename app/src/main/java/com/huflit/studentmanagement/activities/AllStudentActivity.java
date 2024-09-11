package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.adapters.StudentAdapter;
import com.huflit.studentmanagement.databinding.ActivityAddStudentClassBinding;
import com.huflit.studentmanagement.databinding.ActivityAllStudentBinding;
import com.huflit.studentmanagement.listeners.StudentListener;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllStudentActivity extends AppCompatActivity implements StudentListener {

    private ActivityAllStudentBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int id = 0;
    private List<Student> students = new ArrayList<>();
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAllStudentBinding.inflate(getLayoutInflater());
        binding.studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getData();

        binding.imgAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddStudentActivity.class);
            startActivity(intent);
        });

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchStudents(String query) {
        if (query.isEmpty()) {
            getData();
            return;
        }

        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        students.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            String id = queryDocumentSnapshot.getId();
                            String name = queryDocumentSnapshot.getString(Constants.KEY_STUDENT_NAME);
                            if (id.contains(query) || name.toLowerCase().contains(query.toLowerCase())) {
                                Student student = new Student();
                                student.setId(id);
                                student.setName(name);
                                student.setPhone(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_PHONE));
                                student.setEmail(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_EMAIL));
                                student.setAddress(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_ADDRESS));
                                student.setGender(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_GENDER));
                                student.setDob(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_STUDENT_DOB)));
                                student.setImage(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_IMAGE));
                                student.setClasses((List<Map<String, String>>) queryDocumentSnapshot.get(Constants.KEY_STUDENT_CLASSES));
                                students.add(student);
                            }
                        }
                        loading(false);
                        updateRecyclerView();
                    } else {
                        Utils.ShowToast(getApplicationContext(), "Không tìm thấy dữ liệu học sinh!");
                    }
                });
    }

    private void getData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        students.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Student student = new Student();
                            student.setId(queryDocumentSnapshot.getId());
                            student.setName(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_NAME));
                            student.setPhone(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_PHONE));
                            student.setEmail(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_EMAIL));
                            student.setAddress(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_ADDRESS));
                            student.setGender(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_GENDER));
                            student.setDob(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_STUDENT_DOB)));
                            student.setImage(queryDocumentSnapshot.getString(Constants.KEY_STUDENT_IMAGE));
                            student.setClasses((List<Map<String, String>>) queryDocumentSnapshot.get(Constants.KEY_STUDENT_CLASSES));
                            students.add(student);
                        }
                        loading(false);
                        updateRecyclerView();
                    } else {
                        Utils.ShowToast(getApplicationContext(), "Không tìm thấy dữ liệu học sinh!");
                        loading(false);
                    }
                });
    }

    private void updateRecyclerView() {
        if (studentAdapter == null) {
            studentAdapter = new StudentAdapter(students, this, "all", preferenceManager);
            binding.studentRecyclerView.setAdapter(studentAdapter);
        } else {
            studentAdapter.notifyDataSetChanged();
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onStudentClick(Student student) {
        Intent intent = new Intent(getApplicationContext(), EditStudentActivity.class);
        intent.putExtra(Constants.KEY_STUDENT, student);
        startActivity(intent);
    }

    @Override
    public void onStudentLongClick(Student student) {
        //do nothing
    }

    @Override
    public void onStudentChange(Student student) {
        //do nothing
    }
}