package com.example.floweridentifier.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floweridentifier.R;
import com.example.floweridentifier.data.Prediction;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<Prediction> predictionList;

    public HistoryAdapter(List<Prediction> predictionList) {
        this.predictionList = predictionList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Prediction prediction = predictionList.get(position);
        holder.flowerName.setText(prediction.getFlowerName());
        holder.predictionDate.setText(prediction.getDate());
        holder.flowerImage.setImageResource(prediction.getImageResourceId());
    }

    @Override
    public int getItemCount() {
        return predictionList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView flowerImage;
        TextView flowerName;
        TextView predictionDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            flowerImage = itemView.findViewById(R.id.history_flower_image);
            flowerName = itemView.findViewById(R.id.history_flower_name);
            predictionDate = itemView.findViewById(R.id.history_date);
        }
    }
}
