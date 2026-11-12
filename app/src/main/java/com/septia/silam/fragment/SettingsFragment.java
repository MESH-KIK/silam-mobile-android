package com.septia.silam.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.septia.silam.R;
import com.septia.silam.activity.LandingPageActivity;

public class SettingsFragment extends Fragment {

    private TextView tvNamaPegawai, tvIdPegawai, tvJabatan, tvCabang;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvNamaPegawai = view.findViewById(R.id.tvNamaPegawai);
        tvIdPegawai = view.findViewById(R.id.tvIdPegawai);
        tvJabatan = view.findViewById(R.id.tvJabatan);
        tvCabang = view.findViewById(R.id.tvCabang);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Contoh Set Data Pegawai
        tvNamaPegawai.setText("Ratna Wulandari");
        tvIdPegawai.setText("ID: OP-882");
        tvJabatan.setText("Kasir & Operator Cuci");
        tvCabang.setText("Cabang Sukajadi - Bandung");

        // Fitur Logout Aman
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Berhasil Logout. Sesi diakhiri.", Toast.LENGTH_SHORT).show();

            // Kembali ke LandingPageActivity & hapus backstack
            Intent intent = new Intent(getActivity(), LandingPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}