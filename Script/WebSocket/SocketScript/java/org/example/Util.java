package org.example;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {
    public static String getCurrentDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public static boolean isContainString(JSONArray jsonArray, String value){
        for (int i = 0; i < jsonArray.length(); i++){
            if(jsonArray.getString(i).equals(value)){
                return true;
            }
        }
        return false;
    }

    public static boolean removeElement(JSONArray jsonArray, String value){
        for (int i = 0; i < jsonArray.length(); i++){
            if(jsonArray.getString(i).equals(value)){
                jsonArray.remove(i);
                return true;
            }
        }
        return false;
    }
}
