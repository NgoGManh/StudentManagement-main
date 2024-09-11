package com.huflit.studentmanagement.activities;

import static com.huflit.studentmanagement.utilities.Utils.convertStringToTimestamp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityAddStudentBinding;
import com.huflit.studentmanagement.databinding.ActivityAddTeacherBinding;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.ExcelUtils;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.SpinnerUtils;
import com.huflit.studentmanagement.utilities.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {
    private ActivityAddStudentBinding binding;
    private boolean isDateSelected = false;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int id = 0;
    private  String encodedImage;
    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityAddStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getIndex();

        binding.btConfirm.setOnClickListener(v -> {
            if (isValidTeacherDetails()) {
                if (isDateSelected) {
                    addStudent();
                } else {
                    Utils.ShowToast(this, "Vui lòng chọn ngày sinh!");
                }
            }
        });
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
        binding.btPickDOB.setOnClickListener(v -> showDatePickerDialog(binding.tvDOB, true));
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImage.launch(intent);
        });
        binding.imgUpload.setOnClickListener(v -> openFileChooser());
    }

    private void getIndex() {
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String teacherId = documentSnapshot.getId();
                        if (teacherId.length() >= 2) {
                            String indexStr = teacherId.substring(teacherId.length() - 2);
                            try {
                                int i = Integer.parseInt(indexStr);
                                if (i > id) {
                                    id = i;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void showDatePickerDialog(TextView textView, boolean isDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddStudentActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    textView.setText(selectedDate);

                    if (isDate) {
                        isDateSelected = true;
                    }

                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                    if (selectedCalendar.after(c)) {
                        Utils.ShowToast(this, "Ngày sinh không được lớn hơn ngày hiện tại!");
                        textView.setText("");
                        isDateSelected = false;
                        return;
                    }

                    int age = year - selectedYear;
                    if (age < 15 || age > 20) {
                        Utils.ShowToast(this, "Tuổi phải nằm trong khoảng từ 15 đến 20!");
                        textView.setText("");
                        isDateSelected = false;
                    }
                }, year, month, day);
        datePickerDialog.show();
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
                            binding.imageTeacher.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = EncodedImage(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode ==  RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    List<Student> students = ExcelUtils.readStudentsFromExcel(inputStream);
                    saveStudentsToDatabase(students);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveStudentsToDatabase(List<Student> students) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (Student student : students) {
            if (student.getId() != null && !student.getId().isEmpty()) {
                if (student.getDob() != null && !student.getDob().isEmpty()) {
                    student.setDobTimestamp(Utils.convertStringToTimestamp(student.getDob()));
                }

                HashMap<String, Object> studentData = new HashMap<>();
                studentData.put(Constants.KEY_STUDENT_ID, student.getId());
                studentData.put(Constants.KEY_STUDENT_NAME, student.getName());
                studentData.put(Constants.KEY_STUDENT_PHONE, student.getPhone());
                studentData.put(Constants.KEY_STUDENT_EMAIL, student.getEmail());
                studentData.put(Constants.KEY_STUDENT_ADDRESS, student.getAddress());
                studentData.put(Constants.KEY_STUDENT_GENDER, student.getGender());
                studentData.put(Constants.KEY_STUDENT_DOB, student.getDobTimestamp());
                studentData.put(Constants.KEY_STUDENT_IMAGE, student.getImage());

                db.collection(Constants.KEY_COLLECTION_STUDENTS)
                        .document(student.getId())
                        .set(studentData)
                        .addOnSuccessListener(aVoid -> {
                            createAccount(studentData);
                            Utils.ShowToast(this, "Thêm học sinh thành công");
                            finish();
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }
    }

    private void addStudent() {
        loading(true);

        String formattedId = String.format("%08d", id + 1);
        String studentId = "HS" + formattedId;

        int checkId = binding.rgGender.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = binding.getRoot().findViewById(checkId);
        String selectedGender = selectedRadioButton.getText().toString();

        Timestamp time = convertStringToTimestamp(binding.tvDOB.getText().toString());

        HashMap<String, Object> student = new HashMap<>();
        student.put(Constants.KEY_STUDENT_ID, studentId);
        student.put(Constants.KEY_STUDENT_NAME, binding.inputName.getText().toString().trim());
        student.put(Constants.KEY_STUDENT_PHONE, binding.inputPhone.getText().toString().trim());
        student.put(Constants.KEY_STUDENT_EMAIL, binding.inputEmail.getText().toString().trim());
        student.put(Constants.KEY_STUDENT_ADDRESS, binding.inputAddress.getText().toString().trim());
        student.put(Constants.KEY_STUDENT_GENDER, selectedGender);
        student.put(Constants.KEY_STUDENT_DOB, time);
        student.put(Constants.KEY_STUDENT_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                .document(studentId)
                .set(student)
                .addOnSuccessListener(documentReference -> {
                    createAccount(student);
                    Utils.ShowToast(this, "Thêm học sinh thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    Utils.ShowToast(this, "Lỗi: " + e.getMessage());
                });
    }

    private void createAccount(HashMap<String, Object> student) {
        HashMap<String, Object> studentAccount = new HashMap<>();
        studentAccount.put(Constants.KEY_USER_EMAIL, student.get(Constants.KEY_STUDENT_EMAIL));
        studentAccount.put(Constants.KEY_USER_NAME, student.get(Constants.KEY_STUDENT_NAME));
        studentAccount.put(Constants.KEY_USER_PASSWORD, "HS123");
        studentAccount.put(Constants.KEY_USER_ROLE, "Học sinh");
        studentAccount.put(Constants.KEY_USER_IMAGE, student.get(Constants.KEY_STUDENT_IMAGE));
        studentAccount.put(Constants.KEY_USER_INFO_ID, student.get(Constants.KEY_STUDENT_ID));
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(studentAccount)
                .addOnSuccessListener(documentReference1 -> {
                });
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btConfirm.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btConfirm.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidTeacherDetails() {
        if (encodedImage == null) {
            Utils.ShowToast(this, "Hãy chọn ảnh");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
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
        } else {
            return true;
        }
    }
}