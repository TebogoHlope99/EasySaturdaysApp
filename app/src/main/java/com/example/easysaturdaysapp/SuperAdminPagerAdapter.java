package com.example.easysaturdaysapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SuperAdminPagerAdapter extends FragmentStateAdapter {
    public SuperAdminPagerAdapter(Fragment fragment) { super(fragment); }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new SuperAdminRequestsFragment();
        else return new SuperAdminListFragment();
    }

    @Override
    public int getItemCount() { return 2; }
}