package com.septia.silam.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.septia.silam.R;
import com.septia.silam.activity.DashboardPegawaiActivity;
import com.septia.silam.config.ApiClient;
import com.septia.silam.config.ServerConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class KelolaAntrianFragment extends Fragment {

    private static final long POLLING_INTERVAL = 5000; // 5 detik

    private static final String STATUS_ANTRIAN = "antrian";
    private static final String STATUS_PROSES  = "proses";
    private static final String STATUS_SIAP    = "siap_ambil";

    private TextView tvSubJudul, tvKosong;
    private TextView chipSemua, chipAntrian, chipProses, chipSiap;
    private EditText etCari;
    private LinearLayout llDaftar;

    private String filterAktif = null; // null = semua
    private JSONArray dataList = new JSONArray();
    private boolean sudahMuat = false;
    private boolean isFirstError = true;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pollingTask = new Runnable() {
        @Override
        public void run() {
            muatSemua();
            handler.postDelayed(this, POLLING_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_kelola_antrian, container, false);

        tvSubJudul  = v.findViewById(R.id.tvSubJudul);
        tvKosong    = v.findViewById(R.id.tvKosong);
        chipSemua   = v.findViewById(R.id.chipSemua);
        chipAntrian = v.findViewById(R.id.chipAntrian);
        chipProses  = v.findViewById(R.id.chipProses);
        chipSiap    = v.findViewById(R.id.chipSiap);
        etCari      = v.findViewById(R.id.etCariAntrian);
        llDaftar    = v.findViewById(R.id.llDaftar);

        chipSemua.setOnClickListener(x -> pilihFilter(null));
        chipAntrian.setOnClickListener(x -> pilihFilter(STATUS_ANTRIAN));
        chipProses.setOnClickListener(x -> pilihFilter(STATUS_PROSES));
        chipSiap.setOnClickListener(x -> pilihFilter(STATUS_SIAP));

        etCari.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { render(); }
        });

        tampilkanChipAktif();
        return v;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(pollingTask);
        tvSubJudul = null; tvKosong = null; llDaftar = null; etCari = null;
        chipSemua = null; chipAntrian = null; chipProses = null; chipSiap = null;
        sudahMuat = false;
    }

    private void muatSemua() {
        if (!isAdded()) return;

        String url = ServerConfig.API_PESANAN;
        url += (filterAktif == null ? "?per_page=100" : "?status_kerja=" + filterAktif + "&per_page=100");

        getJson(url, r -> {
            JSONArray arr = r.optJSONArray("data");
            dataList = arr != null ? arr : new JSONArray();
            sudahMuat = true;
            render();
        });

        getJson(ServerConfig.API_PESANAN, r ->
                setChip(chipSemua, "Semua", r.optInt("total", 0)));
        getJson(ServerConfig.API_PESANAN + "?status_kerja=" + STATUS_ANTRIAN, r -> {
            setChip(chipAntrian, "Perlu Dicuci", r.optInt("total", 0));
            hitungAktif(r.optInt("total", 0), -1);
        });
        getJson(ServerConfig.API_PESANAN + "?status_kerja=" + STATUS_PROSES, r -> {
            setChip(chipProses, "Diproses", r.optInt("total", 0));
            hitungAktif(-1, r.optInt("total", 0));
        });
        getJson(ServerConfig.API_PESANAN + "?status_kerja=" + STATUS_SIAP, r ->
                setChip(chipSiap, "Siap Diambil", r.optInt("total", 0)));
    }

    private int totalAntrian = 0, totalProses = 0;

    private void hitungAktif(int antrian, int proses) {
        if (antrian >= 0) totalAntrian = antrian;
        if (proses >= 0) totalProses = proses;
        if (tvSubJudul != null) {
            tvSubJudul.setText((totalAntrian + totalProses) + " pesanan aktif perlu ditangani");
        }
    }

    private void setChip(TextView chip, String label, int jumlah) {
        if (chip != null) chip.setText(label + "  " + jumlah);
    }

    private void getJson(String url, Response.Listener<JSONObject> ok) {
        if (!isAdded()) return;

        ApiClient.get(requireContext()).request(
                Request.Method.GET, url, null,
                response -> {
                    if (!isAdded()) return;
                    isFirstError = true;
                    ok.onResponse(response);
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

    private void pilihFilter(@Nullable String status) {
        filterAktif = status;
        tampilkanChipAktif();
        sudahMuat = false;
        llDaftar.removeAllViews();
        tvKosong.setText("Memuat data...");
        tvKosong.setVisibility(View.VISIBLE);
        muatSemua();
    }

    private void tampilkanChipAktif() {
        gayaChip(chipSemua,   filterAktif == null);
        gayaChip(chipAntrian, STATUS_ANTRIAN.equals(filterAktif));
        gayaChip(chipProses,  STATUS_PROSES.equals(filterAktif));
        gayaChip(chipSiap,    STATUS_SIAP.equals(filterAktif));
    }

    private void gayaChip(TextView chip, boolean aktif) {
        if (chip == null) return;
        chip.setBackgroundColor(Color.parseColor(aktif ? "#2E4A85" : "#D3E0FA"));
        chip.setTextColor(Color.parseColor(aktif ? "#FFFFFF" : "#2E4A85"));
    }

    // ------------------------------------------------------------------
    // Tampilkan daftar
    // ------------------------------------------------------------------

    private void render() {
        if (!isAdded() || llDaftar == null || !sudahMuat) return;

        String kata = etCari != null ? etCari.getText().toString().trim().toLowerCase() : "";
        llDaftar.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM HH:mm", new Locale("id", "ID"));
        long sekarang = System.currentTimeMillis();
        int tampil = 0;

        for (int i = 0; i < dataList.length(); i++) {
            JSONObject o = dataList.optJSONObject(i);
            if (o == null) continue;

            String nomor = o.optString("nomor_pesanan", "-");
            JSONObject pel = o.optJSONObject("pelanggan");
            String nama = pel != null ? pel.optString("nama_pelanggan", "-") : "-";

            if (!kata.isEmpty()
                    && !nomor.toLowerCase().contains(kata)
                    && !nama.toLowerCase().contains(kata)) continue;

            View item = inflater.inflate(R.layout.item_antrian, llDaftar, false);

            TextView tvNomor    = item.findViewById(R.id.tvNomor);
            TextView tvStatus   = item.findViewById(R.id.tvStatus);
            TextView tvInisial  = item.findViewById(R.id.tvInisial);
            TextView tvPelanggan = item.findViewById(R.id.tvPelanggan);
            TextView tvDetail   = item.findViewById(R.id.tvDetail);
            TextView tvEstimasi = item.findViewById(R.id.tvEstimasi);
            Button btnAksi      = item.findViewById(R.id.btnAksi);

            String status = o.optString("status_kerja", "");

            tvNomor.setText("#" + nomor);
            tvStatus.setText(labelStatus(status));
            tvInisial.setText(inisial(nama));
            tvPelanggan.setText(nama);

            String detail = o.isNull("status_detail") ? "" : o.optString("status_detail", "");
            tvDetail.setText(detail.isEmpty() ? labelStatus(status) : detail);

            long est = parseWaktu(o.isNull("tanggal_estimasi_selesai")
                    ? "" : o.optString("tanggal_estimasi_selesai", ""));
            if (est > 0) {
                boolean telat = est < sekarang && !STATUS_SIAP.equals(status);
                tvEstimasi.setText((telat ? "Terlambat\n" : "Estimasi\n") + fmt.format(new Date(est)));
                tvEstimasi.setTextColor(Color.parseColor(telat ? "#EF4444" : "#2E4A85"));
            } else {
                tvEstimasi.setText("");
            }

            final int id = o.optInt("id_pesanan", 0);
            final JSONObject dataPesanan = o;

            if (STATUS_ANTRIAN.equals(status)) {
                btnAksi.setText("Mulai Cuci");
                btnAksi.setOnClickListener(v -> {
                    tampilkanDialogKonfirmasi(id, dataPesanan, STATUS_PROSES, "Sedang dicuci", btnAksi);
                });
            } else if (STATUS_PROSES.equals(status)) {
                btnAksi.setText("Selesai Cuci, Siap Diambil");
                btnAksi.setOnClickListener(v -> {
                    tampilkanDialogKonfirmasi(id, dataPesanan, STATUS_SIAP, "Cucian selesai, siap diambil", btnAksi);
                });
            } else {
                btnAksi.setVisibility(View.GONE);
            }

            llDaftar.addView(item);
            tampil++;
        }

        if (tampil == 0) {
            tvKosong.setText(kata.isEmpty() ? "Belum ada pesanan" : "Pesanan tidak ditemukan");
            tvKosong.setVisibility(View.VISIBLE);
        } else {
            tvKosong.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------------
    // Dialog Konfirmasi Detail Pesanan & Catatan Pegawai (Opsional)
    // ------------------------------------------------------------------

    private void tampilkanDialogKonfirmasi(int idPesanan, JSONObject pesanan, String statusBaru, String statusDetail, Button btnAksi) {
        if (!isAdded() || getContext() == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_konfirmasi_status, null);

        TextView tvNomorNota      = dialogView.findViewById(R.id.tvDialogNomorNota);
        TextView tvNamaPelanggan  = dialogView.findViewById(R.id.tvDialogNamaPelanggan);
        TextView tvDetailLayanan  = dialogView.findViewById(R.id.tvDialogDetailLayanan);
        TextView tvStatusBaru     = dialogView.findViewById(R.id.tvDialogStatusBaru);
        EditText etCatatan        = dialogView.findViewById(R.id.etDialogCatatan);

        String nomorNota = pesanan.optString("nomor_pesanan", "-");
        JSONObject pel = pesanan.optJSONObject("pelanggan");
        String namaPelanggan = pel != null ? pel.optString("nama_pelanggan", "-") : "-";
        String noHp = pel != null ? pel.optString("nomor_telepon", "") : "";

        tvNomorNota.setText("Nomor Nota: #" + nomorNota);
        tvNamaPelanggan.setText(namaPelanggan + (!noHp.isEmpty() ? " (" + noHp + ")" : ""));
        tvStatusBaru.setText(labelStatus(statusBaru));

        StringBuilder sbLayanan = new StringBuilder();
        JSONArray arrDetail = pesanan.optJSONArray("detail");

        if (arrDetail != null && arrDetail.length() > 0) {
            for (int i = 0; i < arrDetail.length(); i++) {
                JSONObject d = arrDetail.optJSONObject(i);
                if (d != null) {
                    JSONObject lay = d.optJSONObject("layanan");
                    String namaLayanan = lay != null ? lay.optString("nama_layanan", "Layanan Laundry") : "Layanan Laundry";
                    double jumlah = d.optDouble("jumlah_berat", 1.0);
                    String satuan = lay != null ? lay.optString("satuan", "Kg") : "Kg";

                    sbLayanan.append("• ").append(namaLayanan)
                            .append(" (").append(jumlah).append(" ").append(satuan).append(")\n");
                }
            }
        } else {
            sbLayanan.append("- Layanan Standar");
        }

        tvDetailLayanan.setText(sbLayanan.toString().trim());

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Ya, Update Status", (dialog, which) -> {
                    btnAksi.setEnabled(false);
                    String catatanKeterangan = etCatatan != null ? etCatatan.getText().toString().trim() : "";
                    ubahStatus(idPesanan, statusBaru, statusDetail, catatanKeterangan);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ------------------------------------------------------------------
    // Method Helper Pembantu
    // ------------------------------------------------------------------

    private String labelStatus(String status) {
        if (STATUS_ANTRIAN.equals(status)) return "Perlu Dicuci";
        if (STATUS_PROSES.equals(status))  return "Sedang Dicuci / Diproses";
        if (STATUS_SIAP.equals(status))    return "Siap Diambil";
        return status;
    }

    private String inisial(String nama) {
        String[] p = nama.trim().split("\\s+");
        if (p.length == 0 || p[0].isEmpty()) return "-";
        String s = p[0].substring(0, 1);
        if (p.length > 1) s += p[1].substring(0, 1);
        return s.toUpperCase();
    }

    private long parseWaktu(String s) {
        if (s == null || s.length() < 19) return -1;
        try {
            if (s.contains("T")) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.parse(s.substring(0, 19)).getTime();
            }
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return f.parse(s.substring(0, 19)).getTime();
        } catch (Exception e) {
            return -1;
        }
    }

    // ------------------------------------------------------------------
    // Ubah status via API HTTP PATCH /pesanan/{id}/status
    // ------------------------------------------------------------------

    private void ubahStatus(int id, String statusBaru, String statusDetail, String keterangan) {
        if (!isAdded()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("status_kerja", statusBaru);
            body.put("status_detail", statusDetail);
            if (!keterangan.isEmpty()) {
                body.put("keterangan", keterangan);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiClient.get(requireContext()).request(
                Request.Method.PATCH,
                ServerConfig.pesananStatus(id),
                body,
                response -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    muatSemua();
                },
                error -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), ApiClient.errorMessage(error), Toast.LENGTH_LONG).show();
                    muatSemua();
                }
        );
    }
}