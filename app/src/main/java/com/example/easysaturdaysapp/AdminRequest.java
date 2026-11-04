package com.example.easysaturdaysapp;

public class AdminRequest {
    public int id;
    public int requestedByUserId;
    public String actionType; // CreateAdmin, DeleteAdmin
    public String targetAdminId;
    public String newAdminEmail;
    public String status;
    public String requestedAt;

    // To display the name of the admin who made the request
    public User requestedByUser;
}