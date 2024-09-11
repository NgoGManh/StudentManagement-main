package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.databinding.ActivityForgotPasswordBinding;
import com.huflit.studentmanagement.models.OTP;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.OTPUtils;
import com.huflit.studentmanagement.utilities.SMTP;
import com.huflit.studentmanagement.utilities.Utils;

import java.util.HashMap;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {

    private OTP currentOtp;
    private ActivityForgotPasswordBinding binding;
    private int seeNewPass = 0, seeConfirmPass = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v ->
                finish());
        binding.btConfirm.setOnClickListener(v -> {
            loading(true);
            String email = binding.inputEmail.getText().toString().trim();
            isEmailExists(email, exists -> {
                if (exists) {
                    if (isValidInputDetails()) {
                        forgotPassword();
                    }
                    else {
                        loading(false);
                    }
                } else {
                    loading(false);
                    Utils.ShowToast(this, "Email không tồn tại!");
                }
            });
        });
        binding.seePassword.setOnClickListener(v -> {
            if (seeNewPass == 0) {
                binding.inputPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seeNewPass = 1;
            } else {
                binding.inputPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seeNewPass = 0;
            }
        });

        binding.seeConfirmPass.setOnClickListener(v -> {
            if (seeConfirmPass == 0) {
                binding.inputConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seeConfirmPass = 1;
            } else {
                binding.inputConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seeConfirmPass = 0;
            }
        });
    }

    private void forgotPassword() {
        String email = binding.inputEmail.getText().toString();
        String newPassword = binding.inputPass.getText().toString();

        currentOtp = OTPUtils.generateOTP();
        sendOTPMail(email, currentOtp.getOtpCode());
        showOtpDialog(email, newPassword, currentOtp.getOtpCode());
    }


    private void showOtpDialog(String email, String newPassword, String otpCode) {
        loading(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực email");
        builder.setMessage("Mã xác thực đã được gửi đến \n\n" + email +" :");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final TextView timerTextView = new TextView(this);
        timerTextView.setPadding(0, 0, 0, 10);
        layout.addView(timerTextView);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            String otpEntered = input.getText().toString().trim();
            if (OTPUtils.isOtpValid(currentOtp) && currentOtp.getOtpCode().equals(otpEntered)) {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                DocumentReference documentReference = documentSnapshot.getReference();
                                HashMap<String, Object> updates = new HashMap<>();
                                updates.put(Constants.KEY_USER_PASSWORD, newPassword);
                                documentReference.update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Utils.ShowToast(this, "Mật khẩu đã được thay đổi thành công!");
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Utils.ShowToast(this, "Lỗi khi cập nhật mật khẩu!");
                                        });
                            }
                        });
            } else {
                loading(false);
                Utils.ShowToast(this, "Sai OTP, hoặc OTP đã hết hiệu lực.");
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) ->  {
                    loading(false);
                    dialog.cancel();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        startCountDown(timerTextView, dialog);
    }

    private void startCountDown(TextView timerTextView, AlertDialog dialog) {
        new CountDownTimer(2 * 60 * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String time = String.format("%02d:%02d", minutes, seconds);
                timerTextView.setText("Thời gian còn lại: " + time);
            }

            public void onFinish() {
                timerTextView.setText("OTP đã hết hiệu lực.");
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }.start();
    }

    private void sendOTPMail(String recipientEmail, String otpCode) {

        try {
            MimeMessage message = new MimeMessage(SMTP.sendMail());
            message.setFrom(new InternetAddress("topchannel102@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã xác thực cho yêu cầu khôi phục mật khẩu.");
            message.setText("Xin chào,"
                    + "\n\n" + otpCode + " là mã xác thực cho yêu cầu khôi phục mật khẩu của bạn."
                    + "\n\nVui lòng không chia sẽ mã cho ngưười khác!");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
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

    private Boolean isValidInputDetails() {
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this,"Hãy nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            Utils.ShowToast(this,"Email không hợp lệ");
            return false;
        } else if (binding.inputPass.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this,"Hãy nhập mật khẩu mới");
            return false;
        }
        else if (binding.inputConfirmPass.getText().toString().trim().isEmpty()) {
            Utils.ShowToast(this,"Hãy xác nhận mật khẩu mới");
            return false;
        }else if (!binding.inputPass.getText().toString().equals(binding.inputConfirmPass.getText().toString())) {
            Utils.ShowToast(this,"Mật khẩu mới không khớp");
            return false;
        }else {
            return true;
        }
    }

    private void isEmailExists(String email, EmailExistsCallback callback) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        callback.onCallback(true);
                    } else {
                        callback.onCallback(false);
                    }
                });
    }
    public interface EmailExistsCallback {
        void onCallback(boolean exists);
    }
}