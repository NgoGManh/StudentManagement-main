package com.huflit.studentmanagement.models;

import java.time.LocalDateTime;

public class OTP {
    private String otpCode;
    private LocalDateTime timestamp;

    public OTP(String otpCode) {
        this.otpCode = otpCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getOtpCode() {
        return otpCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
