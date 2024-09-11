package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddStudentActivity;
import com.huflit.studentmanagement.activities.AllStudentActivity;
import com.huflit.studentmanagement.activities.DetailStudentActivity;
import com.huflit.studentmanagement.activities.LoginActivity;
import com.huflit.studentmanagement.databinding.FragmentMoreBinding;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import okhttp3.internal.Util;

public class MoreFragment extends Fragment {
    private FragmentMoreBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getContext());
        listeners();
    }

    private void listeners(){
        binding.btLogout.setOnClickListener(v -> {
            signOut();
        });

        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
            binding.btAddStudent.setVisibility(View.GONE);
        }

        binding.btAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllStudentActivity.class);
            startActivity(intent);
        });
    }

    private void signOut() {
        loading(true);
        Utils.ShowToast(getContext(), "Đang đăng xuất khỏi tài khoản...");
        preferenceManager.clear();
        startActivity(new Intent(getContext(), LoginActivity.class));
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btLogout.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btLogout.setVisibility(View.VISIBLE);
        }
    }
}