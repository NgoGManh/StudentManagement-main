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
import com.huflit.studentmanagement.databinding.ActivityAddTeacherBinding;
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

public class AddTeacherActivity extends AppCompatActivity {
    private ActivityAddTeacherBinding binding;
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
        binding = ActivityAddTeacherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    private void listeners() {
        getIndex();
        SpinnerUtils.setSpinnerData(AddTeacherActivity.this, SpinnerUtils.getSubjectList(), binding.inputSubject);
        binding.inputSubject.setSelection(0);

        binding.btConfirm.setOnClickListener(v -> {
            if (isValidTeacherDetails()) {
                if (isDateSelected) {
                    addTeacher();
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
        SpinnerUtils.onChooseSpinner(binding.inputSubject);

        binding.imgUpload.setOnClickListener(v -> openFileChooser());
    }

    private void getIndex() {
        database.collection(Constants.KEY_COLLECTION_TEACHERS)
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddTeacherActivity.this,
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
                    if (age < 22 || age > 65) {
                        Utils.ShowToast(this, "Tuổi phải lớn hơn 22!");
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
                    List<Teacher> teachers = ExcelUtils.readTeachersFromExcel(inputStream);
                    saveTeachersToDatabase(teachers);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveTeachersToDatabase(List<Teacher> teachers) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (Teacher teacher : teachers) {
            if (teacher.getId() != null && !teacher.getId().isEmpty()) {
                if (teacher.getDob() != null && !teacher.getDob().isEmpty()) {
                    teacher.setDobTimestamp(Utils.convertStringToTimestamp(teacher.getDob()));
                }

                HashMap<String, Object> teacherData = new HashMap<>();
                teacherData.put(Constants.KEY_TEACHER_ID, teacher.getId());
                teacherData.put(Constants.KEY_TEACHER_NAME, teacher.getName());
                teacherData.put(Constants.KEY_TEACHER_PHONE, teacher.getPhone());
                teacherData.put(Constants.KEY_TEACHER_EMAIL, teacher.getEmail());
                teacherData.put(Constants.KEY_TEACHER_SUBJECT, teacher.getSubject());
                teacherData.put(Constants.KEY_TEACHER_GENDER, teacher.getGender());
                teacherData.put(Constants.KEY_TEACHER_DOB, teacher.getDobTimestamp());
                teacherData.put(Constants.KEY_TEACHER_IMAGE, teacher.getImage());

                db.collection(Constants.KEY_COLLECTION_TEACHERS)
                        .document(teacher.getId())
                        .set(teacherData)
                        .addOnSuccessListener(aVoid -> {
                            createAccount(teacherData);
                            Utils.ShowToast(this, "Thêm giáo viên thành công");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(Constants.KEY_OPEN_TEACHER_FRAGMENT, true);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }
    }

    private void addTeacher() {
        loading(true);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String formattedId = String.format("%04d", id + 1);
        String teacherId = "GV" + year + formattedId;

        int checkId = binding.rgGender.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = binding.getRoot().findViewById(checkId);
        String selectedGender = selectedRadioButton.getText().toString();

        Timestamp time = convertStringToTimestamp(binding.tvDOB.getText().toString());

        HashMap<String, Object> teacher = new HashMap<>();
        teacher.put(Constants.KEY_TEACHER_ID, teacherId);
        teacher.put(Constants.KEY_TEACHER_NAME, binding.inputName.getText().toString().trim());
        teacher.put(Constants.KEY_TEACHER_PHONE, binding.inputPhone.getText().toString().trim());
        teacher.put(Constants.KEY_TEACHER_EMAIL, binding.inputEmail.getText().toString().trim());
        teacher.put(Constants.KEY_TEACHER_SUBJECT, binding.inputSubject.getSelectedItem().toString());
        teacher.put(Constants.KEY_TEACHER_GENDER, selectedGender);
        teacher.put(Constants.KEY_TEACHER_DOB, time);
        teacher.put(Constants.KEY_TEACHER_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_TEACHERS)
                .document(teacherId)
                .set(teacher)
                .addOnSuccessListener(documentReference -> {
                   createAccount(teacher);
                    Utils.ShowToast(this, "Thêm giáo viên thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    Utils.ShowToast(this, "Lỗi: " + e.getMessage());
                });
    }

    private void createAccount(HashMap<String, Object> teacher) {
        HashMap<String, Object> teacherAccount = new HashMap<>();
        teacherAccount.put(Constants.KEY_USER_EMAIL, teacher.get(Constants.KEY_TEACHER_EMAIL));
        teacherAccount.put(Constants.KEY_USER_NAME, teacher.get(Constants.KEY_TEACHER_NAME));
        teacherAccount.put(Constants.KEY_USER_PASSWORD, "GV123");
        teacherAccount.put(Constants.KEY_USER_ROLE, "Giáo viên");
        teacherAccount.put(Constants.KEY_USER_IMAGE, teacher.get(Constants.KEY_TEACHER_IMAGE));
        teacherAccount.put(Constants.KEY_USER_INFO_ID, teacher.get(Constants.KEY_TEACHER_ID));
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(teacherAccount)
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
        } else if (binding.inputSubject.getSelectedItem().toString().equals("Chọn môn")) {
            Utils.ShowToast(this, "Hãy chọn môn dạy");
            return false;
        } else {
            return true;
        }
    }
}