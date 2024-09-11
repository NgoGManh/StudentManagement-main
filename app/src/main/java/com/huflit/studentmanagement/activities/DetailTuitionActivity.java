package com.huflit.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.R;
import com.huflit.studentmanagement.api.CreateOrder;
import com.huflit.studentmanagement.databinding.ActivityDetailAnnouncementBinding;
import com.huflit.studentmanagement.databinding.ActivityDetailTuitionBinding;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.models.Tuition;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;

import org.json.JSONObject;

import java.util.Objects;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class DetailTuitionActivity extends AppCompatActivity {
    private ActivityDetailTuitionBinding binding;
    private Tuition tuition;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private String totalString = "0";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_orange));
        binding = ActivityDetailTuitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        listeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setUpZaloPay() {
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ZaloPaySDK.init(2553, Environment.SANDBOX);
    }

    private void listeners(){
        setUpZaloPay();
        tuition = preferenceManager.getObject(Constants.KEY_TUITION, Tuition.class);
        Log.d("tuition", tuition.toString());
        if (tuition != null) {
            getData();
        }

        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
        binding.btConfirm.setOnClickListener(v -> {
            totalString = tuition.amount;
            handlePayment();
        });
    }

    private void getData() {
        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                        .document(tuition.studentId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                binding.tvName.setText(documentSnapshot.getString(Constants.KEY_STUDENT_NAME));
                            }
                        });
        setData();
    }

    private void setData() {
        binding.tvAmount.setText(Utils.formatNumber(tuition.amount + " VNĐ"));
        if (!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")) {
            binding.btConfirm.setVisibility(View.VISIBLE);
            if (tuition.status.equals("Đã thanh toán")) {
                binding.date.setVisibility(View.VISIBLE);
                binding.tvDate.setVisibility(View.VISIBLE);
                if (tuition.date != null) {
                    binding.tvDate.setText(tuition.date);
                }
                binding.tvPaid.setVisibility(View.VISIBLE);
                binding.tvNotPaid.setVisibility(View.GONE);
                binding.btConfirm.setVisibility(View.GONE);
            } else {
                binding.date.setVisibility(View.GONE);
                binding.tvDate.setVisibility(View.GONE);
                binding.tvPaid.setVisibility(View.GONE);
                binding.tvNotPaid.setVisibility(View.VISIBLE);
            }
        } else {
            binding.btConfirm.setVisibility(View.GONE);
            if (tuition.status.equals("Đã thanh toán")) {
                binding.date.setVisibility(View.VISIBLE);
                binding.tvDate.setVisibility(View.VISIBLE);
                if (tuition.date != null) {
                    binding.tvDate.setText(tuition.date);
                }
                binding.tvPaid.setVisibility(View.VISIBLE);
                binding.tvNotPaid.setVisibility(View.GONE);
            } else {
                binding.date.setVisibility(View.GONE);
                binding.tvDate.setVisibility(View.GONE);
                binding.tvPaid.setVisibility(View.GONE);
                binding.tvNotPaid.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onPaymentSuccess() {
        database.collection(Constants.KEY_COLLECTION_TUITIONS).document(tuition.id).update(Constants.KEY_TUITION_STATUS, "Đã thanh toán", Constants.KEY_TUITION_DATE, Utils.getCurrentDate());
        database.collection(Constants.KEY_COLLECTION_STUDENTS).document(tuition.studentId).update(Constants.KEY_STUDENT_IS_PAID, true);
        loading(false);
        Utils.ShowToast(getApplicationContext(), "Thanh toán học phí thành công!");
        Intent intent = new Intent(DetailTuitionActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void handlePayment() {
        loading(true);
        CreateOrder orderApi = new CreateOrder();
        try {
            JSONObject data = orderApi.createOrder(totalString);
            String code = data.getString("return_code");
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(DetailTuitionActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        onPaymentSuccess();
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        loading(false);
                        Utils.ShowToast(getApplicationContext(), "Thanh toán đã bị huỷ!");
                        Intent intent = new Intent(DetailTuitionActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        loading(false);
                        Utils.ShowToast(getApplicationContext(), "Đã có lỗi xảy ra khi đang thực hiện thanh toán!");
                        Intent intent = new Intent(DetailTuitionActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null) {
            ZaloPaySDK.getInstance().onResult(intent);
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
}