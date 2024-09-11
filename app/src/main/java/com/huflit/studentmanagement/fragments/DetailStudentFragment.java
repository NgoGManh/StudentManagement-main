package com.huflit.studentmanagement.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.activities.AddStudentActivity;
import com.huflit.studentmanagement.adapters.StudentAdapter;
import com.huflit.studentmanagement.databinding.FragmentDetailStudentBinding;
import com.huflit.studentmanagement.databinding.FragmentStudentBinding;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class DetailStudentFragment extends Fragment {

    private FragmentDetailStudentBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Student student;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailStudentBinding.inflate(inflater, container, false);
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
            getData();
    }

    private void getData() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .document(preferenceManager.getString(Constants.KEY_STUDENT_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        student = new Student();
                        student.setId(document.getId());
                        student.setDob(Utils.getReadableDataTime(document.getDate(Constants.KEY_STUDENT_DOB)));
                        student.setAddress(document.getString(Constants.KEY_STUDENT_ADDRESS));
                        student.setPhone(document.getString(Constants.KEY_STUDENT_PHONE));
                        student.setGender(document.getString(Constants.KEY_STUDENT_GENDER));
                        student.setEmail(document.getString(Constants.KEY_STUDENT_EMAIL));
                        student.setImage(document.getString(Constants.KEY_STUDENT_IMAGE));

                        setData(student.getDob(), student.getAddress(), student.getPhone(), student.getEmail(), student.getImage(), student.getGender(), student.getId());
                    } else {
                        Utils.ShowToast(getContext(), "Không tìm thấy dữ liệu học sinh!");
                    }
                });
    }



    private void setData(String dob, String address, String phone, String email, String image, String gender, String id) {
        binding.tvDOB.setText(dob);
        binding.tvGender.setText(gender);
        binding.tvId.setText(id);
        binding.tvAddress.setText(address);
        binding.tvPhone.setText(phone);
        binding.tvEmail.setText(email);
        if (image != null && !image.isEmpty()) {
            Bitmap bitmap = Utils.getBitmapFromEncodedString(image);
            if (bitmap != null) {
                binding.imgStudent.setImageBitmap(bitmap);
            } else {
                binding.imgStudent.setImageResource(R.drawable.ic_default_image);
            }
        } else {
            binding.imgStudent.setImageResource(R.drawable.ic_default_image);
        }

        binding.imgStudent.setOnClickListener(v -> {
            if (image != null && !image.isEmpty()) {
                Utils.onImageClick(image, getContext());
            }
        });
        loading(false);
    }


    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.fragContainer.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.fragContainer.setVisibility(View.VISIBLE);
        }
    }
}