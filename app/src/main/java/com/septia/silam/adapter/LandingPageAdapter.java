package com.septia.silam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septia.silam.R;
import com.septia.silam.activity.CekStatusPelangganActivity;
import com.septia.silam.model.SlideModel;

import java.util.List;

public class LandingPageAdapter extends RecyclerView.Adapter<LandingPageAdapter.SlideViewHolder> {

    private final List<SlideModel> slideList;
    private final Runnable onCekStatusClickListener;

    public LandingPageAdapter(List<SlideModel> slideList, Runnable onCekStatusClickListener) {
        this.slideList = slideList;
        this.onCekStatusClickListener = onCekStatusClickListener;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slide_landing_page, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        SlideModel slide = slideList.get(position);

        // Bind data dari SlideModel ke View
        holder.tvSlideTagline.setText(slide.getTagline());
        holder.tvSlideTitle.setText(slide.getTitle());
        holder.tvSlideDescription.setText(slide.getDescription());
        holder.imgSlideIcon.setImageResource(slide.getIconResId());

        // Hanya tampilkan Kotak Aksi Nota pada slide yang bernilai true (Slide #1)
        if (slide.isHasNotaAction()) {
            holder.layoutNotaAction.setVisibility(View.VISIBLE);
        } else {
            holder.layoutNotaAction.setVisibility(View.GONE);
        }

        // Listener Tombol Cek Status -> Berpindah ke CekStatusPelangganActivity
        holder.btnCekStatus.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CekStatusPelangganActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return slideList != null ? slideList.size() : 0;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlideIcon;
        TextView tvSlideTagline, tvSlideTitle, tvSlideDescription;
        LinearLayout layoutNotaAction;
        Button btnCekStatus;

        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlideIcon = itemView.findViewById(R.id.imgSlideIcon);
            tvSlideTagline = itemView.findViewById(R.id.tvSlideTagline);
            tvSlideTitle = itemView.findViewById(R.id.tvSlideTitle);
            tvSlideDescription = itemView.findViewById(R.id.tvSlideDescription);
            layoutNotaAction = itemView.findViewById(R.id.layoutNotaAction);
            btnCekStatus = itemView.findViewById(R.id.btnCekStatus);
        }
    }
}