package com.septia.silam.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.septia.silam.R;
import com.septia.silam.activity.DashboardPegawaiActivity;
import com.septia.silam.config.ApiClient;
import com.septia.silam.config.ServerConfig;

public class DashboardFragment extends Fragment {

    private static final long POLLING_INTERVAL = 10000; // 10 detik

    // HARUS sama persis dengan key di PesananModel::STATUS_KERJA milik Naila
    private static final String STATUS_ANTRIAN = "antrian";
    private static final String STATUS_PROSES  = "proses";
    private static final String STATUS_SIAP    = "siap_ambil";

    private TextView tvNamaKasir, tvTotalNota;
    private TextView tvJumlahAntrian, tvJumlahProses, tvJumlahSiap;
    private CardView btnKelolaAntrian;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFirstError = true;

    private final Runnable pollingTask = new Runnable() {
        @Override
        public void run() {
            fetchRingkasanAntrian();
            handler.postDelayed(this, POLLING_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupUserData();
        setupListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(pollingTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollingTask);
    }

    private void initViews(View view) {
        tvNamaKasir      = view.findViewById(R.id.tvNamaKasir);
        tvTotalNota      = view.findViewById(R.id.tvTotalNota);
        btnKelolaAntrian = view.findViewById(R.id.btnKelolaAntrian);
        tvJumlahAntrian  = view.findViewById(R.id.tvJumlahAntrian);
        tvJumlahProses   = view.findViewById(R.id.tvJumlahProses);
        tvJumlahSiap     = view.findViewById(R.id.tvJumlahSiap);
    }

    private void setupUserData() {
        if (getActivity() == null) return;
        SharedPreferences pref = getActivity()
                .getSharedPreferences("SILAM_SESSION", Context.MODE_PRIVATE);
        String nama = pref.getString("nama_pegawai", "Pegawai");
        if (tvNamaKasir != null) tvNamaKasir.setText("Halo, " + nama);
    }

    private void setupListeners() {
        if (btnKelolaAntrian != null) {
            btnKelolaAntrian.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardPegawaiActivity) {
                    ((DashboardPegawaiActivity) getActivity()).navigateToTab(R.id.nav_antrian);
                }
            });
        }
    }

    // 4 request ringan: tiga status + total semua pesanan
    private void fetchRingkasanAntrian() {
        if (!isAdded()) return;
        fetchTotal(STATUS_ANTRIAN, tvJumlahAntrian, "%d");
        fetchTotal(STATUS_PROSES,  tvJumlahProses,  "%d");
        fetchTotal(STATUS_SIAP,    tvJumlahSiap,    "%d");
        fetchTotal(null,           tvTotalNota,     "Total: %d Nota");
    }

    /** Minta /pesanan (dengan filter status kalau ada), lalu baca field "total" dari paginate. */
    private void fetchTotal(@Nullable String status, TextView target, String format) {
        String url = ServerConfig.API_PESANAN;
        if (status != null) url += "?status_kerja=" + status;
        final String finalUrl = url;

        ApiClient.get(requireContext()).request(
                Request.Method.GET,
                finalUrl,
                null,
                response -> {
                    if (!isAdded()) return;
                    isFirstError = true;

                    int total = response.optInt("total", 0);
                    Log.d("SILAM_API", finalUrl + " -> total=" + total);

                    if (target != null) target.setText(String.format(format, total));
                },
                error -> {
                    if (!isAdded()) return;

                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        handler.removeCallbacks(pollingTask);
                        if (getActivity() instanceof DashboardPegawaiActivity) {
                            ((DashboardPegawaiActivity) getActivity()).forceLogout();
                        }
                        return;
                    }

                    if (isFirstError) {
                        isFirstError = false;
                        Toast.makeText(requireContext(),
                                ApiClient.errorMessage(error), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(pollingTask);
        tvNamaKasir = null;
        tvTotalNota = null;
        tvJumlahAntrian = null;
        tvJumlahProses = null;
        tvJumlahSiap = null;
        btnKelolaAntrian = null;
    }
}