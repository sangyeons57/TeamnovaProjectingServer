package org.example;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {
    public static final int SYSTEM_ID = 1;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String getCurrentDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return now.format(formatter);
    }
    public static String changeDatetimeToFormat(String datetime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return LocalDateTime.parse(datetime).format(formatter);
    }

    public static boolean isInArray(JSONArray jsonArray, int value){
        for (int i = 0; i < jsonArray.length(); i++){
            if(jsonArray.getInt(i) == value){
                return true;
            }
        }
        return false;
    }

    public static boolean removeElement(JSONArray jsonArray, int value){
        for (int i = 0; i < jsonArray.length(); i++){
            if(jsonArray.getInt(i) == value){
                jsonArray.remove(i);
                return true;
            }
        }
        return false;
    }
}
