package com.example.easysaturdaysapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SuperAdminFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_super_admin, container, false);

        TabLayout tabLayout = view.findViewById(R.id.superAdminTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.superAdminViewPager);

        viewPager.setAdapter(new SuperAdminPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Pending Requests");
            else tab.setText("Current Admins");
        }).attach();

        return view;
    }
}