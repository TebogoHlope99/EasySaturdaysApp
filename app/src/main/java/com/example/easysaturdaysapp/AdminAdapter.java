package com.example.easysaturdaysapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminAdapter extends FragmentStateAdapter {
    public AdminAdapter(Fragment fragment) { super(fragment); }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AdminCampaignsFragment();
            case 1: return new AdminUsersFragment();
            case 2: return new AdminDonationsFragment();
            case 3: return new AdminReportsFragment();
            default: return new AdminCampaignsFragment();
        }
    }

    @Override
    public int getItemCount() { return 4; }
}