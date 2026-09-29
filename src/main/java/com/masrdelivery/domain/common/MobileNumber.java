package com.masrdelivery.domain.common;

import com.masrdelivery.exception.InvalidMobileNumberException;

import java.util.regex.Pattern;

public record MobileNumber(String number) {
    private static final Pattern PATTERN =
            Pattern.compile("^01[0125]\\d{8}$");

    public MobileNumber {
        if(number != null) {
            number = number.trim();
        }

        if (number == null ||
                !PATTERN.matcher(number).matches()) {

            throw new InvalidMobileNumberException(
                    number);
        }
    }
}
