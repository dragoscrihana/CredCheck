package com.example.credcheck.ui.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import androidx.fragment.app.Fragment;

import com.example.credcheck.R;
import com.example.credcheck.util.AuthManager;
import com.example.credcheck.model.EventModel;
import com.example.credcheck.model.TransactionModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private TransactionAdapter adapter;
    private final List<TransactionModel> transactions = new ArrayList<>();

    private static final String API_URL = "https://free-barnacle-exciting.ngrok-free.app/ui/presentations/recent";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        listView = root.findViewById(R.id.historyListView);
        adapter = new TransactionAdapter(requireContext(), transactions);
        listView.setAdapter(adapter);

        fetchTransactions(root);

        return root;
    }

    private void fetchTransactions(View root) {
        AuthManager.getFreshAccessToken(requireContext(), (accessToken, authState) -> {
            if (accessToken == null) {
                // handle token refresh failure (optional: redirect to login)
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                    int code = conn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        parseTransactions(response.toString(), root);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void parseTransactions(String json, View root) {
        try {
            JSONArray array = new JSONArray(json);
            List<TransactionModel> newList = new ArrayList<>();
            int passed = 0;
            int failed = 0;

            for (int i = 0; i < array.length(); i++) {
                JSONObject tx = array.getJSONObject(i);
                String transactionId = tx.getString("transactionId");
                String status = tx.getString("status");
                long lastUpdated = tx.getLong("lastUpdated");

                if ("ACCEPTED".equalsIgnoreCase(status))
                    passed++;
                else if ("DENIED".equalsIgnoreCase(status))
                    failed++;

                List<EventModel> events = new ArrayList<>();
                JSONArray eventArray = tx.getJSONArray("events");

                Locale locale = new Locale("ro", "RO");
                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", locale);

                for (int j = 0; j < eventArray.length(); j++) {
                    JSONObject ev = eventArray.getJSONObject(j);
                    String event = ev.getString("event");
                    String actor = ev.getString("actor");
                    long timestamp = ev.getLong("timestamp");

                    String formattedTime = timeFormat.format(new Date(timestamp));

                    JSONObject payload = ev.optJSONObject("payload");
                    String details = payload != null ? payload.toString(2) : "";

                    events.add(new EventModel(event, actor, formattedTime, details));
                }

                String date = timeFormat.format(new Date(lastUpdated));
                newList.add(new TransactionModel(transactionId, date, events, status));
            }

            final int finalPassed = passed;
            final int finalFailed = failed;

            requireActivity().runOnUiThread(() -> {
                transactions.clear();
                transactions.addAll(newList);
                adapter.notifyDataSetChanged();
                setupPieChart(root, finalPassed, finalFailed);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPieChart(View root, int passed, int failed) {
        PieChart pieChart = root.findViewById(R.id.pieChart);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(passed, "Passed"));
        entries.add(new PieEntry(failed, "Failed"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getColorBasedOnTheme());
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.2f", value);
            }
        });

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
        legend.setXEntrySpace(20f);
        legend.setDrawInside(false);

        pieChart.invalidate();
    }

    private int getColorBasedOnTheme() {
        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");
        return getResources().getColor(theme.equals("dark") ? android.R.color.white : android.R.color.black, null);
    }
}
