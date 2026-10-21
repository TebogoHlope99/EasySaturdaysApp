package com.example.easysaturdaysapp;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String firstName;
    public String lastName;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    public String role; // Donor, Volunteer, Both, Admin
    public String phone;
    public String skills;

    // GLOBAL SESSION
    public static User currentUser = null;
}