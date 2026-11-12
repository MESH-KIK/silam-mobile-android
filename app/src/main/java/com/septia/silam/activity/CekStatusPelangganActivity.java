package com.septia.silam.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.septia.silam.R;
import com.septia.silam.config.ServerConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class CekStatusPelangganActivity extends AppCompatActivity {

    // Input Views
    private ImageView btnKeluarLandingPage;
    private EditText etNomorNota;
    private CardView btnTempel;
    private Button btnCekStatusSekarang;

    // Output Views
    private LinearLayout layoutHasilCekStatus;
    private TextView tvNamaPelanggan, tvNomorNotaBadge;
    private TextView tvNamaLayanan, tvDetailBerat, tvTotalTagihan, tvStatusPembayaran;
    private TextView tvStepNama, tvStepStatus, tvStepDeskripsi;
    private Button btnBagikanStatus, btnHubungiCS;

    // Networking & Realtime Polling
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnablePolling;
    private RequestQueue requestQueue;
    private String lastSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cek_status_pelanggan);

        requestQueue = Volley.newRequestQueue(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnKeluarLandingPage = findViewById(R.id.btnKeluarLandingPage);
        etNomorNota          = findViewById(R.id.etNomorNota);
        btnTempel            = findViewById(R.id.btnTempel);
        btnCekStatusSekarang = findViewById(R.id.btnCekStatusSekarang);

        // Layout Container
        layoutHasilCekStatus = findViewById(R.id.layoutHasilCekStatus);
        tvNamaPelanggan      = findViewById(R.id.tvNamaPelanggan);
        tvNomorNotaBadge     = findViewById(R.id.tvNomorNotaBadge);

        // Tautkan ID Layanan & Tagihan
        tvNamaLayanan        = findViewById(R.id.tvNamaLayanan);
        tvDetailBerat        = findViewById(R.id.tvDetailBerat);
        tvTotalTagihan       = findViewById(R.id.tvTotalTagihan);
        tvStatusPembayaran   = findViewById(R.id.tvStatusPembayaran);

        // Tautkan ID Status Progres
        tvStepNama           = findViewById(R.id.tvStepNama);
        tvStepStatus         = findViewById(R.id.tvStepStatus);
        tvStepDeskripsi      = findViewById(R.id.tvStepDeskripsi);

        // Tombol Aksi
        btnBagikanStatus     = findViewById(R.id.btnBagikanStatus);
        btnHubungiCS         = findViewById(R.id.btnHubungiCS);
    }

    private void setupListeners() {
        if (btnKeluarLandingPage != null) {
            btnKeluarLandingPage.setOnClickListener(v -> showExitDialog());
        }

        if (btnTempel != null) {
            btnTempel.setOnClickListener(v -> pasteFromClipboard());
        }

        if (btnCekStatusSekarang != null) {
            btnCekStatusSekarang.setOnClickListener(v -> {
                String query = etNomorNota.getText().toString().trim();
                if (query.isEmpty()) {
                    Toast.makeText(this, "Masukkan nomor nota terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                lastSearchQuery = query;
                startRealtimePolling();
            });
        }

        if (btnBagikanStatus != null) {
            btnBagikanStatus.setOnClickListener(v -> {
                String textShare = "Halo, status cucian SILAM dengan No. Nota #" + lastSearchQuery + " sedang diproses!";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, textShare);
                startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
            });
        }

        if (btnHubungiCS != null) {
            btnHubungiCS.setOnClickListener(v -> {
                String urlWA = "https://api.whatsapp.com/send?phone=6282119793095&text=Halo%20SILAM,%20saya%20ingin%20menanyakan%20status%20pesanan%20no%20" + lastSearchQuery;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urlWA));
                startActivity(i);
            });
        }
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                etNomorNota.setText(item.getText().toString().trim());
                Toast.makeText(this, "Teks berhasil ditempel", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Papan klip kosong", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRealtimePolling() {
        stopRealtimePolling();

        runnablePolling = new Runnable() {
            @Override
            public void run() {
                fetchStatusPesanan(lastSearchQuery);
                handler.postDelayed(this, 5000); // Update otomatis tiap 5 detik
            }
        };

        handler.post(runnablePolling);
    }

    private void fetchStatusPesanan(String query) {
        String url = ServerConfig.API_TRACKING + query;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String noNota        = response.optString("nomor_pesanan", query);
                        String namaPelanggan = response.optString("pelanggan", "Pelanggan");
                        String statusLabel   = response.optString("status_label", "Sedang Diproses");
                        String statusBayar   = response.optString("status_pembayaran", "belum_lunas");
                        double totalTagihan  = response.optDouble("total_tagihan", 0);

                        // Tampilkan Container Rincian
                        layoutHasilCekStatus.setVisibility(View.VISIBLE);

                        if (tvNamaPelanggan != null) tvNamaPelanggan.setText(namaPelanggan);
                        if (tvNomorNotaBadge != null) tvNomorNotaBadge.setText(noNota);

                        // 1. Format Tagihan & Status Bayar (Rp 12.000 & Lunas)
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        if (tvTotalTagihan != null) tvTotalTagihan.setText(formatRupiah.format(totalTagihan));

                        if (tvStatusPembayaran != null) {
                            if ("lunas".equalsIgnoreCase(statusBayar)) {
                                tvStatusPembayaran.setText("✓ Lunas");
                                tvStatusPembayaran.setTextColor(0xFF16A34A); // Warna Hijau
                            } else {
                                tvStatusPembayaran.setText("✕ Belum Lunas");
                                tvStatusPembayaran.setTextColor(0xFFDC2626); // Warna Merah
                            }
                        }

                        // 2. Data Layanan (Kilat, 1.00 Kg)
                        JSONArray arrayLayanan = response.optJSONArray("layanan");
                        if (arrayLayanan != null && arrayLayanan.length() > 0) {
                            JSONObject itemLayanan = arrayLayanan.getJSONObject(0);
                            String namaLayanan = itemLayanan.optString("nama", "-");
                            String jumlahBerat = itemLayanan.optString("jumlah", "0");
                            String satuan      = itemLayanan.optString("satuan", "Kg");

                            if (tvNamaLayanan != null) tvNamaLayanan.setText(namaLayanan);
                            if (tvDetailBerat != null) tvDetailBerat.setText(jumlahBerat + " " + satuan);
                        }

                        // 3. Status Tracking Terakhir (Cucian Selesai & Siap Diambil)
                        JSONArray arrayRiwayat = response.optJSONArray("riwayat");
                        if (arrayRiwayat != null && arrayRiwayat.length() > 0) {
                            JSONObject riwayatTerakhir = arrayRiwayat.getJSONObject(0);
                            String statusRiwayat = riwayatTerakhir.optString("status", statusLabel);
                            String ket           = riwayatTerakhir.optString("keterangan", "");

                            if (tvStepNama != null) tvStepNama.setText(statusRiwayat);
                            if (tvStepDeskripsi != null) {
                                tvStepDeskripsi.setText((ket.isEmpty() || ket.equals("null")) ? "Diperbarui oleh petugas outlet." : ket);
                            }
                        } else {
                            if (tvStepNama != null) tvStepNama.setText(statusLabel);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CekStatusPelangganActivity.this, "Gagal memproses data JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    layoutHasilCekStatus.setVisibility(View.GONE);
                    String errorMsg = "Pesanan tidak ditemukan atau Server Unreachable";
                    if (error.networkResponse != null) {
                        errorMsg = "HTTP Status: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(CekStatusPelangganActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
        );

        android.util.Log.d("SILAM_API", "URL yang dipanggil: " + url);
        requestQueue.add(jsonRequest);
    }

    private void stopRealtimePolling() {
        if (handler != null && runnablePolling != null) {
            handler.removeCallbacks(runnablePolling);
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari halaman Cek Status?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    stopRealtimePolling();
                    Intent intent = new Intent(CekStatusPelangganActivity.this, LandingPageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRealtimePolling();
    }
}