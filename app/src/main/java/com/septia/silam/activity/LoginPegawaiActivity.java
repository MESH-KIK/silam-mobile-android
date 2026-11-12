package com.septia.silam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.septia.silam.R;
import com.septia.silam.config.ApiClient;
import com.septia.silam.config.ServerConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPegawaiActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;
    private static final String PIN_BENAR = "123456";            // sementara, lihat catatan keamanan
    private static final String EMAIL_PEGAWAI = "pegawai@silam.test";
    private static final String PASSWORD_PEGAWAI = "pegawai123";

    private final StringBuilder inputPin = new StringBuilder();
    private final View[] dotPins = new View[PIN_LENGTH];
    private GestureDetector gestureDetector;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_pegawai_activity);

        dotPins[0] = findViewById(R.id.dotPin1);
        dotPins[1] = findViewById(R.id.dotPin2);
        dotPins[2] = findViewById(R.id.dotPin3);
        dotPins[3] = findViewById(R.id.dotPin4);
        dotPins[4] = findViewById(R.id.dotPin5);
        dotPins[5] = findViewById(R.id.dotPin6);

        setupKeypadListeners();

        View btnDelete = findViewById(R.id.btnKeyDelete);
        if (btnDelete != null) btnDelete.setOnClickListener(v -> deletePinDigit());

        LinearLayout btnModePelanggan = findViewById(R.id.btnModePelanggan);
        if (btnModePelanggan != null) btnModePelanggan.setOnClickListener(v -> navigateToLandingPage());

        setupSwipeDownGesture();
    }

    private void setupKeypadListeners() {
        int[] keyIds = {
                R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
                R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };
        for (int i = 0; i < keyIds.length; i++) {
            final String digit = String.valueOf(i);
            View keyView = findViewById(keyIds[i]);
            if (keyView != null) keyView.setOnClickListener(v -> appendPinDigit(digit));
        }
    }

    private void appendPinDigit(String digit) {
        if (isLoading) return;
        if (inputPin.length() < PIN_LENGTH) {
            inputPin.append(digit);
            updatePinDots();
            if (inputPin.length() == PIN_LENGTH) {
                verifyPinWithApi(inputPin.toString());
            }
        }
    }

    private void deletePinDigit() {
        if (isLoading) return;
        if (inputPin.length() > 0) {
            inputPin.deleteCharAt(inputPin.length() - 1);
            updatePinDots();
        }
    }

    private void updatePinDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            if (dotPins[i] != null) {
                dotPins[i].setBackgroundColor(Color.parseColor(
                        i < inputPin.length() ? "#2E4A85" : "#D3E0FA"));
            }
        }
    }

    private void verifyPinWithApi(String pin) {
        if (!pin.equals(PIN_BENAR)) {
            Toast.makeText(this, "PIN Petugas Salah!", Toast.LENGTH_SHORT).show();
            resetPin();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("email", EMAIL_PEGAWAI);
            body.put("password", PASSWORD_PEGAWAI);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        isLoading = true;

        ApiClient.get(this).request(
                Request.Method.POST,
                ServerConfig.API_LOGIN,
                body,
                response -> {
                    isLoading = false;

                    // kalau server mengirim status=false, anggap gagal
                    if (response.has("status") && !response.optBoolean("status", true)) {
                        Toast.makeText(this, response.optString("message", "Login gagal"), Toast.LENGTH_LONG).show();
                        resetPin();
                        return;
                    }

                    String token = response.optString("token", "");
                    if (token.isEmpty()) {
                        Toast.makeText(this, "Token tidak ditemukan di respons server", Toast.LENGTH_LONG).show();
                        resetPin();
                        return;
                    }

                    JSONObject userObj = response.optJSONObject("user");
                    String namaUser = "Pegawai";
                    String role = "";
                    int idUser = 0;

                    if (userObj != null) {
                        idUser   = userObj.optInt("id_user", userObj.optInt("id", 0));
                        namaUser = userObj.optString("nama_user", userObj.optString("name", "Pegawai"));
                        role     = userObj.optString("role", "");
                    }

                    SharedPreferences pref = getSharedPreferences("SILAM_SESSION", MODE_PRIVATE);
                    pref.edit()
                            .putString("auth_token", token)
                            .putInt("id_user", idUser)
                            .putString("nama_pegawai", namaUser)
                            .putString("role", role)
                            .putBoolean("is_logged_in", true)
                            .apply();

                    Toast.makeText(this, "Selamat datang, " + namaUser, Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, DashboardPegawaiActivity.class));
                    finish();
                },
                error -> {
                    isLoading = false;
                    Toast.makeText(this, ApiClient.errorMessage(error), Toast.LENGTH_LONG).show();
                    resetPin();
                }
        );
    }

    private void resetPin() {
        inputPin.setLength(0);
        updatePinDots();
    }

    private void setupSwipeDownGesture() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffY) > Math.abs(diffX)
                            && diffY > SWIPE_THRESHOLD
                            && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        navigateToLandingPage();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void navigateToLandingPage() {
        finish();
        try {
            overridePendingTransition(R.anim.fade_in_static, R.anim.slide_down_out);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}