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

public class AdminFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new AdminAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Campaigns"); break;
                case 1: tab.setText("Users"); break;
                case 2: tab.setText("Donations"); break;
                case 3: tab.setText("Reports"); break;
                case 4: tab.setText("Admin Requests"); break;
            }
        }).attach();

        return view;
    }
}