package com.example.easysaturdaysapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("EasySaturdays");

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        applyRolePermissions(navigationView);

        // --- FIX: ROBUST DEFAULT FRAGMENT LOGIC ---
        if (savedInstanceState == null) {
            if (User.currentUser != null) {
                String role = User.currentUser.role != null ? User.currentUser.role : "";

                // If Super Admin, load Super Admin Panel
                if (role.equalsIgnoreCase("SuperAdmin")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SuperAdminFragment()).commit();
                }
                // If normal Admin, load Admin Panel
                else if (role.equalsIgnoreCase("Admin")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminFragment()).commit();
                }
                // If Donor, Volunteer, or Both, load Dashboard
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                }
            } else {
                // If user is null, go back to Login
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void applyRolePermissions(NavigationView navigationView) {
        if (User.currentUser == null) return;
        String role = User.currentUser.role != null ? User.currentUser.role : "";

        MenuItem donateItem = navigationView.getMenu().findItem(R.id.nav_donate);
        MenuItem volunteerItem = navigationView.getMenu().findItem(R.id.nav_volunteer);
        MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_admin);
        MenuItem superAdminItem = navigationView.getMenu().findItem(R.id.nav_super_admin);

        if (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("SuperAdmin")) {
            if(adminItem != null) adminItem.setVisible(false);
            if(superAdminItem != null) superAdminItem.setVisible(false);
        }

        if (role.equalsIgnoreCase("Volunteer")) {
            if(donateItem != null) donateItem.setVisible(false);
        } else if (role.equalsIgnoreCase("Donor")) {
            if(volunteerItem != null) volunteerItem.setVisible(false);
        } else if (role.equalsIgnoreCase("Admin")) {
            if(superAdminItem != null) superAdminItem.setVisible(false);
        } else if (role.equalsIgnoreCase("SuperAdmin")) {
            if(adminItem != null) adminItem.setVisible(false);
            if(donateItem != null) donateItem.setVisible(false);
            if(volunteerItem != null) volunteerItem.setVisible(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (id == R.id.nav_campaigns) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CampaignsFragment()).commit();
        } else if (id == R.id.nav_dashboard) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        } else if (id == R.id.nav_donate) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DonateFragment()).commit();
        } else if (id == R.id.nav_volunteer) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolunteerFragment()).commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else if (id == R.id.nav_admin) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminFragment()).commit();
        } else if (id == R.id.nav_super_admin) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SuperAdminFragment()).commit();
        } else if (id == R.id.nav_logout) {
            User.currentUser = null;
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}