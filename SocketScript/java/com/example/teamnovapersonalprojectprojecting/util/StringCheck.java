package com.example.teamnovapersonalprojectprojecting.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringCheck {
    public static boolean containsUpperCase(String password) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean containsLowerCase(String password) {
        Pattern pattern = Pattern.compile("[a-z]");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean containsDigit(String password) {
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean containsSpecialChar(String password) {
        Pattern pattern = Pattern.compile("[@#$%^&+=!]");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

}
