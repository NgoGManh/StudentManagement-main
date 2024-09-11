package com.huflit.studentmanagement.utilities;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.studentmanagement.activities.ViewImageActivity;
import com.huflit.studentmanagement.models.Image;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String SubjectId = "";
    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    public static String formatNumber(String numberStr) {
        try {
            long number = Long.parseLong(numberStr);


            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

            return formatter.format(number);
        } catch (NumberFormatException e) {
            return numberStr;
        }
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static String formatDateTime(String time) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(time));
    }

    public static void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static Timestamp convertStringToTimestamp(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getReadableDataTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    public static void onImageClick(String image, Context context) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("imageMessage",image);
        context.startActivity(intent);
    }

    public static Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage == null || encodedImage.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSubjectId(String subjectName) {
        database.collection(Constants.KEY_COLLECTION_SUBJECTS)
                .whereEqualTo(Constants.KEY_SUBJECT_NAME, subjectName)
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                SubjectId = document.getId();
                            }
                        }
                );
        return SubjectId;
    }

}
