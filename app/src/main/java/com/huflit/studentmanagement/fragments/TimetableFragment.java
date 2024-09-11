package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddTimetableActivity;
import com.huflit.studentmanagement.activities.DetailTimeTableActivity;
import com.huflit.studentmanagement.databinding.FragmentTimetableBinding;
import com.huflit.studentmanagement.models.Timetable;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableFragment extends Fragment {
    private FragmentTimetableBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<Timetable> timetables = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTimetableBinding.inflate(inflater, container, false);
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
        createTable();
        loadData();
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddTimetableActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void createTable() {
        int rows = 9;
        int columns = 6;

        for (int i = 1; i <= rows; i++) {
            TableRow tableRow = new TableRow(getContext());

            TextView lessonNumber = new TextView(getContext());
            lessonNumber.setBackgroundResource(R.drawable.cell_border);
            lessonNumber.setText(String.valueOf(i));
            lessonNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
            lessonNumber.setPadding(8, 8, 8, 8);
            tableRow.setBackgroundResource(R.drawable.normal_row_border);
            tableRow.addView(lessonNumber);

            for (int j = 1; j <= columns; j++) {
                TextView textView = new TextView(getContext());
                textView.setBackgroundResource(R.drawable.cell_border);
                textView.setPadding(8, 8, 8, 8);
                textView.setText("");

                textView.setTag(R.id.tag_period, i);
                textView.setTag(R.id.tag_day, j + 1);

                textView.setOnClickListener(v -> {
                    int period = (int) v.getTag(R.id.tag_period);
                    int day = (int) v.getTag(R.id.tag_day);
                    Timetable timetable = null;
                    for (Timetable t : timetables) {
                        if (t.getPeriod().equals(String.valueOf(period)) && t.getDay().equals(String.valueOf(day))) {
                            timetable = t;
                            break;
                        }
                    }
                    if (timetable != null) {
                        Intent intent = new Intent(getContext(), DetailTimeTableActivity.class);
                        intent.putExtra(Constants.KEY_TIMETABLE, timetable);
                        startActivity(intent);
                    }
                });

                tableRow.addView(textView);
            }

            binding.tablelayout.addView(tableRow);
        }
    }

    private void loadData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_TIMETABLE)
                .whereEqualTo(Constants.KEY_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<Integer, Map<Integer, Timetable>> scheduleMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Timetable timetable = document.toObject(Timetable.class);
                            String day = timetable.getDay();
                            String period = timetable.getPeriod();

                            timetables.add(timetable);

                            if (!scheduleMap.containsKey(Integer.parseInt(period))) {
                                scheduleMap.put(Integer.parseInt(period), new HashMap<>());
                            }
                            scheduleMap.get(Integer.parseInt(period)).put(Integer.parseInt(day), timetable);
                        }
                        updateTable(scheduleMap);
                        loading(false);
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu thời khóa biểu!");
                    }
                });
    }

    private void updateTable(Map<Integer, Map<Integer, Timetable>> scheduleMap) {
        for (int period = 1; period <= 9; period++) {
            TableRow tableRow = (TableRow) binding.tablelayout.getChildAt(period);

            for (int day = 2; day <= 7; day++) {
                TextView textView = (TextView) tableRow.getChildAt(day - 1);
                String subject = "";
                if (scheduleMap.containsKey(period) && scheduleMap.get(period).containsKey(day)) {
                    subject = scheduleMap.get(period).get(day).getSubject();
                }
                textView.setText(subject);
            }
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.tablelayout.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.tablelayout.setVisibility(View.VISIBLE);
        }
    }
}