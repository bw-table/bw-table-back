package com.zero.bwtableback.common.util;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;

import java.util.regex.Pattern;

public class PhoneAndBusinessNumberUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");
    private static final Pattern BUSINESS_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");

    /**
     * 전화번호에서 하이픈을 제거하고 유효성 검사
     */
    public static String cleanAndValidatePhoneNumber(String phone) {
        String cleanedPhone = removeHyphensAndTrim(phone);
        if (!PHONE_PATTERN.matcher(cleanedPhone).matches()) {
            throw new CustomException(ErrorCode.INVALID_PHONE_NUMBER);
        }
        return cleanedPhone;
    }

    /**
     * 사업자등록번호에서 하이픈을 제거하고 유효성을 검사
     */
    public static String cleanAndValidateBusinessNumber(String businessNumber) {
        String cleanedNumber = removeHyphensAndTrim(businessNumber);
        if (!BUSINESS_NUMBER_PATTERN.matcher(cleanedNumber).matches()) {
            throw new CustomException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }
        return cleanedNumber;
    }

    /**
     * 문자열에서 하이픈과 공백을 제거
     */
    public static String removeHyphensAndTrim(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[-\\s]", "");
    }
}