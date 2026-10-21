package com.example.easysaturdaysapp;
import java.util.List;

public class DashboardData {
    public double totalDonated;
    public int campaignsSupported;
    public int hoursContributed;
    public int shiftsCompleted;
    public List<UpcomingShift> upcomingShifts;

    public static class UpcomingShift {
        public int id;
        public String campaignTitle;
        public String date;
        public String time;
        public String status;
    }
}