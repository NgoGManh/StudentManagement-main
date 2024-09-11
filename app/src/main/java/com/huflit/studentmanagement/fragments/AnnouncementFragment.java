package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddAnnouncementActivity;
import com.huflit.studentmanagement.activities.AddStudentActivity;
import com.huflit.studentmanagement.activities.DetailAnnouncementActivity;
import com.huflit.studentmanagement.activities.EditAnnouncementActivity;
import com.huflit.studentmanagement.adapters.AnnouncementAdapter;
import com.huflit.studentmanagement.adapters.StudentAdapter;
import com.huflit.studentmanagement.databinding.FragmentAnnouncementBinding;
import com.huflit.studentmanagement.listeners.AnnouncementListener;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementFragment extends Fragment implements AnnouncementListener {

    private FragmentAnnouncementBinding binding;
    private PreferenceManager preferenceManager;
    private AnnouncementAdapter announcementAdapter;
    private FirebaseFirestore database;
    private List<Announcement> announcements = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.announcementRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        listeners();
    }

    private void listeners() {
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddAnnouncementActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
        getData();
    }

    private void getData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENT)
                .whereEqualTo(Constants.KEY_ANNOUNCEMENT_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        announcements.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Announcement announcement = new Announcement();
                            announcement.setId(queryDocumentSnapshot.getId());
                            announcement.setTitle(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_TITLE));
                            announcement.setContent(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_CONTENT));
                            announcement.setTime(Utils.getReadableDataTime(queryDocumentSnapshot.getDate(Constants.KEY_ANNOUNCEMENT_TIME)));
                            announcement.setClassId(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_CLASS_ID));
                            announcements.add(announcement);
                        }
                        loading(false);
                        updateRecyclerView();
                    } else {
                        loading(false);
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu thông báo nào!");
                    }
                });
    }

    private void updateRecyclerView() {
        if (announcementAdapter == null) {
            announcementAdapter = new AnnouncementAdapter(announcements, this);
            binding.announcementRecyclerView.setAdapter(announcementAdapter);
        } else {
            announcementAdapter.notifyDataSetChanged();
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.announcementRecyclerView.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.announcementRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onAnnouncementClick(Announcement announcement) {
            Intent intent = new Intent(getActivity(), DetailAnnouncementActivity.class);
            intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
            startActivity(intent);

    }

    @Override
    public void onAnnouncementLongClick(Announcement announcement) {
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
        } else {
            Intent intent = new Intent(getActivity(), EditAnnouncementActivity.class);
            intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
            startActivity(intent);
        }
    }
}