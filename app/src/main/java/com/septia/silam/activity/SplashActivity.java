package com.septia.silam.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.septia.silam.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2800; // Durasi total splash screen (2.8 detik)
    private ProgressBar progressBar;
    private TextView tvStatusLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogoSilam = findViewById(R.id.imgLogoSilam);
        View viewPulse = findViewById(R.id.viewPulse);
        LinearLayout containerText = findViewById(R.id.containerText);
        progressBar = findViewById(R.id.progressBarSplash);
        tvStatusLoading = findViewById(R.id.tvStatusLoading);

        // Load & Jalankan Animasi Visual
        Animation animLogo = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        Animation animPulse = AnimationUtils.loadAnimation(this, R.anim.splash_pulse_anim);
        Animation animText = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim);

        imgLogoSilam.startAnimation(animLogo);
        viewPulse.startAnimation(animPulse);

        // Jeda Teks Tagline
        new Handler().postDelayed(() -> {
            containerText.setVisibility(View.VISIBLE);
            containerText.startAnimation(animText);
        }, 300);

        // Start Animasi Interaktif Progress & Status Loading
        startInteractiveLoading();

        // Pindah ke LandingPageActivity setelah selesai
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LandingPageActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    private void startInteractiveLoading() {
        // Animasi Smooth Progress Bar dari 0% ke 100%
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(2200);
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);

            // Pergantian Teks Status Dinamis berdasarkan persentase
            if (progress < 40) {
                tvStatusLoading.setText("Menyiapkan ekosistem SILAM...");
            } else if (progress < 75) {
                tvStatusLoading.setText("Memuat konfigurasi modul...");
            } else {
                tvStatusLoading.setText("Sistem siap digunakan!");
            }
        });
        animator.start();
    }
}