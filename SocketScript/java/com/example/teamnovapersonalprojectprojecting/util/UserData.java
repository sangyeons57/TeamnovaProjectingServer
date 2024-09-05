package com.example.teamnovapersonalprojectprojecting.util;

public class UserData {
    public UserData(int userId ) {
        this(userId, "USER", null);
    }
    public UserData(int userId, String username, String profileImagePath) {
        this.userId = userId;
        this.username = username;
        this.profileImagePath = profileImagePath;
    }
    public int userId;
    public String username;
    public String profileImagePath;
}
