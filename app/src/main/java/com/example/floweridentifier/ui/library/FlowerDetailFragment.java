package com.example.floweridentifier.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floweridentifier.R;

public class FlowerDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_flower_detail, container, false);

        ImageView flowerImage = root.findViewById(R.id.flower_detail_image);
        TextView flowerName = root.findViewById(R.id.flower_detail_name);
        TextView scientificName = root.findViewById(R.id.flower_detail_scientific_name);
        TextView description = root.findViewById(R.id.flower_detail_description);

        if (getArguments() != null) {
            flowerImage.setImageResource(getArguments().getInt("imageResourceId"));
            flowerName.setText(getArguments().getString("flowerName"));
            scientificName.setText(getArguments().getString("scientificName"));
            description.setText(getArguments().getString("description"));
        }

        return root;
    }
}
