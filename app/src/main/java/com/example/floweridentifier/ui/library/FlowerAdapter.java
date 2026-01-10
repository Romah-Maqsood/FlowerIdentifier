package com.example.floweridentifier.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floweridentifier.R;
import com.example.floweridentifier.data.Flower;

import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.FlowerViewHolder> {

    private final List<Flower> flowerList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Flower flower);
    }

    public FlowerAdapter(List<Flower> flowerList, OnItemClickListener listener) {
        this.flowerList = flowerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flower, parent, false);
        return new FlowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerViewHolder holder, int position) {
        Flower flower = flowerList.get(position);
        holder.flowerName.setText(flower.getName());
        holder.flowerImage.setImageResource(flower.getImageResourceId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(flower));
    }

    @Override
    public int getItemCount() {
        return flowerList.size();
    }

    static class FlowerViewHolder extends RecyclerView.ViewHolder {
        ImageView flowerImage;
        TextView flowerName;

        public FlowerViewHolder(@NonNull View itemView) {
            super(itemView);
            flowerImage = itemView.findViewById(R.id.flower_image);
            flowerName = itemView.findViewById(R.id.flower_name);
        }
    }
}
