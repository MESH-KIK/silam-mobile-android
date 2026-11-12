package com.septia.silam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.septia.silam.R;
import com.septia.silam.adapter.LandingPageAdapter;
import com.septia.silam.model.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class LandingPageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private ImageView imgLogoSilam;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // Inisialisasi View dengan pengecekan aman
        viewPager = findViewById(R.id.viewPagerOnboarding);
        layoutDots = findViewById(R.id.layoutDotsIndicator);
        imgLogoSilam = findViewById(R.id.imgLogoSilam);

        // 1. Data Slide Publik
        List<SlideModel> slides = new ArrayList<>();
        slides.add(new SlideModel(
                "FITUR MANDIRI #1",
                "Cek Status Laundry Mandiri Real-Time",
                "Lacak setiap tahapan pengerjaan pakaian mulai dari Diterima, Dicuci, Dikeringkan, hingga Siap Diambil tanpa repot menelepon kasir.",
                android.R.drawable.ic_popup_sync,
                true
        ));
        slides.add(new SlideModel(
                "FITUR MANDIRI #2",
                "Notifikasi Otomatis Pakaian Selesai",
                "Dapatkan pemberitahuan langsung saat laundry Anda selesai dikeringkan dan siap untuk diambil atau diantar.",
                android.R.drawable.ic_dialog_info,
                false
        ));
        slides.add(new SlideModel(
                "FITUR MANDIRI #3",
                "Transparansi Riwayat & Rincian Biaya",
                "Lihat detail rincian berat timbangan, jenis layanan, dan total pembayaran secara rinci dan terpercaya.",
                android.R.drawable.ic_menu_agenda,
                false
        ));

        // 2. Set Adapter ke ViewPager2
        if (viewPager != null) {
            LandingPageAdapter adapter = new LandingPageAdapter(slides, () ->
                    Toast.makeText(LandingPageActivity.this, "Membuka pencarian status nota...", Toast.LENGTH_SHORT).show()
            );
            viewPager.setAdapter(adapter);

            // 3. Inisialisasi Indikator Titik
            if (layoutDots != null) {
                setupIndicators(slides.size());
                setCurrentIndicator(0);
            }

            // 4. Sinkronisasi Indikator saat Slide Digeser
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setCurrentIndicator(position);
                }
            });
        }

        // 5. Setup Gesture Swipe Up
        setupSwipeUpGesture();
    }

    private void setupSwipeUpGesture() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();

                    if (Math.abs(diffY) > Math.abs(diffX)) {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY < 0) { // Geser ke atas
                                onSwipeUpDetected();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });

        if (imgLogoSilam != null) {
            imgLogoSilam.setOnLongClickListener(v -> {
                onSwipeUpDetected();
                return true;
            });
        }
    }

    private void onSwipeUpDetected() {
        Toast.makeText(LandingPageActivity.this, "Akses Khusus Petugas Dideteksi...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LandingPageActivity.this, LoginPegawaiActivity.class);
        startActivity(intent);
    }

    private void setupIndicators(int count) {
        if (layoutDots == null) return;

        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_landing_page)
            );

            // Atur margin antar titik secara konsisten
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(6, 0, 6, 0);
            imageView.setLayoutParams(layoutParams);

            layoutDots.addView(imageView);
        }
    }

    private void setCurrentIndicator(int index) {
        if (layoutDots == null) return;

        int childCount = layoutDots.getChildCount();

        // Konversi ukuran Dp ke Pixel untuk kepastian presisi di semua layar
        int activeWidth = dpToPx(24);   // Lebar kapsul aktif
        int inactiveWidth = dpToPx(8); // Lebar lingkaran tidak aktif
        int height = dpToPx(8);        // Tinggi indikator

        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutDots.getChildAt(i);
            if (imageView != null) {
                boolean isSelected = (i == index);
                imageView.setSelected(isSelected);

                // Perbarui dimensi LayoutParams secara proporsional sesuai status seleksi
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                params.width = isSelected ? activeWidth : inactiveWidth;
                params.height = height;
                params.setMargins(6, 0, 6, 0);
                imageView.setLayoutParams(params);
            }
        }
    }

    // Utility Method untuk Konversi DP ke PX
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}