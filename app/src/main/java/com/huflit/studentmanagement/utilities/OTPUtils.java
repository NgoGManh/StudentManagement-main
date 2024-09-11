package com.huflit.studentmanagement.utilities;

import com.huflit.studentmanagement.models.OTP;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class OTPUtils {

    private static final int OTP_LENGTH = 6;
    private static final String OTP_CHARACTERS = "0123456789";
    private static final int OTP_VALIDITY_DURATION = 2;

    public static OTP generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARACTERS.charAt(random.nextInt(OTP_CHARACTERS.length())));
        }
        return new OTP(otp.toString());
    }

    public static boolean isOtpValid(OTP otp) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(otp.getTimestamp(), now);
        return duration.toMinutes() <= OTP_VALIDITY_DURATION;
    }
}

