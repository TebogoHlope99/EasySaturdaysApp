package com.example.easysaturdaysapp;

public class AdminRequest {
    // --- FIELDS TO SEND TO C# ---
    public int requestedByUserId;
    public String actionType;
    public String targetAdminId;
    public String newAdminEmail;

    // --- FIELDS FOR SUPER ADMIN UI DISPLAY (C# will ignore these) ---
    public int id;
    public String status;
    public String requestedAt;
}