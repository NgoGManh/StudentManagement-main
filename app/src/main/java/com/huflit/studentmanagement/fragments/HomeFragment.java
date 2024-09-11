package com.huflit.studentmanagement.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.FragmentHomeBinding;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.models.Tuition;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<Student> students = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        listeners();

    }

    private void listeners() {
        getStudentsFromDatabase();
        getTeachersFromDatabase();
        getPieChartData(students);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPieChartData(students);
    }

    private void getStudentsFromDatabase() {
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        students.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = new Student();
                            student.setId(document.getId());
                            student.setName(document.getString(Constants.KEY_STUDENT_NAME));
                            student.setDob(Utils.getReadableDataTime(document.getDate(Constants.KEY_STUDENT_DOB)));
                            student.setGender(document.getString(Constants.KEY_STUDENT_GENDER));
                            student.setAddress(document.getString(Constants.KEY_STUDENT_ADDRESS));
                            student.setPaid(document.getBoolean(Constants.KEY_STUDENT_IS_PAID));
                            student.setClasses((List<Map<String, String>>) document.get(Constants.KEY_STUDENT_CLASSES));
                            students.add(student);
                        }
                        getPieChartData(students);
                    }
                });
    }

    private List<Student> filterStudentsByCurrentYear(List<Student> students, int currentYear) {
        List<Student> filteredStudents = new ArrayList<>();
        String currentYearString = String.valueOf(currentYear).substring(2, 4);

        for (Student student : students) {
            if (student.getClasses() != null) {
                for (Map<String, String> classMap : student.getClasses()) {
                    String classId = classMap.get("classId");
                    String yearString = classId.substring(classId.length() - 4, classId.length() - 2);
                    if (yearString.equals(currentYearString)) {
                        filteredStudents.add(student);
                        break;
                    }
                }
            }
        }
        return filteredStudents;
    }

    private Map<String, Integer> countStudentsByGrade(List<Student> students) {
        Map<String, Integer> gradeCountMap = new LinkedHashMap<>();
        gradeCountMap.put("10", 0);
        gradeCountMap.put("11", 0);
        gradeCountMap.put("12", 0);

        for (Student student : students) {
            if (student.getClasses() != null) {
                for (Map<String, String> classMap : student.getClasses()) {
                    String classId = classMap.get("classId");
                    String grade = classId.substring(0, 2);

                    if (gradeCountMap.containsKey(grade)) {
                        gradeCountMap.put(grade, gradeCountMap.get(grade) + 1);
                        break;
                    }
                }
            }
        }
        return gradeCountMap;
    }

    private void getPieChartData(List<Student> students) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        List<Student> currentYearStudents = filterStudentsByCurrentYear(students, currentYear);

        Map<String, Integer> currentYearCount = countStudentsByGrade(currentYearStudents);

        List<PieEntry> entries = new ArrayList<>();
        List<PieEntry> sortedEntries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : currentYearCount.entrySet()) {
            sortedEntries.add(new PieEntry(entry.getValue(), "Khối " + entry.getKey()));
        }

        Collections.sort(sortedEntries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry o1, PieEntry o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        entries.addAll(sortedEntries);

        PieDataSet dataSet = new PieDataSet(entries, "Năm Học " + currentYear);
        PieData pieData = new PieData(dataSet);

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        binding.pieChart.setData(pieData);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(61f);

        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawEntryLabels(false);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setExtraOffsets(5, 10, 5, 5);

        binding.pieChart.invalidate();
    }

    private void getTeachersFromDatabase() {
        database.collection(Constants.KEY_COLLECTION_TEACHERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        teachers.clear();
                        for (int i = 0; i < task.getResult().size(); i++) {
                            Teacher teacher = new Teacher();
                            teacher.setId(task.getResult().getDocuments().get(i).getId());
                            teacher.setName(task.getResult().getDocuments().get(i).getString(Constants.KEY_TEACHER_NAME));
                            teacher.setSubject(task.getResult().getDocuments().get(i).getString(Constants.KEY_TEACHER_SUBJECT));
                            teachers.add(teacher);
                        }
                        getBarChartData(teachers);
                    }
                });
    }

    private Map<String, Integer> countTeachersBySubject(List<Teacher> teachers) {
        Map<String, Integer> subjectCountMap = new LinkedHashMap<>();

        for (Teacher teacher : teachers) {
            String subject = teacher.getSubject();

            if (subjectCountMap.containsKey(subject)) {
                subjectCountMap.put(subject, subjectCountMap.get(subject) + 1);
            } else {
                subjectCountMap.put(subject, 1);
            }
        }
        return subjectCountMap;
    }

    private void getBarChartData(List<Teacher> teachers) {
        Map<String, Integer> teacherCountBySubject = countTeachersBySubject(teachers);

        List<BarEntry> entries = new ArrayList<>();
        List<String> subjects = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : teacherCountBySubject.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            subjects.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Môn dạy");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);

        binding.barChart.setData(barData);

        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setFitBars(true);
        binding.barChart.animateY(1000);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(subjects));

        YAxis leftAxis = binding.barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = binding.barChart.getAxisRight();
        rightAxis.setEnabled(false);

        binding.barChart.invalidate();
    }
}