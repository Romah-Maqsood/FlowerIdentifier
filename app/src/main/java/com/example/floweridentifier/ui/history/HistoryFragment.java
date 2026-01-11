package com.example.floweridentifier.ui.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floweridentifier.R;
import com.example.floweridentifier.data.Prediction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {

    private HistoryAdapter adapter;
    private ArrayList<Prediction> historyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistory();

        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        Button clearButton = root.findViewById(R.id.btn_clear_history);
        clearButton.setOnClickListener(v -> clearHistory());

        return root;
    }

    private void loadHistory() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("prediction_history", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("history", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Prediction>>() {}.getType();
        historyList = gson.fromJson(json, type);
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }

    private void clearHistory() {
        historyList.clear();
        adapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("prediction_history", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("history", "[]");
        editor.apply();
    }
}
