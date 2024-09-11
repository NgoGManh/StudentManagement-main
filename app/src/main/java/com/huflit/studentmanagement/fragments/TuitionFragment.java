package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddStudentActivity;
import com.huflit.studentmanagement.activities.AddTuitionActivity;
import com.huflit.studentmanagement.activities.DetailTuitionActivity;
import com.huflit.studentmanagement.databinding.FragmentDetailStudentBinding;
import com.huflit.studentmanagement.databinding.FragmentTuitionBinding;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Tuition;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

public class TuitionFragment extends Fragment {
    private FragmentTuitionBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Tuition tuition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTuitionBinding.inflate(inflater, container, false);
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
        if (preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Quản lý")) {
            binding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddTuitionActivity.class);
                startActivity(intent);
            });
        } else {
            binding.imgAdd.setVisibility(View.GONE);
        }
        getData();
    }

    private void getData() {
        loading(true);
        if(!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")){
            database.collection(Constants.KEY_COLLECTION_TUITIONS)
                    .whereEqualTo(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .whereEqualTo(Constants.KEY_TUITION_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                Tuition tuition = new Tuition();
                                tuition.setId(document.getId());
                                tuition.setAmount(document.getString(Constants.KEY_TUITION_AMOUNT));
                                tuition.setStatus(document.getString(Constants.KEY_TUITION_STATUS));

                                String date = document.getString(Constants.KEY_TUITION_DATE);
                                if (tuition.getStatus().equals("Đã thanh toán") && date != null) {
                                    tuition.setDate(date);
                                } else {
                                    tuition.setDate("");
                                }

                                tuition.setStudentId(document.getString(Constants.KEY_STUDENT_ID));

                                setData(tuition.getAmount(), tuition.getStatus(), tuition.getDate());

                                binding.container.setOnClickListener(v -> {
                                    preferenceManager.putObject(Constants.KEY_TUITION, tuition);
                                    Intent intent = new Intent(getContext(), DetailTuitionActivity.class);
                                    startActivity(intent);
                                });
                            } else {
                                binding.container.setVisibility(View.GONE);
                                Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học phí!");
                            }
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học phí!");
                        }
                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_TUITIONS)
                    .whereEqualTo(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_USER_INFO_ID))
                    .whereEqualTo(Constants.KEY_TUITION_CLASS_ID, preferenceManager.getString(Constants.KEY_CLASS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                Tuition tuition = new Tuition();
                                tuition.setId(document.getId());
                                tuition.setAmount(document.getString(Constants.KEY_TUITION_AMOUNT));
                                tuition.setStatus(document.getString(Constants.KEY_TUITION_STATUS));

                                String date = document.getString(Constants.KEY_TUITION_DATE);
                                if (tuition.getStatus().equals("Đã thanh toán") && date != null) {
                                    tuition.setDate(date);
                                } else {
                                    tuition.setDate("");
                                }

                                tuition.setStudentId(document.getString(Constants.KEY_STUDENT_ID));

                                setData(tuition.getAmount(), tuition.getStatus(), tuition.getDate());

                                binding.container.setOnClickListener(v -> {
                                    preferenceManager.putObject(Constants.KEY_TUITION, tuition);
                                    Intent intent = new Intent(getContext(), DetailTuitionActivity.class);
                                    startActivity(intent);
                                });
                            } else {
                                binding.container.setVisibility(View.GONE);
                                Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học phí!");
                            }
                        } else {
                            Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học phí!");
                        }
                    });
        }

    }



    private void setData(String amount, String status, String date) {
        binding.tvPrice.setText(Utils.formatNumber(amount) + " VNĐ");
        if (status.equals("Đã thanh toán")) {
            binding.tvPaid.setVisibility(View.VISIBLE);
            binding.tvNotPaid.setVisibility(View.GONE);
            binding.framePaidDate.setVisibility(View.VISIBLE);
            if (date != null) {
                binding.tvPaidDate.setText(date);
            }
        } else {
            binding.tvPaid.setVisibility(View.GONE);
            binding.framePaidDate.setVisibility(View.GONE);
            binding.tvNotPaid.setVisibility(View.VISIBLE);
        }
        loading(false);
    }


    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.container.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.container.setVisibility(View.VISIBLE);
        }
    }
}