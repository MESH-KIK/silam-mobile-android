package com.septia.silam.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "SILAM_SESSION";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAMA_USER = "namaUser";
    private static final String KEY_ROLE = "role";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Simpan Data Session Saat Login
    public void createLoginSession(int userId, String namaUser, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_NAMA_USER, namaUser);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    // Cek Apakah Pegawai Sudah Login
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Ambil Nama Pegawai
    public String getNamaUser() {
        return pref.getString(KEY_NAMA_USER, "Pegawai");
    }

    // Clear Session Saat Logout
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}