package com.example.credcheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import androidx.fragment.app.Fragment;

import com.example.credcheck.model.EventModel;
import com.example.credcheck.model.TransactionModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

public class HistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        PieChart pieChart = root.findViewById(R.id.pieChart);

        int passed = 11;
        int failed = 6;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(passed, "Passed"));
        entries.add(new PieEntry(failed, "Failed"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getColorBasedOnTheme());
        pieChart.setDrawEntryLabels(false);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.2f", value);
            }
        });
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getColorBasedOnTheme());

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setHoleColor(android.R.color.transparent);
        pieChart.setCenterText("Credential Stats");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(getColorBasedOnTheme());
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(getColorBasedOnTheme());
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.invalidate();

        ListView listView = root.findViewById(R.id.historyListView);
        List<TransactionModel> transactions = getDummyData();
        TransactionAdapter adapter = new TransactionAdapter(requireContext(), transactions);
        listView.setAdapter(adapter);

        return root;
    }

    private int getColorBasedOnTheme() {
        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");

        if (theme.equals("dark")) {
            return getResources().getColor(android.R.color.white, null);
        } else {
            return getResources().getColor(android.R.color.black, null);
        }
    }

    private List<TransactionModel> getDummyData() {
        List<EventModel> events = new ArrayList<>();
        events.add(new EventModel("Transaction initialized", "Verifier", 1746291795075L, "request_uri: /wallet/request.jwt/..."));
        events.add(new EventModel("Request object retrieved", "Wallet", 1746291811098L, "jwt: eyJra..."));
        events.add(new EventModel("Wallet response posted", "Wallet", 1746291816994L, "vp_token: eyJhb..."));

        List<TransactionModel> transactions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            transactions.add(new TransactionModel("GJ8HUnJR4T...", "2025-05-03 14:56", events));
        }

        return transactions;
    }
}
