package com.septia.silam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.septia.silam.R;
import com.septia.silam.fragment.DashboardFragment;
import com.septia.silam.fragment.KelolaAntrianFragment;
import com.septia.silam.fragment.SettingsFragment;

public class DashboardPegawaiActivity extends AppCompatActivity {

    private static final String KEY_TAB = "current_tab_index";

    private BottomNavigationView bottomNav;
    private int currentTabIndex = 0; // 0: Dashboard, 1: Antrian, 2: Settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Cek sesi login dulu sebelum menampilkan apa pun
        if (!isSessionValid()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_dashboard_pegawai);

        bottomNav = findViewById(R.id.bottomNavigation);

        // 2. Pulihkan posisi tab kalau layar diputar / activity dibuat ulang
        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(KEY_TAB, 0);
        }

        // 3. Fragment default saat pertama masuk
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }

        // 4. Navigasi bawah
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int targetIndex = 0;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                    targetIndex = 0;
                } else if (itemId == R.id.nav_antrian) {
                    selectedFragment = new KelolaAntrianFragment();
                    targetIndex = 1;
                } else if (itemId == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                    targetIndex = 2;
                }

                // Ganti fragment hanya kalau tab yang dipilih berbeda dari tab aktif
                if (selectedFragment != null && targetIndex != currentTabIndex) {
                    int animIn, animOut;

                    if (targetIndex > currentTabIndex) {
                        animIn = R.anim.slide_in_right;
                        animOut = R.anim.slide_out_left;
                    } else {
                        animIn = R.anim.slide_in_left;
                        animOut = R.anim.slide_out_right;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(animIn, animOut)
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();

                    currentTabIndex = targetIndex;
                }
                return true;
            });
        }

        // 5. Tombol Back: dari tab lain kembali ke Dashboard dulu, baru keluar
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTabIndex != 0 && bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, currentTabIndex);
    }

    // ---------------------------------------------------------------
    // Helper: dipanggil dari Fragment
    // ---------------------------------------------------------------

    /** Pindah tab otomatis dari Fragment lain, contoh: navigateToTab(R.id.nav_antrian) */
    public void navigateToTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    /** Dipanggil dari Fragment kalau server membalas 401 (token kedaluwarsa) atau saat logout */
    public void forceLogout() {
        getSharedPreferences("SILAM_SESSION", MODE_PRIVATE).edit().clear().apply();
        goToLogin();
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private boolean isSessionValid() {
        SharedPreferences pref = getSharedPreferences("SILAM_SESSION", MODE_PRIVATE);
        return pref.getBoolean("is_logged_in", false)
                && !pref.getString("auth_token", "").isEmpty();
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginPegawaiActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}