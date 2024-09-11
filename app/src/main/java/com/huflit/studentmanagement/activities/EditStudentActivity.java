package com.huflit.studentmanagement.activities;

import static com.huflit.studentmanagement.utilities.Utils.convertStringToTimestamp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityEditAnnouncementBinding;
import com.huflit.studentmanagement.databinding.ActivityEditStudentBinding;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

public class EditStudentActivity extends AppCompatActivity {
    private ActivityEditStudentBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Student student;
    private String encodedImage;
    private boolean isDateSelected = false;
    private boolean isImgChoosen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityEditStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);

        getData();
        binding.btConfirm.setOnClickListener(v -> {
            if (isValidStudentDetails()) {
                editStudent();
            }
        });
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
        binding.imageDelete.setOnClickListener(v -> {
            deleteStudent();
        });
        binding.btPickDOB.setOnClickListener(v -> showDatePickerDialog(binding.tvDOB, true));
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImage.launch(intent);
        });
    }


    private void getData() {
        if (student != null) {
            String image = student.getImage();

            if (image != null && !image.isEmpty()) {
                Bitmap bitmap = Utils.getBitmapFromEncodedString(image);
                if (bitmap != null) {
                    binding.imageStudent.setImageBitmap(bitmap);
                    isImgChoosen = true;
                }
            }
            binding.inputName.setText(student.getName());
            binding.inputAddress.setText(student.getAddress());
            binding.inputEmail.setText(student.getEmail());
            binding.inputPhone.setText(student.getPhone());
            binding.tvDOB.setText(student.getDob());


            if (student.gender.equals("Nam")) {
                binding.rbMale.setChecked(true);
            } else {
                binding.rbFemale.setChecked(true);
            }
        }
    }


    private void showDatePickerDialog(TextView textView, boolean isDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditStudentActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    textView.setText(selectedDate);

                    if (isDate) {
                        isDateSelected = true;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void editStudent() {
        loading(true);
        int checkId = binding.rgGender.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = binding.getRoot().findViewById(checkId);
        String selectedGender = selectedRadioButton.getText().toString();

        Timestamp time = convertStringToTimestamp(binding.tvDOB.getText().toString());


        HashMap<String, Object> students = new HashMap<>();
        students.put(Constants.KEY_STUDENT_NAME, binding.inputName.getText().toString().trim());
        students.put(Constants.KEY_STUDENT_PHONE, binding.inputPhone.getText().toString().trim());
        students.put(Constants.KEY_STUDENT_EMAIL, binding.inputEmail.getText().toString().trim());
        students.put(Constants.KEY_STUDENT_ADDRESS, binding.inputAddress.getText().toString().trim());
        students.put(Constants.KEY_STUDENT_DOB, time);
        students.put(Constants.KEY_STUDENT_GENDER, selectedGender);

        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .document(student.id)
                .update(students)
                .addOnSuccessListener(documentReference -> {
                    Utils.ShowToast(getApplicationContext(), "Sửa thông tin học sinh thành công");
                    finish();
                })
                .addOnFailureListener(e ->  {
                    Utils.ShowToast(getApplicationContext(), e.getMessage());
                    loading(false);
                });
    }

    private void deleteStudent() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .document(student.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Utils.ShowToast(getApplicationContext(), "Xoá học sinh thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Utils.ShowToast(getApplicationContext(), e.getMessage());
                    loading(false);
                });
    }

    private String EncodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth /bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageStudent.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = EncodedImage(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btConfirm.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btConfirm.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidStudentDetails() {
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập họ và tên");
            return false;
        } else if (binding.inputPhone.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập số điện thoại");
            return false;
        } else if (binding.inputPhone.getText().length() != 10) {
            Utils.ShowToast(this, "Số điện thoại phải có 10 số");
            return false;
        }else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            Utils.ShowToast(this, "Email không hợp lệ");
            return false;
        } else if (binding.inputAddress.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this, "Hãy nhập địa chỉ");
            return false;
        } else if(!isImgChoosen) {
            Utils.ShowToast(this, "Hãy chọn ảnh");
            return false;
        } else {
            return true;
        }
    }
}