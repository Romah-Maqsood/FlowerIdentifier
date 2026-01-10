package com.example.floweridentifier.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floweridentifier.R;
import com.example.floweridentifier.data.Flower;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements FlowerAdapter.OnItemClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_library, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_library);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<Flower> flowerList = new ArrayList<>();
        flowerList.add(new Flower("Rose", "Rosa", "A woody perennial flowering plant.", R.drawable.flower_rose));
        flowerList.add(new Flower("Daisy", "Bellis perennis", "A common European composite flower.", R.drawable.flower_daisy));
        flowerList.add(new Flower("Dandelion", "Taraxacum", "A widely distributed weed.", R.drawable.flower_dandelion));
        flowerList.add(new Flower("Tulip", "Tulipa", "A bulbous spring-flowering plant.", R.drawable.flower_tulip));
        flowerList.add(new Flower("Sunflower", "Helianthus", "A tall North American plant.", R.drawable.flower_sunflower));

        FlowerAdapter adapter = new FlowerAdapter(flowerList, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onItemClick(Flower flower) {
        Bundle bundle = new Bundle();
        bundle.putString("flowerName", flower.getName());
        bundle.putString("scientificName", flower.getScientificName());
        bundle.putString("description", flower.getDescription());
        bundle.putInt("imageResourceId", flower.getImageResourceId());
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_library_to_flowerDetailFragment, bundle);
    }
}
